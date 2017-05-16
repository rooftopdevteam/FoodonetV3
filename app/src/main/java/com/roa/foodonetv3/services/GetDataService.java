package com.roa.foodonetv3.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.GroupMembersDBHandler;
import com.roa.foodonetv3.db.LatestPlacesDBHandler;
import com.roa.foodonetv3.db.NotificationsDBHandler;
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.db.ReportsDBHandler;
import com.roa.foodonetv3.model.GroupMember;
import com.roa.foodonetv3.serverMethods.ServerMethods;
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
            switch (intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)){
                case ReceiverConstants.ACTION_SIGN_OUT:
                    // TODO: 05/12/2016 check if it is written as it should...
                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    // remove user phone number and foodonet user ID from sharedPreferences */
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(getString(R.string.key_prefs_user_phone));
                    editor.remove(getString(R.string.key_prefs_user_id));
                    editor.apply();
                    // delete the data from the db that won't be re-downloaded */
                    GroupMembersDBHandler groupMembersDBHandler = new GroupMembersDBHandler(this);
                    groupMembersDBHandler.deleteAllGroupsMembers();
                    LatestPlacesDBHandler latestPlacesDBHandler = new LatestPlacesDBHandler(this);
                    latestPlacesDBHandler.deleteAllPlaces();
                    ReportsDBHandler reportsDBHandler = new ReportsDBHandler(this);
                    reportsDBHandler.deleteAllReports();
                    NotificationsDBHandler notificationsDBHandler = new NotificationsDBHandler(this);
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
                    }
                    // if the user is not registered yet, with userID -1, skip getting the groups and get the publications (which will get only the 'audience 0 - public' group) */

                case ReceiverConstants.ACTION_GET_PUBLICATIONS:
                    // clear old non - user publications from db
                    publicationsDBHandler = new PublicationsDBHandler(this);
                    publicationsDBHandler.clearOldPublications();
                    // get publications */
                    ServerMethods.getPublications(this);
                    break;

                case ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS:
                    // get registered users */
                    ServerMethods.getAllRegisteredUsers(this);
                    // continue to clean unused images

                case ReceiverConstants.ACTION_CLEAN_IMAGES:
                    publicationsDBHandler = new PublicationsDBHandler(this);
                    File directoryPublications = (getExternalFilesDir(CommonConstants.FILE_TYPE_PUBLICATIONS));
                    if(directoryPublications!= null) {
                        ArrayList<String> fileNames = publicationsDBHandler.getPublicationImagesFileNames();

                        File[] files = directoryPublications.listFiles();
                        for (File file : files) {
                            if (!fileNames.contains(file.getName())) {
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
            }
        }
    }
}
