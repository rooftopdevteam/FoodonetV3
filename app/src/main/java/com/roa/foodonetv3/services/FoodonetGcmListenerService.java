package com.roa.foodonetv3.services;

import android.os.Bundle;

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
        /*
        if(from.startsWith(getString(R.string.push_notification_prefix)) || from.compareTo(getString(R.string.notifications_server_id)) == 0){
            String msg = data.getString(PUSH_OBJECT_MSG);
            JSONObject jo = new JSONObject();
            try {
                jo = new JSONObject(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
           // pushNotification = FNotification.ParseSingleNotificationFromJSON(jo);//PushObject.DecodePushObject(data);
          //  HandleMessage(pushNotification);

    }



}
