package com.roa.foodonetv3.services;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.roa.foodonetv3.R;

import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.data;

/**
 * Created by felixshor on 3/8/17.
 */

public class FoodonetGcmListenerService extends GcmListenerService {
    public static final String PUSH_OBJECT_MSG = "message";
    public static final String PUBLICATION_NUMBER = "pubnumber";
    @Override
    public void onMessageReceived(String s, Bundle bundle) {

        if(s.startsWith(getString(R.string.push_notification_prefix)) || s.compareTo(getString(R.string.notifications_server_id)) == 0) {
            String msg = bundle.getString(PUSH_OBJECT_MSG);
            JSONObject jo = new JSONObject();
            try {
                jo = new JSONObject(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendNotification(s,"New public");
            /*//TODO chek from old app the parms
            getContentResolver().insert(FooDoNetSQLProvider.URI_NOTIFICATIONS, notification.GetContentValuesRow());
            String basePath = getString(R.string.server_base_url);
            String subPath = getString(R.string.server_edit_publication_path).replace("{0}", String.valueOf(notification.get_publication_or_group_id()));
            HttpServerConnectorAsync connectorAsync = new HttpServerConnectorAsync(basePath, (IFooDoNetServerCallback) this);
            connectorAsync.execute(new InternalRequest(InternalRequest.ACTION_PUSH_NEW_PUB, subPath),
                    new InternalRequest(InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION,
                            getResources().getString(R.string.server_get_registered_for_publications)),
                    new InternalRequest(InternalRequest.ACTION_GET_PUBLICATION_REPORTS,
                            getResources().getString(R.string.server_get_publication_report)));

                            */

            /*
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.drawer_notifications)
                            .setContentTitle("New publish")
                            .setContentText("Test");
            */
            // pushNotification = FNotification.ParseSingleNotificationFromJSON(jo);//PushObject.DecodePushObject(data);
            //  HandleMessage(pushNotification);
        }
    }


    private void sendNotification(String title, String body) {
        Context context = getBaseContext();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.drawer_notifications).setContentTitle(title)
                .setContentText(body);
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.string.notifications_server_id, mBuilder.build());
    }
}
