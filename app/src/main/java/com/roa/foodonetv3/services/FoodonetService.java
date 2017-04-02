package com.roa.foodonetv3.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.model.User;
import com.roa.foodonetv3.serverMethods.StartFoodonetServiceMethods;
import com.roa.foodonetv3.db.GroupMembersDBHandler;
import com.roa.foodonetv3.db.GroupsDBHandler;
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.db.RegisteredUsersDBHandler;
import com.roa.foodonetv3.model.Group;
import com.roa.foodonetv3.model.GroupMember;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.model.PublicationReport;
import com.roa.foodonetv3.model.RegisteredUser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

/** main service to communicate with foodonet server */
public class FoodonetService extends IntentService {
    private static final String TAG = "FoodonetService";

    private ArrayList<Parcelable> data;
    private String[] args;
    private static final int TIMEOUT_TIME = 5000;

    public FoodonetService() {
        super("FoodonetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // a universal service for all foodonet server communications */
        if (intent != null) {
            Intent finishedIntent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
            boolean serviceError = false;
            int actionType = intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1);
            args = intent.getStringArrayExtra(ReceiverConstants.ADDRESS_ARGS);
            data = intent.getParcelableArrayListExtra(ReceiverConstants.DATA);
            String urlAddress = StartFoodonetServiceMethods.getUrlAddress(this,actionType, args);
            HttpsURLConnection connection = null;
            BufferedReader reader = null;
            URL url;
            StringBuilder builder = new StringBuilder();
            try {
                url = new URL(urlAddress);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setConnectTimeout(TIMEOUT_TIME);
                // TODO: 28/11/2016 add logic for timeout
                int httpType = StartFoodonetServiceMethods.getHTTPType(actionType);
                switch (httpType){
                    case CommonConstants.HTTP_GET:
                        break;

                    case CommonConstants.HTTP_POST:
                        connection.setRequestMethod("POST");
                    case CommonConstants.HTTP_PUT:
                        if(httpType==CommonConstants.HTTP_PUT){
                            connection.setRequestMethod("PUT");
                        }
                        connection.addRequestProperty("Accept","application/json");
                        connection.addRequestProperty("Content-Type","application/json");
                        connection.setDoOutput(true);
                        OutputStream putOs = connection.getOutputStream();
                        BufferedWriter putWriter = new BufferedWriter(new OutputStreamWriter(putOs,"utf-8"));
                        String putJsonToSend = intent.getStringExtra(ReceiverConstants.JSON_TO_SEND);
                        putWriter.write(putJsonToSend);
                        putWriter.flush();
                        putWriter.close();
                        putOs.close();
                        break;

                    case CommonConstants.HTTP_DELETE:
                        connection.setRequestMethod("DELETE");
                        break;
                    default:
                        serviceError = true;
                }
                int responseCode = connection.getResponseCode();
                if(responseCode !=HttpsURLConnection.HTTP_OK && responseCode != HttpsURLConnection.HTTP_CREATED
                        // right now deleting the last member from a group gives response 500 from the server, though still deleting the member,
                        // in order to operate adding this logic here for now
                        && responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR){
                    serviceError = true;
                } else{
                    // right now deleting the last member from a group gives response 500 from the server, though still deleting the member,
                    // in order to operate adding this logic here for now
                    if(responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }
                    finishedIntent = addResponseToIntent(actionType,builder.toString(),finishedIntent);
                }
            } catch (IOException e) {
                serviceError = true;
                Log.e(TAG,e.getMessage());
            }
            finally {
                if(connection!= null){
                    connection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG,e.getMessage());
                    }
                }
            }
            finishedIntent.putExtra(ReceiverConstants.ACTION_TYPE,actionType);
            finishedIntent.putExtra(ReceiverConstants.SERVICE_ERROR,serviceError);
            LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
        }
    }

    private Intent addResponseToIntent(int actionType, String responseRoot, Intent intent){
        Log.d(TAG,responseRoot);
        try {
            PublicationsDBHandler publicationsDBHandler;
            GroupsDBHandler groupsDBHandler;
            RegisteredUsersDBHandler registeredUsersDBHandler;
            GroupMembersDBHandler groupMembersDBHandler;
            if(actionType == ReceiverConstants.ACTION_GET_PUBLICATIONS){
                // get the users groups id, as we don't care about the others */
                groupsDBHandler = new GroupsDBHandler(this);
                ArrayList<Long> groupsIDs = groupsDBHandler.getGroupsIDs();
                ArrayList<Publication> publications = new ArrayList<>();
                JSONArray rootGetPublications;
                rootGetPublications = new JSONArray(responseRoot);

                long id,audience;
                String title,subtitle,address,startingDate,endingDate,contactInfo,activeDeviceDevUUID,photoURL,identityProviderUserName,priceDescription;
                short typeOfCollecting;
                Double lat,lng,price;
                boolean isOnAir;
                int version;

                for (int i = 0; i < rootGetPublications.length(); i++) {
                    JSONObject publication = rootGetPublications.getJSONObject(i);
                    audience = publication.getLong("audience");

                    if(audience == 0 || groupsIDs.contains(audience)){
                        long publisherID = publication.getLong("publisher_id");
                        id = publication.getLong("id");
                        version = publication.getInt("version");
                        title = publication.getString("title");
                        subtitle = publication.getString("subtitle");
                        address = publication.getString("address");
                        typeOfCollecting = (short) publication.getInt("type_of_collecting");
                        lat = publication.getDouble("latitude");
                        lng = publication.getDouble("longitude");
                        startingDate = publication.getString("starting_date");
                        endingDate = publication.getString("ending_date");
                        contactInfo = publication.getString("contact_info");
                        isOnAir = publication.getBoolean("is_on_air");
                        activeDeviceDevUUID = publication.getString("active_device_dev_uuid");
                        photoURL = publication.getString("photo_url");

                        identityProviderUserName = publication.getString("identity_provider_user_name");
                        price = publication.getDouble("price");
                        priceDescription = publication.getString("price_description");
                        publications.add(new Publication(id, version, title, subtitle, address, typeOfCollecting, lat, lng, startingDate, endingDate, contactInfo, isOnAir,
                                activeDeviceDevUUID, photoURL, publisherID, audience, identityProviderUserName, price, priceDescription));
                    }
                }
                publicationsDBHandler = new PublicationsDBHandler(this);
                publicationsDBHandler.replaceAllPublications(publications);
                Intent getDataIntent = new Intent(this,GetDataService.class);
                getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS);
                startService(getDataIntent);
            }

            else if(actionType == ReceiverConstants.ACTION_ADD_PUBLICATION){
                JSONObject rootAddPublication = new JSONObject(responseRoot);
                long publicationID = rootAddPublication.getLong("id");
                int publicationVersion = rootAddPublication.getInt("version");
                intent.putExtra(Publication.PUBLICATION_ID,publicationID);
                intent.putExtra(Publication.PUBLICATION_VERSION,publicationVersion);
                if(data!= null){
                    Publication publication = (Publication) data.get(0);
                    publication.setId(publicationID);
                    publication.setVersion(publicationVersion);
                    publicationsDBHandler = new PublicationsDBHandler(this);
                    publicationsDBHandler.insertPublication(publication);
                    // instantiate the transfer utility for the s3*/
                    TransferUtility transferUtility = CommonMethods.getTransferUtility(this);
                    // if there is an image to upload */
                    if(publication.getPhotoURL()!=null && !publication.getPhotoURL().equals("")){
                        String[] split = publication.getPhotoURL().split(":");
                        File file = new File(split[1]);
                        File destFile = new File(CommonMethods.getPhotoPathByID(this,publicationID,publicationVersion));
                        Log.d(TAG,"pre"+file.getPath());
                        String s3Name = CommonMethods.getFileNameFromPublicationID(publicationID,publicationVersion);
                        boolean renamed = file.renameTo(destFile);
                        Log.d(TAG,"post"+destFile.getPath());
                        if(renamed){
                            transferUtility.upload(getResources().getString(R.string.amazon_publications_bucket),s3Name,destFile);
                        }else{
                            Log.d(TAG,"Rename failed");
                        }
                        // TODO: 25/01/2017 currently not checking if the upload was successful or not
                    }
                }
            }

            else if(actionType == ReceiverConstants.ACTION_EDIT_PUBLICATION){
                JSONObject root = new JSONObject(responseRoot);
                long publicationID = root.getLong("id");
                int publicationVersion = root.getInt("version");
                if(data!= null){
                    Publication publication = (Publication) data.get(0);
                    publication.setVersion(publicationVersion);
                    publicationsDBHandler = new PublicationsDBHandler(this);
                    publicationsDBHandler.updatePublication(publication);
                    // instantiate the transfer utility for the s3*/
                    TransferUtility transferUtility = CommonMethods.getTransferUtility(this);
                    // if there is an image to upload */
                    if(publication.getPhotoURL()!=null && !publication.getPhotoURL().equals("")){
                        String[] split = publication.getPhotoURL().split(":");
                        File file = new File(split[1]);
                        File destFile = new File(CommonMethods.getPhotoPathByID(this,publicationID,publicationVersion));
                        Log.d(TAG,"pre"+file.getPath());
                        String s3Name = CommonMethods.getFileNameFromPublicationID(publicationID,publicationVersion);
                        boolean renamed = file.renameTo(destFile);
                        Log.d(TAG,"post"+destFile.getPath());
                        if(renamed){
                            transferUtility.upload(getResources().getString(R.string.amazon_publications_bucket),s3Name,destFile);
                        }else{
                            Log.d(TAG,"Rename failed");
                        }
                        // TODO: 05/03/2017 currently not checking if the upload was successful or not
                    }
                }
            }

            else if(actionType == ReceiverConstants.ACTION_DELETE_PUBLICATION){
                publicationsDBHandler = new PublicationsDBHandler(this);
                publicationsDBHandler.deletePublication(Long.parseLong(args[0]));
                intent.putExtra(Publication.PUBLICATION_ID,Long.valueOf(args[0]));
                intent.putExtra(ReceiverConstants.UPDATE_DATA,true);
            }

            else if(actionType == ReceiverConstants.ACTION_GET_PUBLICATION){
                groupsDBHandler = new GroupsDBHandler(this);
                publicationsDBHandler = new PublicationsDBHandler(this);
                ArrayList<Long> groupsIDs = groupsDBHandler.getGroupsIDs();
                long id,audience;
                String title,subtitle,address,startingDate,endingDate,contactInfo,activeDeviceDevUUID,photoURL,identityProviderUserName,priceDescription;
                short typeOfCollecting;
                Double lat,lng,price;
                boolean isOnAir;
                int version;
                JSONObject publicationObject = new JSONObject(responseRoot);
                audience = publicationObject.getLong("audience");
                activeDeviceDevUUID = publicationObject.getString("active_device_dev_uuid");
                boolean updateData = false;

                if((audience == 0 || groupsIDs.contains(audience)) && !activeDeviceDevUUID.equals(CommonMethods.getDeviceUUID(this))){
                    long publisherID = publicationObject.getLong("publisher_id");
                    id = publicationObject.getLong("id");
                    version = publicationObject.getInt("version");
                    title = publicationObject.getString("title");
                    subtitle = publicationObject.getString("subtitle");
                    address = publicationObject.getString("address");
                    typeOfCollecting = (short) publicationObject.getInt("type_of_collecting");
                    lat = publicationObject.getDouble("latitude");
                    lng = publicationObject.getDouble("longitude");
                    startingDate = publicationObject.getString("starting_date");
                    endingDate = publicationObject.getString("ending_date");
                    contactInfo = publicationObject.getString("contact_info");
                    isOnAir = publicationObject.getBoolean("is_on_air");
                    photoURL = publicationObject.getString("photo_url");

                    identityProviderUserName = publicationObject.getString("identity_provider_user_name");
                    price = publicationObject.getDouble("price");
                    priceDescription = publicationObject.getString("price_description");
                    Publication publication = new Publication(id, version, title, subtitle, address, typeOfCollecting, lat, lng, startingDate, endingDate, contactInfo, isOnAir,
                            activeDeviceDevUUID, photoURL, publisherID, audience, identityProviderUserName, price, priceDescription);
                    publicationsDBHandler.insertPublication(publication);

                    boolean notifyUser = args[1].equals(String.valueOf(CommonConstants.VALUE_TRUE));
                    if(notifyUser){
                        final String msgTitle = getString(R.string.foodonet);
                        final String msgBody = String.format("%1$s: %2$s",getString(R.string.new_share),publication.getTitle());
                        CommonMethods.sendNotification(this,msgTitle,msgBody);
                    }
                    intent.putExtra(User.IDENTITY_PROVIDER_USER_ID,publisherID);
                    updateData = true;
                }
                intent.putExtra(ReceiverConstants.UPDATE_DATA,updateData);
            }

            else if(actionType == ReceiverConstants.ACTION_GET_REPORTS){
                ArrayList<PublicationReport> reports = new ArrayList<>();
                JSONArray rootGetReports;
                rootGetReports = new JSONArray(responseRoot);

                long reportId,publicationID,reportUserID;
                int publicationVersion,rating;
                String active_device_dev_uuid,dateOfReport,reportUserName,reportContactInfo;
                short reportType;

                for (int i = 0; i < rootGetReports.length(); i++) {
                    JSONObject report = rootGetReports.getJSONObject(i);
                    reportId = report.getLong("id");
                    publicationID = report.getLong("publication_id");
                    publicationVersion = report.getInt("publication_version");
                    reportType = (short) report.getInt("report");
                    active_device_dev_uuid = report.getString("active_device_dev_uuid");
                    dateOfReport = report.getString("date_of_report");
                    reportUserName = "No user name";
                    if (report.getString("report_user_name") != null) {
                        reportUserName = report.getString("report_user_name");
                    }
                    reportContactInfo = report.getString("report_contact_info");
                    reportUserID = report.getLong("reporter_user_id");
                    rating = report.getInt("rating");

                    reports.add(new PublicationReport(reportId, publicationID, publicationVersion, reportType, active_device_dev_uuid,
                            //createdDate, updateDate,
                            dateOfReport, reportUserName, reportContactInfo, reportUserID, rating));
                }
                intent.putParcelableArrayListExtra(PublicationReport.REPORT_KEY, reports);
            }

            else if(actionType == ReceiverConstants.ACTION_ADD_REPORT){
                // TODO: 27/11/2016 add logic
            }

            else if(actionType == ReceiverConstants.ACTION_ADD_USER){
                JSONObject rootAddUser;
                rootAddUser = new JSONObject(responseRoot);
                long userID = rootAddUser.getLong("id");
                CommonMethods.setMyUserID(this, userID);
                Log.d("Add user response", "id: " + userID);
            }

            else if(actionType == ReceiverConstants.ACTION_UPDATE_USER){
                // TODO: 22/02/2017 add logic
            }

            else if(actionType == ReceiverConstants.ACTION_REGISTER_TO_PUBLICATION){
                JSONObject rootRegistered = new JSONObject(responseRoot);
                long publicationID, collectorUserID;
                int publicationVersion;
                String activeDeviceUUID, name, phone;

                publicationID = rootRegistered.getLong("publication_id");
                publicationVersion = rootRegistered.getInt("publication_version");
                collectorUserID = rootRegistered.getLong("collector_user_id");
                activeDeviceUUID = rootRegistered.getString("active_device_dev_uuid");
                name = rootRegistered.getString("collector_name");
                phone = rootRegistered.getString("collector_contact_info");

                RegisteredUser registeredUser = new RegisteredUser(publicationID,(double)-1,activeDeviceUUID,publicationVersion,name,phone,collectorUserID);
                registeredUsersDBHandler = new RegisteredUsersDBHandler(this);
                registeredUsersDBHandler.insertRegisteredUser(registeredUser);
            }

//            else if(actionType == ReceiverConstants.ACTION_GET_PUBLICATION_REGISTERED_USERS){
//                JSONArray registeredUsersArray = new JSONArray(responseRoot);
//                intent.putExtra(Publication.PUBLICATION_COUNT_OF_REGISTER_USERS_KEY,registeredUsersArray.length());
//            }

            else if(actionType == ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS){
                ArrayList<RegisteredUser> registeredUsers = new ArrayList<>();

                publicationsDBHandler = new PublicationsDBHandler(this);
                ArrayList<Long> publicationsIDs = publicationsDBHandler.getPublicationsIDs();

                long publicationID, collectorUserID;
                int publicationVersion;
                String activeDeviceUUID, name, phone;

                JSONArray root = new JSONArray(responseRoot);
                for (int i = 0; i < root.length(); i++) {
                    JSONObject registeredUser = root.getJSONObject(i);
                    publicationID = registeredUser.getLong("publication_id");
                    if(publicationsIDs.contains(publicationID)){
                        publicationVersion = registeredUser.getInt("publication_version");
                        collectorUserID = registeredUser.getLong("collector_user_id");
                        activeDeviceUUID = registeredUser.getString("active_device_dev_uuid");
                        name = registeredUser.getString("collector_name");
                        phone = registeredUser.getString("collector_contact_info");

                        registeredUsers.add(new RegisteredUser(publicationID,(double)-1,activeDeviceUUID,publicationVersion,name,phone,collectorUserID));
                    }
                }
                registeredUsersDBHandler = new RegisteredUsersDBHandler(this);
                registeredUsersDBHandler.replaceAllRegisteredUsers(registeredUsers);
            }

            else if(actionType == ReceiverConstants.ACTION_UNREGISTER_FROM_PUBLICATION){
                registeredUsersDBHandler = new RegisteredUsersDBHandler(this);
                registeredUsersDBHandler.deleteRegisteredUser(Long.parseLong(args[0]));
            }

            else if(actionType == ReceiverConstants.ACTION_ADD_GROUP){
                long groupID,userID;
                String groupName;

                JSONObject groupObject = new JSONObject(responseRoot);
                userID = CommonMethods.getMyUserID(this);
                groupID = groupObject.getLong("id");
                groupName = args[0];
                Group group = new Group(groupName,userID,groupID);

                // add group to db */
                intent.putExtra(Group.GROUP_ID,groupID);
                groupsDBHandler = new GroupsDBHandler(this);
                groupsDBHandler.insertGroup(group);

                // send an admin member to the server */
                Intent addAdminMemberIntent = new Intent(this,GetDataService.class);
                addAdminMemberIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_ADD_ADMIN_MEMBER);
                addAdminMemberIntent.putExtra(ReceiverConstants.GROUP_ID,groupID);
                this.startService(addAdminMemberIntent);
            }

            else if(actionType == ReceiverConstants.ACTION_GET_GROUPS){
                JSONArray groupArray = new JSONArray(responseRoot);

                ArrayList<Group> groups = new ArrayList<>();
                ArrayList<GroupMember> members = new ArrayList<>(), groupMembers = new ArrayList<>();
                ArrayList<Long> groupsID = new ArrayList<>();
                long uniqueID,groupID,memberID,userID,myUserID;
                String groupName,phoneNumber,memberName;
                boolean isAdmin, foundUserInMembers;

                myUserID = CommonMethods.getMyUserID(this);

                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject group = groupArray.getJSONObject(i);
                    groupID = group.getLong(Group.GROUP_ID);
                    JSONArray membersArray = group.getJSONArray(Group.MEMBERS);
                    if(!groupsID.contains(groupID) && membersArray.length()>0){
                        groupsID.add(groupID);
                        userID = group.getLong(Group.USER_ID);
                        groupName = group.getString(Group.GET_GROUP_NAME);
                        foundUserInMembers = false;
                        groupMembers.clear();
                        for (int j = 0; j < membersArray.length(); j++) {
                            JSONObject member = membersArray.getJSONObject(j);
                            uniqueID = member.getLong(GroupMember.UNIQUE_ID);
                            memberID = member.getLong(GroupMember.USER_ID);
                            if(memberID== myUserID){
                                foundUserInMembers = true;
                            }
                            phoneNumber = member.getString(GroupMember.PHONE_NUMBER);
                            memberName = member.getString(GroupMember.NAME);
                            isAdmin = member.getBoolean(GroupMember.IS_ADMIN);
                            groupMembers.add(new GroupMember(uniqueID,groupID,memberID,phoneNumber,memberName,isAdmin));
                        }
                        if(foundUserInMembers){
                            groups.add(new Group(groupName,userID,groupID));
                            members.addAll(groupMembers);
                        }
                    }
                }
                groupsDBHandler = new GroupsDBHandler(this);
                groupsDBHandler.replaceAllGroups(groups);
                groupMembersDBHandler = new GroupMembersDBHandler(this);
                groupMembersDBHandler.replaceAllGroupsMembers(members);

                Intent getDataIntent = new Intent(this,GetDataService.class);
                getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_PUBLICATIONS);
                startService(getDataIntent);
            }

            else if (actionType == ReceiverConstants.ACTION_ADD_GROUP_MEMBER){
                JSONArray root = new JSONArray(responseRoot);
                boolean isMemberAdded = false;
                if (root.length()>0) {
                    JSONObject memberObject = root.getJSONObject(0);
                    long uniqueID = memberObject.getLong(GroupMember.UNIQUE_ID);
                    long userID = memberObject.getLong(GroupMember.USER_ID);
                    long groupID = memberObject.getLong(GroupMember.GROUP_ID);
                    String phone = memberObject.getString(GroupMember.PHONE_NUMBER);
                    String name = memberObject.getString(GroupMember.NAME);
                    boolean isAdmin = memberObject.getBoolean(GroupMember.IS_ADMIN);
                    GroupMember groupMember = new GroupMember(uniqueID,groupID,userID,phone,name,isAdmin);
                    groupMembersDBHandler = new GroupMembersDBHandler(this);
                    isMemberAdded = groupMembersDBHandler.insertMemberToGroup(groupID,groupMember);
                }
                intent.putExtra(ReceiverConstants.MEMBER_ADDED,isMemberAdded);
            }

            else if (actionType == ReceiverConstants.ACTION_DELETE_GROUP_MEMBER){
                long uniqueID = Long.valueOf(args[0]);
                groupMembersDBHandler = new GroupMembersDBHandler(this);
                groupMembersDBHandler.deleteGroupMember(uniqueID);
                boolean userExitedGroup;
                if(args[1].equals("1")){
                    userExitedGroup = true;
                    long groupID = Long.valueOf(args[2]);
                    groupsDBHandler = new GroupsDBHandler(this);
                    groupsDBHandler.deleteGroup(groupID);
                } else{
                    userExitedGroup = false;
                }
                intent.putExtra(ReceiverConstants.USER_EXITED_GROUP,userExitedGroup);
            }

            else if(actionType == ReceiverConstants.ACTION_POST_FEEDBACK){

            }

            else if(actionType == ReceiverConstants.ACTION_ACTIVE_DEVICE_NEW_USER){

            }
        } catch (JSONException e){
            Log.e(TAG,e.getMessage());
        }
        return intent;
    }
}
