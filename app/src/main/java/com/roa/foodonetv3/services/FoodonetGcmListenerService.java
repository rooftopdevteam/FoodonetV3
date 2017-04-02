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
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.db.RegisteredUsersDBHandler;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.serverMethods.ServerMethods;

import org.json.JSONException;
import org.json.JSONObject;

public class FoodonetGcmListenerService extends GcmListenerService {
    private static final String TAG = "GcmListenerService";

    public static final String PUSH_OBJECT_MSG = "message";
    public static final String PUBLICATION_NUMBER = "pubnumber";
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
        String type = msgRoot.getString("type");
        if(type.equals("new_publication")){
            long publicationID = msgRoot.getLong("id");
            if(sendNotifications){
                double publicationLat = msgRoot.getDouble("latitude");
                double publicationLng = msgRoot.getDouble("longitude");
                sendNotifications = isEventInNotificationRadius(new LatLng(publicationLat,publicationLng));
            }
            ServerMethods.getPublication(this,publicationID,sendNotifications);

        } else if(type.equals("deleted_publication")) {
            PublicationsDBHandler publicationsDBHandler = new PublicationsDBHandler(this);
            RegisteredUsersDBHandler registeredUsersDBHandler = new RegisteredUsersDBHandler(this);
            long publicationID = msgRoot.getLong("id");
            String publicationTitle = msgRoot.getString("title");
            if (sendNotifications && registeredUsersDBHandler.isUserRegistered(publicationID)) {
                String title = getString(R.string.foodonet);
                String body = String.format("%1$s: %2$s", getString(R.string.deleted_share), publicationTitle);

                CommonMethods.sendNotification(this, title, body);
            }

            publicationsDBHandler.deletePublication(publicationID);
            Intent intent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
            intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_DELETE_PUBLICATION);
            intent.putExtra(ReceiverConstants.SERVICE_ERROR, false);
            intent.putExtra(ReceiverConstants.UPDATE_DATA, true);
            intent.putExtra(Publication.PUBLICATION_ID, publicationID);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } else if(type.equals("registration_for_publication")){
            long publicationID = msgRoot.getLong("id");
            PublicationsDBHandler publicationsDBHandler = new PublicationsDBHandler(this);
            boolean isUserAdmin = publicationsDBHandler.isUserAdmin(publicationID);
            if(isUserAdmin){
                ServerMethods.getAllRegisteredUsers(this);
                if(sendNotifications){
                    String title = getString(R.string.foodonet);
                    String body = String.format("%1$s: %2$s",getString(R.string.a_new_user_registered_to_your_share),
                            publicationsDBHandler.getPublicationTitle(publicationID));
                    CommonMethods.sendNotification(this,title,body);
                }
            }

        } else if(type.equals("publication_report")){
            long publicationID = msgRoot.getLong("publication_id");
            PublicationsDBHandler publicationsDBHandler = new PublicationsDBHandler(this);
            boolean isUserAdmin = publicationsDBHandler.isUserAdmin(publicationID);
            if(isUserAdmin && sendNotifications){
                int reportType = msgRoot.getInt("report");
                String title = getString(R.string.foodonet);
                String body = String.format("%1$s: %2$s, %3$s: %4$s",getString(R.string.your_share),publicationsDBHandler.getPublicationTitle(publicationID),
                        getString(R.string.got_a_new_report),CommonMethods.getReportStringFromType(this,reportType));
                CommonMethods.sendNotification(this,title,body);
            }
            Intent intent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
            intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GOT_NEW_REPORT);
            intent.putExtra(ReceiverConstants.SERVICE_ERROR, false);
            intent.putExtra(ReceiverConstants.UPDATE_DATA, true);
            intent.putExtra(Publication.PUBLICATION_ID, publicationID);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } else if(type.equals("group_members")){
            if(sendNotifications){
                String title = getString(R.string.foodonet);
                String body = getString(R.string.you_were_added_to_a_new_group);
                CommonMethods.sendNotification(this,title,body);
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
