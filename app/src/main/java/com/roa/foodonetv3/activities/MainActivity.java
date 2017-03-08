package com.roa.foodonetv3.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.fragments.ActiveFragment;
import com.roa.foodonetv3.fragments.ClosestFragment;
import com.roa.foodonetv3.fragments.RecentFragment;
import com.roa.foodonetv3.model.User;
import com.roa.foodonetv3.services.FoodonetService;
import com.roa.foodonetv3.services.MyGcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.UUID;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.v4.content.WakefulBroadcastReceiver.startWakefulService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,TabLayout.OnTabSelectedListener, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";

    private ViewPager viewPager;
    private ViewHolderAdapter adapter;
    private TabLayout tabs;
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences preferenceManager;
    private Button buttonTest;
    private GcmBroadcastReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** toolbar set up */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.foodonet);
        setSupportActionBar(toolbar);

        /** check if the app is initialized*/
        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        // TODO: 21/12/2016  get the string from a static field or a resource string
        if(!preferenceManager.getBoolean("initialized",false)){
            init();
        }

        buttonTest = (Button) findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerToPushNotification(MainActivity.this);
            }
        });
        /** set the google api ? */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        // TODO: 01/01/2017 remove the notification token generator to initializes place
        /** generate notification token to register the device to get notification*/
        String token = preferenceManager.getString("notification_token",null);
        Log.e("token: ", token);
        if (token == null) {
            generateNotificationToken();
        }

        /** set the drawer */
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        CircleImageView circleImageView = (CircleImageView) hView.findViewById(R.id.headerCircleImage);
        TextView headerTxt = (TextView) hView.findViewById(R.id.headerNavTxt);
        circleImageView.setImageResource(R.drawable.foodonet_image);
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser == null) {
            // TODO: 24/11/2016 add logic?
        } else {
            String mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                String mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                Glide.with(this).load(mPhotoUrl).into(circleImageView);
                headerTxt.setText(mUsername);
            }
        }

        /** set the view pager */
        tabs = (TabLayout) findViewById(R.id.tabs);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new ViewHolderAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        tabs.setOnTabSelectedListener(this);
        tabs.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        /** set the floating action button, since it only serves one purpose, no need to animate or change the view */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** pressed on create new publication */
                Intent i;
                if(FirebaseAuth.getInstance().getCurrentUser()==null){
                    /** no user logged in yet, open the sign in activity */
                    i = new Intent(MainActivity.this,SignInActivity.class);
                } else{
                    /** a user is logged in, continue to open the activity and fragment of the add publication */
                    i = new Intent(MainActivity.this,PublicationActivity.class);
                    i.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION, PublicationActivity.ADD_PUBLICATION_TAG);
                }
                startActivity(i);
            }
        });
    }


    private void init(){
        /** in first use, get a new UUID for the device and save it in the shared preferences */
        SharedPreferences.Editor edit = preferenceManager.edit();
        // TODO: 21/12/2016 get the string from a static field or a resource string
        edit.putBoolean("initialized",true);
        String deviceUUID = UUID.randomUUID().toString();
        edit.putString(User.ACTIVE_DEVICE_DEV_UUID, deviceUUID).apply();
        Log.v("Got new device UUID",deviceUUID);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.map:
                CommonMethods.navigationItemSelectedAction(this,R.id.nav_map_view);
                return true;
            case R.id.search:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        /** handle the navigation actions in the common methods class */
        CommonMethods.navigationItemSelectedAction(this,item.getItemId());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }
    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }
    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    //view pager adapter...
    public static class ViewHolderAdapter extends FragmentPagerAdapter {

        public ViewHolderAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position){
                case 0:
                    return new ActiveFragment();
                case 1:
                    return new RecentFragment();
                case 2:
                    return new ClosestFragment();
            }
            return null;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return "Active";
                case 1:
                    return "Recent";
                case 2:
                    return "Closest";
            }

            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    /** test - sign to notifications */
    public void registerToPushNotification(Context context){


    JSONObject activeDeviceRoot = new JSONObject();
    JSONObject activeDevice = new JSONObject();
    try {
        String token = preferenceManager.getString("notification_token", null);
        activeDevice.put("dev_uuid",CommonMethods.getDeviceUUID(context));
        if (token== null) {
            activeDevice.put("remote_notification_token", activeDevice.NULL);
        }else {
            activeDevice.put("remote_notification_token", token);
        }
        activeDevice.put("is_ios", false);
        activeDevice.put("last_location_latitude", PreferenceManager.getDefaultSharedPreferences(context).getString(SplashScreenActivity.USER_LATITUDE, null));
        activeDevice.put("last_location_longitude", PreferenceManager.getDefaultSharedPreferences(context).getString(SplashScreenActivity.USER_LONGITUDE,null));
        activeDeviceRoot.put("active_device",activeDevice);
    } catch (JSONException e) {
        e.printStackTrace();
    }
    Intent intent = new Intent(context, FoodonetService.class);
    intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_ACTIVE_DEVICE_NEW_USER);
    intent.putExtra(ReceiverConstants.JSON_TO_SEND,activeDeviceRoot.toString());
    context.startService(intent);
    }

    public void generateNotificationToken(){
        Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    InstanceID instanceID = InstanceID.getInstance(MainActivity.this);
                    String token;

                    token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    SharedPreferences.Editor editor = preferenceManager.edit();
                    editor.putString("notification_token", token);
                    editor.apply();

                    Log.i(TAG, "GCM Registration Token: " + token);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to complete token refresh " + e.getMessage(), e);
                }
            }
        };
        t.start();
    }

    public static class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Explicitly specify that GcmIntentService will handle the intent.
            ComponentName comp = new ComponentName(context.getPackageName(),
                    MyGcmListenerService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
    }
}


