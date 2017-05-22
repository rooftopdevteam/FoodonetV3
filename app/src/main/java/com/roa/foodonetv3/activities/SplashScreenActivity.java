package com.roa.foodonetv3.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.services.GetLocationService;

import java.util.UUID;

public class SplashScreenActivity extends AppCompatActivity  {
    private static final String TAG = "SplashScreenActivity";

    private SharedPreferences sharedPreferences;
    private static final long TIME_TO_SHOW_SPLASH_MILLIS = 1000 + CommonConstants.TIME_SWITCH_TO_FUSED_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        setTitle(R.string.foodonet);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPreferences.getBoolean(getString(R.string.key_prefs_initialized), false)) {
            init();
        }
        getNewLocation(true,GetLocationService.TYPE_NORMAL);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }, TIME_TO_SHOW_SPLASH_MILLIS);
        if (PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_prefs_notification_token), null) == null) {
            generateNotificationToken();
        }
    }

    private void init() {
        // in first use, get a new UUID for the device and save it in the shared preferences */
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(getString(R.string.key_prefs_initialized), true);
        String deviceUUID = UUID.randomUUID().toString();
        edit.putString(getString(R.string.key_prefs_device_uuid), deviceUUID).apply();
        Log.d("Got new device UUID", deviceUUID);
    }

    public void generateNotificationToken() {
        Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    String token = InstanceID.getInstance(SplashScreenActivity.this).getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SplashScreenActivity.this).edit();
                    editor.putString(getString(R.string.key_prefs_notification_token), token);
                    editor.apply();

                    Log.i(TAG, "GCM Registration Token: " + token);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to complete token refresh " + e.getMessage(), e);
                }
            }
        };
        t.start();
    }

    /** start getting new location and data , don't show UI for failure*/
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
