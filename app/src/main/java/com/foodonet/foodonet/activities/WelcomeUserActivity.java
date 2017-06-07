package com.foodonet.foodonet.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.serverMethods.ServerMethods;
import org.json.JSONException;
import org.json.JSONObject;
import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeUserActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editUserName;
    private FirebaseUser mFirebaseUser;
    private EditText userPhoneNumber;
    private CircleImageView circleImageView;
    private FoodonetReceiver receiver;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_user);

        setTitle(R.string.app_name);

        findViewById(R.id.buttonFinishRegistration).setOnClickListener(this);
        findViewById(R.id.textWithoutRegistration).setOnClickListener(this);
        editUserName = (EditText) findViewById(R.id.editUserName);
        userPhoneNumber = (EditText) findViewById(R.id.editUserPhoneNumber);
        circleImageView = (CircleImageView) findViewById(R.id.circleImageUser);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null && mFirebaseUser.getPhotoUrl()!= null) {
            //load the photo from file
            ServerMethods.getMyUserImage(this);
            String userName = mFirebaseUser.getDisplayName();
            if(userName!= null){
                int userNameLengthLimit = getResources().getInteger(R.integer.user_name_length_limit);
                userName = userName.replace("\n","");
                if(userName.length()> userNameLengthLimit){
                    userName = userName.substring(0,userNameLengthLimit);
                }
                editUserName.setText(userName);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new FoodonetReceiver();
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        if(dialog!=null){
            dialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        CommonMethods.signOffUser(this);
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.textWithoutRegistration:
                // in case user clicked "continue without registration - sign the user off (from firebase)
                CommonMethods.signOffUser(this);
                finish();
                break;
            case R.id.buttonFinishRegistration:
                String phone = userPhoneNumber.getText().toString();
                phone = CommonMethods.getDigitsFromPhone(phone);
                String userName = editUserName.getText().toString();
                if(userName.equals("")){
                    Toast.makeText(this, R.string.toast_please_enter_your_name, Toast.LENGTH_SHORT).show();
                } else{
                    if(PhoneNumberUtils.isGlobalPhoneNumber(phone)){
                        phone = CommonMethods.getDigitsFromPhone(phone);
                        ServerMethods.addUser(this, phone, userName);
                        registerToPushNotification(this);
                        dialog = new ProgressDialog(this);
                        dialog.show();
                    } else{
                        Toast.makeText(this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }

    /**
     * register the new user to the server's push notification db
     */
    public void registerToPushNotification(Context context){
        JSONObject activeDeviceRoot = new JSONObject();
        JSONObject activeDevice = new JSONObject();
        try {

            String token = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_prefs_notification_token), null);
            activeDevice.put("dev_uuid",CommonMethods.getDeviceUUID(context));
            if (token== null) {
                activeDevice.put("remote_notification_token", JSONObject.NULL);
            }else {
                activeDevice.put("remote_notification_token", token);
            }
            activeDevice.put("is_ios", false);
            LatLng userLatLng = CommonMethods.getLastLocation(this);
            activeDevice.put("last_location_latitude", userLatLng.latitude);
            activeDevice.put("last_location_longitude", userLatLng.longitude);
            activeDeviceRoot.put("active_device",activeDevice);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerMethods.activeDeviceNewUser(this,activeDeviceRoot.toString());
    }

    /** local receiver */
    private class FoodonetReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)){
                case ReceiverConstants.ACTION_ADD_USER:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    if (intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR, false)) {
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else {
                        CommonMethods.getNewData(getBaseContext());
                        Intent startActivityIntent = new Intent(WelcomeUserActivity.this, MainActivity.class);
                        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(startActivityIntent);
                        finish();
                    }
                    break;

                case ReceiverConstants.ACTION_SAVE_USER_IMAGE:
                    Glide.with(context).load(CommonMethods.getMyUserImageFilePath(context)).into(circleImageView);
                    break;
            }
        }
    }
}
