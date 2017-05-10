package com.roa.foodonetv3.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.maps.model.LatLng;
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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean sendNotifications = sharedPreferences.getBoolean(getString(R.string.key_prefs_get_notifications),true);
        NotificationsDBHandler notificationsDBHandler;
        PublicationsDBHandler publicationsDBHandler;
        String type = msgRoot.getString("type");
        if(type.equals(CommonConstants.NOTIF_TYPE_NEW_PUBLICATION)){
            long publicationID = msgRoot.getLong("id");
            if(sendNotifications){
                double publicationLat = msgRoot.getDouble("latitude");
                double publicationLng = msgRoot.getDouble("longitude");
                sendNotifications = isEventInNotificationRadius(new LatLng(publicationLat,publicationLng));
            }
            ServerMethods.getPublication(this,publicationID,sendNotifications);

        } else if(type.equals(CommonConstants.NOTIF_TYPE_DELETED_PUBLICATION)) {
            publicationsDBHandler = new PublicationsDBHandler(this);
            RegisteredUsersDBHandler registeredUsersDBHandler = new RegisteredUsersDBHandler(this);
            long publicationID = msgRoot.getLong("id");
            String publicationTitle = msgRoot.getString("title");
            boolean isUserRegistered = registeredUsersDBHandler.isUserRegistered(publicationID);
            if(isUserRegistered){
                notificationsDBHandler = new NotificationsDBHandler(this);
                notificationsDBHandler.insertNotification(new NotificationFoodonet(NotificationFoodonet.NOTIFICATION_TYPE_PUBLICATION_DELETED,
                        publicationID,publicationTitle,CommonMethods.getCurrentTimeSeconds()));
                if(sendNotifications){
                    CommonMethods.sendNotification(this);
                }
            }
            publicationsDBHandler.takePublicationOffline(publicationID);
//            publicationsDBHandler.deletePublication(publicationID);
            Intent intent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
            intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE);
            intent.putExtra(ReceiverConstants.SERVICE_ERROR, false);
            intent.putExtra(ReceiverConstants.UPDATE_DATA, true);
            intent.putExtra(Publication.PUBLICATION_ID, publicationID);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } else if(type.equals(CommonConstants.NOTIF_TYPE_REGISTRATION_FOR_PUBLICATION)){
            long publicationID = msgRoot.getLong("id");
            publicationsDBHandler = new PublicationsDBHandler(this);
            boolean isUserAdmin = publicationsDBHandler.isUserAdmin(publicationID);
            if(isUserAdmin){
                ServerMethods.getAllRegisteredUsers(this);
                String publicationTitle = publicationsDBHandler.getPublicationTitle(publicationID);
                double timeRegistered = msgRoot.getDouble("date");
                notificationsDBHandler = new NotificationsDBHandler(this);
                notificationsDBHandler.insertNotification(new NotificationFoodonet(NotificationFoodonet.NOTIFICATION_TYPE_NEW_REGISTERED_USER,
                        publicationID,publicationTitle,timeRegistered));
                if(sendNotifications){
                    CommonMethods.sendNotification(this);
                }
            }

        } else if(type.equals(CommonConstants.NOTIF_TYPE_PUBLICATION_REPORT)){
            long publicationID = msgRoot.getLong("publication_id");
            publicationsDBHandler = new PublicationsDBHandler(this);
            boolean isUserAdmin = publicationsDBHandler.isUserAdmin(publicationID);
            if(isUserAdmin){
                String publicationTitle = publicationsDBHandler.getPublicationTitle(publicationID);
                double timeRegistered = msgRoot.getDouble("date_of_report");
                notificationsDBHandler = new NotificationsDBHandler(this);
                notificationsDBHandler.insertNotification(new NotificationFoodonet(NotificationFoodonet.NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT,
                        publicationID,publicationTitle,timeRegistered));
                if(sendNotifications){
                    CommonMethods.sendNotification(this);
                }
            }
            Intent intent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
            intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GOT_NEW_REPORT);
            intent.putExtra(ReceiverConstants.SERVICE_ERROR, false);
            intent.putExtra(ReceiverConstants.UPDATE_DATA, true);
            intent.putExtra(Publication.PUBLICATION_ID, publicationID);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } else if(type.equals(CommonConstants.NOTIF_TYPE_GROUP_MEMBERS)){
            notificationsDBHandler = new NotificationsDBHandler(this);
            long groupID = msgRoot.getLong("id");
            String title = msgRoot.getString("title");
            // TODO: 26/04/2017 currently server always returns title as "public"
            notificationsDBHandler.insertNotification(new NotificationFoodonet(NotificationFoodonet.NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP,
                    groupID,title,CommonMethods.getCurrentTimeSeconds()));
            if(sendNotifications){
                CommonMethods.sendNotification(this);
            }
            CommonMethods.getNewData(this);
        }
    }

    private boolean isEventInNotificationRadius(LatLng notificationLatLng){
        LatLng userLocation = CommonMethods.getLastLocation(this);
        String keyListNotificationRadius = getString(R.string.key_prefs_list_notification_radius);
        String[] notificationRadiusListKMValues = getResources().getStringArray(R.array.prefs_notification_radius_values_km);
        String currentValueNotificationRadiusListKM = PreferenceManager.getDefaultSharedPreferences(this).getString(keyListNotificationRadius,
                notificationRadiusListKMValues[CommonConstants.DEFAULT_NOTIFICATION_RADIUS_ITEM]);
        return (currentValueNotificationRadiusListKM.equals("-1") ||
                CommonMethods.distance(userLocation.latitude,userLocation.longitude,notificationLatLng.latitude,notificationLatLng.longitude)
                <= Integer.valueOf(currentValueNotificationRadiusListKM));
    }
}
