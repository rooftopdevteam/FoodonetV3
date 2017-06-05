package com.foodonet.foodonet.serverMethods;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.asyncTasks.UpdatePublicationsTask;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.db.PublicationsDBHandler;
import com.foodonet.foodonet.model.Feedback;
import com.foodonet.foodonet.model.Group;
import com.foodonet.foodonet.model.GroupMember;
import com.foodonet.foodonet.model.Publication;
import com.foodonet.foodonet.model.PublicationReport;
import com.foodonet.foodonet.model.RegisteredUser;
import com.foodonet.foodonet.model.User;
import com.foodonet.foodonet.services.FoodonetService;
import com.foodonet.foodonet.services.GetMyUserImageService;
import java.util.ArrayList;

/**
 * helper class to start server connections
 */
public class ServerMethods {
    private static final String TAG = "ServerMethods";

    /**
     * handles getting publications data from the server, because the server's "get all publications" method only returns online public publications
     * we first get those and update the local db accordingly, simultaneously we start updating the non public publications one by one in an asynctask
     * user created publications can be set to offline or ended, non user created publications can only be online, therefore other states will be locally deleted
     */
    public static void getPublications(Context context) {
        getPublicPublications(context);
        updateNonPublicPublications(context);
    }

    /**
     * handles getting new public publications from the server, after receiving the new data, further queries will be made to update or delete specific publications
     */
    private static void getPublicPublications(Context context){
        Intent getPublicationsIntent = new Intent(context, FoodonetService.class);
        getPublicationsIntent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GET_PUBLICATIONS);
        context.startService(getPublicationsIntent);
    }

    /**
     * handles updating (not getting - only way to get new non public publication is through push notifications) non public publications, checking one by one if there are updates,
     * or if the publication was taken offline or deleted, updates the local db accordingly
     */
    private static void updateNonPublicPublications(Context context){
        PublicationsDBHandler publicationsDBHandler = new PublicationsDBHandler(context);
        ArrayList<Long> publicationsToUpdate = publicationsDBHandler.getNonPublicPublicationsToUpdateIDs();
        UpdatePublicationsTask updatePublicationsTask = new UpdatePublicationsTask(context,false);
        updatePublicationsTask.execute(publicationsToUpdate);
    }

    /**
     * handles sending new publication the user created to the foodonet server,
     * after successfully adding it, will send the image to the s3 server as well
     * @param publication new Publication to add
     */
    public static void addPublication(Context context, Publication publication){
        sendPublication(context,publication,ReceiverConstants.ACTION_ADD_PUBLICATION);
    }

    /**
     * handles sending updated data of an existing publication to the foodonet server,
     * after successfully updating it, will send the image of the updated publication to the s3 server as well
     * @param publication the updated Publication to update
     */
    public static void editPublication(Context context, Publication publication){
        sendPublication(context,publication,ReceiverConstants.ACTION_EDIT_PUBLICATION);
    }

    /**
     * handles sending an updated (offline) publication data to the foodonet server,
     * after successfully updating it, will send the image of the last version one to the s3 server as well
     * (notification activity relies on images with versions, even offline ones)
     * the publication will remain in the local db
     * @param publication the publication to take offline
     */
    public static void takePublicationOffline(Context context, Publication publication){
        sendPublication(context,publication,ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE);
    }

    /**
     * handles sending a new publication with the same data (excluding starting and ending time) of the old offline one to be republished
     * (note that ios version doesn't send a new publication, but rather updates the offline one to be online again,
     * the problem lies in the fact that there are already registered users and reports for the old one, therefor it was decided that it will be a new publication)
     * the old publication will be deleted after successfully uploading the new one, and publication image will be uploaded to the s3 server
     * @param publication the new publication to be published
     */
    public static void republishPublication(Context context, Publication publication) {
        sendPublication(context,publication,ReceiverConstants.ACTION_REPUBLISH_PUBLICATION);
    }

    /**
     * handles sending a publication data to the server
     * @param publication the Publication data to be sent
     * @param actionType the type of method to do:
     *                   ReceiverConstants.ACTION_ADD_PUBLICATION
     *                   ReceiverConstants.ACTION_EDIT_PUBLICATION
     *                   ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE
     *                   ReceiverConstants.ACTION_REPUBLISH_PUBLICATION
     */
    private static void sendPublication(Context context, Publication publication, int actionType){
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, actionType);
        String jsonToSend = publication.getPublicationJson().toString();
        intent.putExtra(ReceiverConstants.JSON_TO_SEND, jsonToSend);
        // data - (ArrayList<Parcelable>) with Publication
        ArrayList<Parcelable> data = new ArrayList<>();
        data.add(publication);
        intent.putExtra(ReceiverConstants.DATA,data);
        switch (actionType){
            case ReceiverConstants.ACTION_EDIT_PUBLICATION:
            case ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE:
            case ReceiverConstants.ACTION_REPUBLISH_PUBLICATION:
                // add args[0] = (String) publication id
                String[] args = {String.valueOf(publication.getId())};
                intent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
                break;
        }
        context.startService(intent);
    }

    /**
     * handles deleting a publication from the foodonet server and if successful, deletes it from the local db as well
     * @param publicationID id of Publication to delete
     */
    public static void deletePublication(Context context, long publicationID){
        Intent deleteIntent = new Intent(context,FoodonetService.class);
        deleteIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_DELETE_PUBLICATION);
        // args[0] = (String) publication id
        String[] args = {String.valueOf(publicationID)};
        deleteIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(deleteIntent);
    }

    /**
     * handles getting a specific new publication - after getting the message to do so from the GCM push notification, from the foodonet server,
     * currently receiving all groups so needs to filter to be only new public publication or new publication of a group the user is part of
     * @param publicationID ID of the publication to query from the foodonet server
     */
    public static void getNewPublication(Context context, long publicationID){
        Intent getPublicationIntent = new Intent(context, FoodonetService.class);
        getPublicationIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_NEW_PUBLICATION);
        // args[0] = (String) publication id
        String[] args = {String.valueOf(publicationID)};
        getPublicationIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(getPublicationIntent);
    }

    /**
     * handles getting reports for a specific publication from the foodonet server
     * @param publicationID ID of the queried publication
     * @param publicationVersion version of the queried publication
     */
    public static void getPublicationReports(Context context, long publicationID, int publicationVersion){
        Intent intent = new Intent(context,FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GET_REPORTS);
        // args[0] = (String) publication id
        String[] args = {String.valueOf(publicationID),String.valueOf(publicationVersion)};
        intent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(intent);
    }

    /**
     * handles sending a new user created report of a publication to the foodonet server.
     * the user should only be able to send one report per publication, so limit the user before getting the data whether he sent one or not
     * @param publicationReport the PublicationReport to send
     */
    public static void addReport(Context context, PublicationReport publicationReport){
        String reportJson = publicationReport.getAddReportJson().toString();
        Intent i = new Intent(context,FoodonetService.class);
        i.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_ADD_REPORT);
        // args[0] = (String) publication id
        String[] reportArgs = {String.valueOf(publicationReport.getPublicationID())};
        i.putExtra(ReceiverConstants.ADDRESS_ARGS,reportArgs);
        i.putExtra(ReceiverConstants.JSON_TO_SEND,reportJson);
        context.startService(i);
    }

    /**
     * handles setting the phone and user name in shared prefs and sending the user data after the user has signed in to the app to the foodonet server,
     * if successful, will get a user ID from the server and set it to shared prefs
     * @param phoneNumber String of the user's phone number, should be formatted without non digits, and with a local code (ie 0500000000)
     * @param userName String of the user's name
     */
    public static void addUser(Context context, String phoneNumber, String userName){
        sendUser(context,phoneNumber,userName,ReceiverConstants.ACTION_ADD_USER);
    }

    /**
     * handles setting the phone and user name in shared prefs and sending updated data of the user to the foodonet server
     * @param phoneNumber String of the user's phone number, should be formatted without non digits, and with a local code (ie 0500000000)
     * @param userName String of the user's name
     */
    public static void updateUser(Context context, String phoneNumber, String userName){
        sendUser(context,phoneNumber,userName,ReceiverConstants.ACTION_UPDATE_USER);
    }

    /**
     * handles sending the data of the user to the foodonet server
     * @param phoneNumber String of the user's phone number, should be formatted without non digits, and with a local code (ie 0500000000)
     * @param userName String of the user's name
     * @param actionType type of action, handles:
     *                   ReceiverConstants.ACTION_ADD_USER
     *                   ReceiverConstants.ACTION_UPDATE_USER
     */
    private static void sendUser(Context context, String phoneNumber, String userName, int actionType){
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // save user phone number and user name to sharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(context.getString(R.string.key_prefs_user_phone), phoneNumber)
                .putString(context.getString(R.string.key_prefs_user_name),userName)
                .apply();

        String uuid = CommonMethods.getDeviceUUID(context);
        String providerId = "";
        String userEmail = mFirebaseUser.getEmail();
        for (UserInfo userInfo : mFirebaseUser.getProviderData()) {
            String tempProviderId = userInfo.getProviderId();
            if(tempProviderId.equals("google.com")){
                providerId = "google";
            }
            if (tempProviderId.equals("facebook.com")) {
                providerId = "facebook";
            }
        }
        User user = new User(providerId,mFirebaseUser.getUid(),"token1",phoneNumber,userEmail,userName,true,uuid);

        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, actionType);
        if(actionType == ReceiverConstants.ACTION_UPDATE_USER){
            String[] args = {String.valueOf(CommonMethods.getMyUserID(context))};
            intent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        }
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,user.getUserJson().toString());
        context.startService(intent);

        String message = "user: "+user.getUserJson().toString();
        Log.i(TAG,message);
    }

    /**
     * handles a user registering to a publication - and sending the data to the foodonet server,
     * a user should be able to register just once, so make sure to disable the button before checking
     * if the user is already registered and also after clicking and waiting for the server response
     * @param registeredUser RegisteredUser to send
     */
    public static void registerToPublication(Context context, RegisteredUser registeredUser){
        String registration = registeredUser.getJsonForRegistration().toString();
        Intent intent = new Intent(context,FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_REGISTER_TO_PUBLICATION);
        // args[0] = (String) publication id
        String[] args = {String.valueOf(registeredUser.getPublicationID())};
        intent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,registration);
        context.startService(intent);
    }

    /**
     * handles un-registering the user from a publication - sending the data to the foodonet server
     * @param publicationID ID of the publication to unregister from
     * @param publicationVersion version of the publication to unregister from
     */
    public static void unregisterFromPublication(Context context, long publicationID, int publicationVersion){
        Intent unregisterIntent = new Intent(context,FoodonetService.class);
        unregisterIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_UNREGISTER_FROM_PUBLICATION);
        // args[0] (string) publication id, args[1] (String) publication version, args[2] (String) user UUID
        String[] args = {String.valueOf(publicationID),String.valueOf(publicationVersion),
                String.valueOf(CommonMethods.getDeviceUUID(context))};
        unregisterIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(unregisterIntent);
    }

    /**
     * handles receiving a huge list of all registered users, including past ones, and filtering the count of registered users for publications in the local db,
     * as the options for querying this data was either getting the data for each publication individually or getting through this method,
     * we decided to use this one for now, should be noted that this is a BAD practice to user
     */
    public static void getAllRegisteredUsers(Context context){
        Intent getDataIntent = new Intent(context,FoodonetService.class);
        getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS);
        context.startService(getDataIntent);
    }

    /**
     * handles sending a new group to the foodonet server
     * @param newGroup Group to add
     */
    public static void addGroup(Context context, Group newGroup){
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_ADD_GROUP);
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,newGroup.getAddGroupJson().toString());
        // args[0] = (String) group name
        String[] args = {newGroup.getGroupName()};
        intent.putExtra(ReceiverConstants.ADDRESS_ARGS, args);
        context.startService(intent);
    }

    /**
     * handles getting all groups in which the user is a part of
     * (note that in the root of the json response there is always the user id of the group's creator,
     * which may have left the group, therefor we only check in the array of the group members and never the root)
     * @param userID ID of the user
     */
    public static void getGroups(Context context, long userID){
        Intent getDataIntent = new Intent(context,FoodonetService.class);
        getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GET_GROUPS);
        // args[0] = (String) user id
        String[] args = new String[]{String.valueOf(userID)};
        getDataIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(getDataIntent);
    }

    /**
     * handles adding a new group member to the group by sending the data to the foodonet server.
     * note that the server checks if a user is an existing foodonet user or not by checking the phone number
     * @param groupMember the new GroupMember to add to the group
     */
    public static void addGroupMember(Context context, GroupMember groupMember){
        Intent addMemberIntent = new Intent(context, FoodonetService.class);
        addMemberIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_ADD_GROUP_MEMBER);
        ArrayList<GroupMember> members = new ArrayList<>();
        members.add(groupMember);
        addMemberIntent.putExtra(ReceiverConstants.JSON_TO_SEND,Group.getAddGroupMembersJson(members).toString());
        String[] args = {String.valueOf(groupMember.getGroupID())};
        addMemberIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        // data - (ArrayList<Parcelable>) with GroupMember
        ArrayList<GroupMember> data = new ArrayList<>();
        data.add(groupMember);
        addMemberIntent.putExtra(ReceiverConstants.DATA,data);
        context.startService(addMemberIntent);
    }

    /**
     * handles a user's deletion of either himself (admin and non admin), or others (admin only) from a group
     * @param uniqueID the unique group member id of the user in the group
     * @param isUserExitingGroup true if the user is the one exiting the group, false if an admin is removing another user
     * @param groupID ID of the group
     */
    public static void deleteGroupMember(Context context, long uniqueID, boolean isUserExitingGroup, long groupID){
        Intent deleteMemberIntent = new Intent(context,FoodonetService.class);
        deleteMemberIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_DELETE_GROUP_MEMBER);
        String isUserExitingGroupString;
        if(isUserExitingGroup){
            isUserExitingGroupString = CommonConstants.VALUE_TRUE_STRING;
        } else{
            isUserExitingGroupString = CommonConstants.VALUE_FALSE_STRING;
        }
        // args[0] = (String) uniqueID, args[1] - (String) "1" or "0" - isUserExitingGroup, args[2] - (String) groupID
        String[] args = {String.valueOf(uniqueID),isUserExitingGroupString,String.valueOf(groupID)};
        deleteMemberIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(deleteMemberIntent);
    }

    /**
     * handles sending a feedback post to the foodonet server
     * @param message String of message to send
     */
    public static void postFeedback(Context context, String message){
        Feedback feedback = new Feedback(CommonMethods.getDeviceUUID(context), CommonMethods.getMyUserName(context),message);
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_POST_FEEDBACK);
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,feedback.getFeedbackJson().toString());
        context.startService(intent);
    }

    /**
     * handles sending a new device for push notifications registering in the foodonet server
     * @param jsonString String of the json to send
     */
    public static void activeDeviceNewUser(Context context, String jsonString){
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_ACTIVE_DEVICE_NEW_USER);
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,jsonString);
        context.startService(intent);
    }

    /**
     * @deprecated currently trying to update returns a 404, disabling for now
     * handles sending updated location of the user to the server
     * @param stringToSend String of the json to send
     */
    public static void activeDeviceUpdateLocation(Context context, String stringToSend){
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_ACTIVE_DEVICE_UPDATE_USER_LOCATION);
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,stringToSend);
        context.startService(intent);
    }

    /**
     * handles getting the group's admin image. GCM push notification of type update group members, notifications will be made when data is received
     * @param groupID ID of the group
     * @param groupName String of the name of the group
     */
    public static void getGroupAdminImage(Context context, long groupID, String groupName) {
        String[] args = {String.valueOf(groupID),groupName};
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_GROUP_ADMIN_IMAGE);
        // args[0] - (String) group id, args[1] - String group name
        intent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(intent);
    }

    /**
     * handles getting the user image from the firebase photo url through the GetMyUserImageService service and broadcasts when finished
     */
    public static void getMyUserImage(Context context){
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null && mFirebaseUser.getPhotoUrl()!= null) {
            String userImageUrl = mFirebaseUser.getPhotoUrl().toString();
            Intent getUserImageIntent = new Intent(context, GetMyUserImageService.class);
            getUserImageIntent.putExtra(GetMyUserImageService.IMAGE_URL,userImageUrl);
            context.startService(getUserImageIntent);
        }
    }
}