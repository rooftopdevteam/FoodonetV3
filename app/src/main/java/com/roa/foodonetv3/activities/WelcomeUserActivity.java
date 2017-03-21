package com.roa.foodonetv3.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.serverMethods.ServerMethods;
import com.roa.foodonetv3.services.GetDataService;

import de.hdodenhof.circleimageview.CircleImageView;

public class WelcomeUserActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "WelcomeUserActivity";
    private Button finishRegisterationButton;
    private EditText editUserName;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private EditText userPhoneNumber;
    private String userName = "";
    private CircleImageView circleImageView;
    private SharedPreferences preferences;
    private FoodonetReceiver receiver;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_user);

        setTitle(R.string.foodonet);

        finishRegisterationButton = (Button) findViewById(R.id.buttonFinishRegistration);
        editUserName = (EditText) findViewById(R.id.editUserName);
        userPhoneNumber = (EditText) findViewById(R.id.editUserPhoneNumber);
        circleImageView = (CircleImageView) findViewById(R.id.circleImageUser);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null) {
            //load the photo from fireBase
            Uri userPhotoUrl = mFirebaseUser.getPhotoUrl();
            if (userPhotoUrl != null) {
                Glide.with(this).load(userPhotoUrl).into(circleImageView);
            } else {
                Glide.with(this).load(R.drawable.foodonet_image).into(circleImageView);
            }
            userName = mFirebaseUser.getDisplayName();
            if (userName != null) {
                editUserName.setText(userName);
            } else {
                // TODO: 28/11/2016 add logic
            }
            finishRegisterationButton.setOnClickListener(this);
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
    public void onClick(View v) {
        String phone = userPhoneNumber.getText().toString();
        phone = CommonMethods.getDigitsFromPhone(phone);
        String userName = editUserName.getText().toString();
        if(PhoneNumberUtils.isGlobalPhoneNumber(phone)){
            ServerMethods.addUser(this, phone, userName);
            dialog = new ProgressDialog(WelcomeUserActivity.this);
            dialog.show();
        } else{
            Toast.makeText(WelcomeUserActivity.this, R.string.invalid_phone_number, Toast.LENGTH_SHORT).show();
        }
    }

    private class FoodonetReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)== ReceiverConstants.ACTION_ADD_USER){
                /** receiver from the foodonet server of creating the new user */
                if(dialog!=null){
                    dialog.dismiss();
                }
                if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                    // TODO: 27/11/2016 add logic if fails
                    Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                } else{
                    /** user successfully added, finish the activity*/
                    CommonMethods.getNewData(getBaseContext());
                    Intent startActivityIntent = new Intent(WelcomeUserActivity.this, MainActivity.class);
                    startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startActivityIntent);
                    finish();
                }
            }
        }
    }
}
