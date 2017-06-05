package com.foodonet.foodonet.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.db.GroupMembersDBHandler;
import com.foodonet.foodonet.db.GroupsDBHandler;
import com.foodonet.foodonet.db.LatestPlacesDBHandler;
import com.foodonet.foodonet.db.NotificationsDBHandler;
import com.foodonet.foodonet.db.PublicationsDBHandler;
import com.foodonet.foodonet.db.ReportsDBHandler;
import com.foodonet.foodonet.model.GroupMember;
import com.foodonet.foodonet.model.Publication;
import com.foodonet.foodonet.serverMethods.ServerMethods;
import java.io.File;
import java.util.ArrayList;

public class GetDataService extends IntentService {
    private static final String TAG = "GetDataService";

    public GetDataService() {
        super("GetDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG,"entered "+ intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1 ));

            PublicationsDBHandler publicationsDBHandler;
            GroupsDBHandler groupsDBHandler;
            GroupMembersDBHandler groupMembersDBHandler;
            LatestPlacesDBHandler latestPlacesDBHandler;
            ReportsDBHandler reportsDBHandler;
            NotificationsDBHandler notificationsDBHandler;
            switch (intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)){

                case ReceiverConstants.ACTION_SIGN_OUT:
                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    // remove user phone number and foodonet user ID from sharedPreferences */
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(getString(R.string.key_prefs_user_phone));
                    editor.remove(getString(R.string.key_prefs_user_id));
                    editor.apply();
                    // delete the data from the db that won't be re-downloaded */
                    groupMembersDBHandler = new GroupMembersDBHandler(this);
                    groupMembersDBHandler.deleteAllGroupsMembers();
                    publicationsDBHandler = new PublicationsDBHandler(this);
                    publicationsDBHandler.deleteAllPublications();
                    latestPlacesDBHandler = new LatestPlacesDBHandler(this);
                    latestPlacesDBHandler.deleteAllPlaces();
                    notificationsDBHandler = new NotificationsDBHandler(this);
                    notificationsDBHandler.deleteAllNotification();
                    File directoryUsers = (getExternalFilesDir(CommonConstants.FILE_TYPE_USERS));
                    if(directoryUsers!= null) {
                        File[] userFiles = directoryUsers.listFiles();
                        for(File file : userFiles){
                            if(file.delete()){
                                Log.d(TAG,"file Deleted :" + file.getName());
                            } else {
                                Log.d(TAG,"file could not be Deleted :" + file.getName());
                            }
                        }
                    }
                    // continue to get new data from the server */

                case ReceiverConstants.ACTION_GET_DATA:
                case ReceiverConstants.ACTION_GET_GROUPS:
                    // get groups */
                    long userID = CommonMethods.getMyUserID(this);
                    if (userID != (long)-1) {
                        ServerMethods.getGroups(this,userID);
                        break;
                    } else{
                        groupsDBHandler = new GroupsDBHandler(this);
                        groupsDBHandler.deleteAllGroups();
                    }
                    // if the user is not registered yet, with userID -1, skip getting the groups and get the publications (which will get only the 'audience 0 - public' group) */

                case ReceiverConstants.ACTION_GET_PUBLICATIONS:
                    // clear old non - user publications from db
//                    publicationsDBHandler = new PublicationsDBHandler(this);
//                    publicationsDBHandler.clearOldPublications();
                    // get publications */
                    ServerMethods.getPublications(this);
                    break;

                case ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS:
                    // get registered users */
                    ServerMethods.getAllRegisteredUsers(this);
                    // continue to clean unused images

                case ReceiverConstants.ACTION_CLEAN_IMAGES:
                    publicationsDBHandler = new PublicationsDBHandler(this);
                    notificationsDBHandler = new NotificationsDBHandler(this);
                    File directoryPublications = (getExternalFilesDir(CommonConstants.FILE_TYPE_PUBLICATIONS));
                    if(directoryPublications!= null) {
                        ArrayList<String> fileNames = publicationsDBHandler.getPublicationsImagesFileNames();
                        ArrayList<String> notificationsFileNames = notificationsDBHandler.getNotificationPublicationImagesFileNames();

                        File[] files = directoryPublications.listFiles();
                        for (File file : files) {
                            if (!fileNames.contains(file.getName()) && !notificationsFileNames.contains(file.getName())) {
                                if (file.delete()) {
                                    Log.d(TAG,"file Deleted :" + file.getName());
                                } else {
                                    Log.d(TAG,"file could not be Deleted :" + file.getName());
                                }
                            }
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_ADD_ADMIN_MEMBER:
                    // after a new group, add the user as an admin */
                    long groupID = intent.getLongExtra(ReceiverConstants.GROUP_ID,(long)-1);
                    GroupMember adminMember = new GroupMember((long)-1,groupID,CommonMethods.getMyUserID(this),
                            CommonMethods.getMyUserPhone(this),CommonMethods.getMyUserName(this),true);
                    ServerMethods.addGroupMember(this,adminMember);
                    break;

                case ReceiverConstants.ACTION_REPUBLISH_PUBLICATION:
                    // after adding the republished publication, delete the old one
                    long publicationID = intent.getLongExtra(Publication.PUBLICATION_ID,-1);
                    ServerMethods.deletePublication(this,publicationID);
            }
        }
    }
}
