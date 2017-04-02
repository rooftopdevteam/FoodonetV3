package com.roa.foodonetv3.serverMethods;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.GroupMembersDBHandler;
import com.roa.foodonetv3.model.Feedback;
import com.roa.foodonetv3.model.Group;
import com.roa.foodonetv3.model.GroupMember;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.model.PublicationReport;
import com.roa.foodonetv3.model.RegisteredUser;
import com.roa.foodonetv3.model.User;
import com.roa.foodonetv3.services.FoodonetService;

import java.util.ArrayList;

public class ServerMethods {
    private static final String TAG = "ServerMethods";

    public static void getPublications(Context context) {
        Intent getPublicationsIntent = new Intent(context, FoodonetService.class);
        getPublicationsIntent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GET_PUBLICATIONS);
        context.startService(getPublicationsIntent);
    }

    public static void addPublication(Context context, Publication publication){
        sendPublication(context,publication,ReceiverConstants.ACTION_ADD_PUBLICATION);
    }

    public static void editPublication(Context context, Publication publication){
        sendPublication(context,publication,ReceiverConstants.ACTION_EDIT_PUBLICATION);
    }

    private static void sendPublication(Context context, Publication publication, int actionType){
        ArrayList<Parcelable> data = new ArrayList<>();
        data.add(publication);
        Intent i = new Intent(context, FoodonetService.class);
        i.putExtra(ReceiverConstants.ACTION_TYPE, actionType);
        i.putExtra(ReceiverConstants.JSON_TO_SEND, publication.getPublicationJson().toString());
        i.putExtra(ReceiverConstants.DATA,data);
        if(actionType==ReceiverConstants.ACTION_EDIT_PUBLICATION){
            String[] args = {String.valueOf(publication.getId())};
            i.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        }
        context.startService(i);
    }

    public static void deletePublication(Context context, long publicationID){
        String[] args = {String.valueOf(publicationID)};
        Intent deleteIntent = new Intent(context,FoodonetService.class);
        deleteIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_DELETE_PUBLICATION);
        deleteIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(deleteIntent);
    }

    public static void getPublication(Context context, long publicationID, boolean notifyUser){
        String notifyUserString;
        if(notifyUser){
            notifyUserString = String.valueOf(CommonConstants.VALUE_TRUE);
        } else{
            notifyUserString = String.valueOf(CommonConstants.VALUE_FALSE);
        }
        String[] args = {String.valueOf(publicationID),notifyUserString};
        Intent getPublicationIntent = new Intent(context, FoodonetService.class);
        getPublicationIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_PUBLICATION);
        getPublicationIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(getPublicationIntent);
    }

    public static void getReports(Context context, long publicationID, int publicationVersion){
        Intent intent = new Intent(context,FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GET_REPORTS);
        String[] args = {String.valueOf(publicationID),String.valueOf(publicationVersion)};
        intent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(intent);
    }

    public static void addReport(Context context, PublicationReport publicationReport){
        String reportJson = publicationReport.getAddReportJson().toString();
        Log.d(TAG,"report json:"+reportJson);
        Intent i = new Intent(context,FoodonetService.class);
        i.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_ADD_REPORT);
        String[] reportArgs = {String.valueOf(publicationReport.getPublicationID())};
        i.putExtra(ReceiverConstants.ADDRESS_ARGS,reportArgs);
        i.putExtra(ReceiverConstants.JSON_TO_SEND,reportJson);
        context.startService(i);
    }

    public static void addUser(Context context, String phoneNumber, String userName){
        sendUser(context,phoneNumber,userName,ReceiverConstants.ACTION_ADD_USER);
    }

    public static void updateUser(Context context, String phoneNumber, String userName){
        sendUser(context,phoneNumber,userName,ReceiverConstants.ACTION_UPDATE_USER);
    }

    private static void sendUser(Context context, String phoneNumber, String userName, int actionType){
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        /** save user phone number and user name to sharedPreferences */
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(context.getString(R.string.key_prefs_user_phone), phoneNumber)
                .putString(context.getString(R.string.key_prefs_user_name),userName)
                .apply();

        /** sign in the user to foodonet server and get his new (or existing) id and save it to the shared preferences through the service */
        String uuid = CommonMethods.getDeviceUUID(context);
        String providerId = "";
        String userEmail = mFirebaseUser.getEmail();
        for (UserInfo userInfo : mFirebaseUser.getProviderData()) {
            //                        String mail = userInfo.getEmail();
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
        Log.d(TAG,message);
    }

    public static void registerToPublication(Context context, RegisteredUser registeredUser){
        String registration = registeredUser.getJsonForRegistration().toString();
        String[] registrationArgs = {String.valueOf(registeredUser.getPublicationID())};
        Intent i = new Intent(context,FoodonetService.class);
        i.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_REGISTER_TO_PUBLICATION);
        i.putExtra(ReceiverConstants.ADDRESS_ARGS,registrationArgs);
        i.putExtra(ReceiverConstants.JSON_TO_SEND,registration);
        context.startService(i);
    }

    public static void getAllRegisteredUsers(Context context){
        Intent getDataIntent = new Intent(context,FoodonetService.class);
        getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS);
        context.startService(getDataIntent);
    }

    public static void unregisterFromPublication(Context context, long publicationID, int publicationVersion){
        String[] args = {String.valueOf(publicationID),String.valueOf(publicationVersion),
                String.valueOf(CommonMethods.getDeviceUUID(context))};
        Intent unregisterIntent = new Intent(context,FoodonetService.class);
        unregisterIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_UNREGISTER_FROM_PUBLICATION);
        unregisterIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(unregisterIntent);
    }

    public static void addGroup(Context context, Group newGroup){
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_ADD_GROUP);
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,newGroup.getAddGroupJson().toString());
        String[] args = {newGroup.getGroupName()};
        intent.putExtra(ReceiverConstants.ADDRESS_ARGS, args);
        context.startService(intent);
    }

    public static void getGroups(Context context, long userID){
        Intent getDataIntent = new Intent(context,FoodonetService.class);
        getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GET_GROUPS);
        String[] args = new String[]{String.valueOf(userID)};
        getDataIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(getDataIntent);
    }

    public static void addGroupMember(Context context, GroupMember groupMember){
        Intent addMemberIntent = new Intent(context, FoodonetService.class);
        addMemberIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_ADD_GROUP_MEMBER);
        ArrayList<GroupMember> members = new ArrayList<>();
        members.add(groupMember);
        addMemberIntent.putExtra(ReceiverConstants.JSON_TO_SEND,Group.getAddGroupMembersJson(members).toString());
        String[] args = {String.valueOf(groupMember.getGroupID())};
        addMemberIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        ArrayList<GroupMember> memberData = new ArrayList<>();
        memberData.add(groupMember);
        addMemberIntent.putExtra(ReceiverConstants.DATA,memberData);
        context.startService(addMemberIntent);
    }

    public static void deleteGroupMember(Context context, long uniqueID, boolean isUserExitingGroup, long groupID){
        Intent deleteMemberIntent = new Intent(context,FoodonetService.class);
        deleteMemberIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_DELETE_GROUP_MEMBER);
        String isUserExitingGroupString;
        if(isUserExitingGroup){
            isUserExitingGroupString = "1";
        } else{
            isUserExitingGroupString = "0";
        }
        String[] args = {String.valueOf(uniqueID),isUserExitingGroupString,String.valueOf(groupID)};
        deleteMemberIntent.putExtra(ReceiverConstants.ADDRESS_ARGS,args);
        context.startService(deleteMemberIntent);
    }

    public static void postFeedback(Context context, String message){
        Feedback feedback = new Feedback(CommonMethods.getDeviceUUID(context), FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),message);
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_POST_FEEDBACK);
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,feedback.getFeedbackJson().toString());
        context.startService(intent);
    }

    public static void activeDeviceNewUser(Context context, String stringToSend){
        Intent intent = new Intent(context, FoodonetService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_ACTIVE_DEVICE_NEW_USER);
        intent.putExtra(ReceiverConstants.JSON_TO_SEND,stringToSend);
        context.startService(intent);
    }

}
