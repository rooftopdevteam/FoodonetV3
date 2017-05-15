package com.roa.foodonetv3.services;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.NotificationsDBHandler;
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.db.RegisteredUsersDBHandler;
import com.roa.foodonetv3.model.NotificationFoodonet;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.serverMethods.ServerMethods;

import org.json.JSONException;
import org.json.JSONObject;

public class FoodonetGcmListenerService extends GcmListenerService {
    private static final String TAG = "GcmListenerService";
    private static final String PUSH_OBJECT_MSG = "message";

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        Log.d(TAG, s+", :"+bundle.getString(PUSH_OBJECT_MSG));
        if(s.startsWith(getString(R.string.push_notification_prefix)) || s.compareTo(getString(R.string.notifications_server_id)) == 0) {
            String msg = bundle.getString(PUSH_OBJECT_MSG);
            JSONObject msgRoot;
            new JSONObject();
            try {
                msgRoot = new JSONObject(msg);
                handleMessage(msgRoot);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(JSONObject msgRoot) throws JSONException {
        boolean sendNotifications = CommonMethods.isNotificationTurnedOn(this);
        NotificationsDBHandler notificationsDBHandler;
        PublicationsDBHandler publicationsDBHandler;
        String type = msgRoot.getString("type");
        switch (type) {
            case CommonConstants.NOTIF_TYPE_NEW_PUBLICATION: {
                getNewLocation(false,GetLocationService.TYPE_GET_FUSED);
                long publicationID = msgRoot.getLong("id");
                ServerMethods.getPublication(this, publicationID);
                break;
            }
            case CommonConstants.NOTIF_TYPE_DELETED_PUBLICATION: {
                publicationsDBHandler = new PublicationsDBHandler(this);
                RegisteredUsersDBHandler registeredUsersDBHandler = new RegisteredUsersDBHandler(this);
                long publicationID = msgRoot.getLong("id");
                String publicationTitle = msgRoot.getString("title");
                boolean isUserRegistered = registeredUsersDBHandler.isUserRegistered(publicationID);
                if (isUserRegistered) {
                    notificationsDBHandler = new NotificationsDBHandler(this);
                    notificationsDBHandler.insertNotification(new NotificationFoodonet(NotificationFoodonet.NOTIFICATION_TYPE_PUBLICATION_DELETED,
                            publicationID, publicationTitle, CommonMethods.getCurrentTimeSeconds()));
                    if (sendNotifications) {
                        CommonMethods.sendNotification(this);
                    }
                }
                publicationsDBHandler.takePublicationOffline(publicationID);
                Intent intent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
                intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE);
                intent.putExtra(ReceiverConstants.SERVICE_ERROR, false);
                intent.putExtra(ReceiverConstants.UPDATE_DATA, true);
                intent.putExtra(Publication.PUBLICATION_ID, publicationID);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                break;
            }
            case CommonConstants.NOTIF_TYPE_REGISTRATION_FOR_PUBLICATION: {
                long publicationID = msgRoot.getLong("id");
                publicationsDBHandler = new PublicationsDBHandler(this);
                boolean isUserAdmin = publicationsDBHandler.isUserAdmin(publicationID);
                if (isUserAdmin) {
                    ServerMethods.getAllRegisteredUsers(this);
                    String publicationTitle = publicationsDBHandler.getPublicationTitle(publicationID);
                    double timeRegistered = msgRoot.getDouble("date");
                    notificationsDBHandler = new NotificationsDBHandler(this);
                    notificationsDBHandler.insertNotification(new NotificationFoodonet(NotificationFoodonet.NOTIFICATION_TYPE_NEW_REGISTERED_USER,
                            publicationID, publicationTitle, timeRegistered));
                    if (sendNotifications) {
                        CommonMethods.sendNotification(this);
                    }
                }
                break;
            }
            case CommonConstants.NOTIF_TYPE_PUBLICATION_REPORT: {
                long publicationID = msgRoot.getLong("publication_id");
                publicationsDBHandler = new PublicationsDBHandler(this);
                boolean isUserAdmin = publicationsDBHandler.isUserAdmin(publicationID);
                if (isUserAdmin) {
                    String publicationTitle = publicationsDBHandler.getPublicationTitle(publicationID);
                    double timeRegistered = msgRoot.getDouble("date_of_report");
                    notificationsDBHandler = new NotificationsDBHandler(this);
                    notificationsDBHandler.insertNotification(new NotificationFoodonet(NotificationFoodonet.NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT,
                            publicationID, publicationTitle, timeRegistered));
                    if (sendNotifications) {
                        CommonMethods.sendNotification(this);
                    }
                }
                Intent intent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
                intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GOT_NEW_REPORT);
                intent.putExtra(ReceiverConstants.SERVICE_ERROR, false);
                intent.putExtra(ReceiverConstants.UPDATE_DATA, true);
                intent.putExtra(Publication.PUBLICATION_ID, publicationID);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                break;
            }
            case CommonConstants.NOTIF_TYPE_GROUP_MEMBERS:
                notificationsDBHandler = new NotificationsDBHandler(this);
                long groupID = msgRoot.getLong("id");
                String title = msgRoot.getString("title");
                notificationsDBHandler.insertNotification(new NotificationFoodonet(NotificationFoodonet.NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP,
                        groupID, title, CommonMethods.getCurrentTimeSeconds()));
                if (sendNotifications) {
                    CommonMethods.sendNotification(this);
                }
                CommonMethods.getNewData(this);
                break;
        }
    }

    /** start getting new location , don't show UI for failure*/
    private void getNewLocation(boolean getNewData, int actionType){
        String locationType = CommonMethods.getAvailableLocationType(this);
        switch (locationType){
            case LocationManager.GPS_PROVIDER:
            case LocationManager.NETWORK_PROVIDER:
                if(CommonMethods.isLocationPermissionGranted(this)){
                    CommonMethods.startGetLocationService(this,getNewData,locationType, actionType);
                    Log.d(TAG,"have permissions");
                } else{
                    Log.d(TAG,"ask permissions");
                }
                break;
            case CommonConstants.LOCATION_TYPE_LOCATION_DISABLED:
                Log.d(TAG,"location disabled");
                break;
        }
    }
}
