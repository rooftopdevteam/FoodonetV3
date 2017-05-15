package com.roa.foodonetv3.services;

import android.app.IntentService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.NotificationActivity;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.NotificationFoodonet;

public class NotificationsDismissService extends IntentService {
    private static final String TAG = "NotifsDismissService";

    public NotificationsDismissService() {
        super("NotificationsDismissService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Log.d(TAG,"dismiss notifications");
            CommonMethods.updateUnreadNotificationID(this,CommonConstants.NOTIFICATION_ID_CLEAR);
            if (action.equals(CommonConstants.NOTIF_ACTION_OPEN)){
                Intent openNotifActivity = new Intent(this, NotificationActivity.class);
                openNotifActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(openNotifActivity);
            }
        }
    }
}
