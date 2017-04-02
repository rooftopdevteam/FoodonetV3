package com.roa.foodonetv3.activities;

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
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.OnReplaceFragListener;
import com.roa.foodonetv3.fragments.ActiveFragment;
import com.roa.foodonetv3.fragments.ClosestFragment;
import com.roa.foodonetv3.fragments.RecentFragment;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.serverMethods.ServerMethods;
import org.json.JSONException;
import org.json.JSONObject;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,TabLayout.OnTabSelectedListener,
        GoogleApiClient.OnConnectionFailedListener, OnReplaceFragListener {
    private static final String TAG = "MainActivity";


    private ViewPager viewPager;
    private SharedPreferences preferenceManager;
    private Button buttonTest;
    private CircleImageView circleImageView;
    private TextView headerTxt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** toolbar set up */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.foodonet);
        setSupportActionBar(toolbar);

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);

        // TODO: 16/01/2017 remove this after finished testing the push notification user sign in
        buttonTest = (Button) findViewById(R.id.buttonTest);
        // disabling the button for now
//        buttonTest.setVisibility(View.GONE);
        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerToPushNotification(MainActivity.this);
            }
        });

        /** set the google api ? */
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        // TODO: 01/01/2017 remove the notification token generator to initializes place
        /** generate notification token to register the device to get notification*/
        String token = preferenceManager.getString(getString(R.string.key_prefs_notification_token),null);
        if (token == null) {
            generateNotificationToken();
        }

        /** set the drawer */
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        circleImageView = (CircleImageView) hView.findViewById(R.id.headerCircleImage);
        headerTxt = (TextView) hView.findViewById(R.id.headerNavTxt);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        /** set the view pager */
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ViewHolderAdapter adapter = new ViewHolderAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        tabs.setOnTabSelectedListener(this);
        tabs.setupWithViewPager(viewPager);


        /** set the floating action button, since it only serves one fragment, no need to animate or change the view */
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

    @Override
    protected void onResume() {
        super.onResume();
        /** set drawer header and image */
        // TODO: 19/02/2017 currently loading the image from the web
        FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mFirebaseUser != null && mFirebaseUser.getPhotoUrl() != null) {
            Glide.with(this).load(mFirebaseUser.getPhotoUrl()).into(circleImageView);
            headerTxt.setText(CommonMethods.getMyUserName(this));
        } else {
            Glide.with(this).load(android.R.drawable.sym_def_app_icon).into(circleImageView);
            headerTxt.setText(getResources().getString(R.string.not_signed_in));
        }
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

    /** handle the navigation actions in the common methods class */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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

    @Override
    public void onReplaceFrags(String openFragType, long id) {
        Intent i = new Intent(this, PublicationActivity.class);
        i.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION, openFragType);
        i.putExtra(Publication.PUBLICATION_KEY,id);
        this.startActivity(i);
    }

    //view pager adapter...
    public class ViewHolderAdapter extends FragmentPagerAdapter {

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
                    return getString(R.string.view_pager_tab_active);
                case 1:
                    return getString(R.string.view_pager_tab_recent);
                case 2:
                    return getString(R.string.view_pager_tab_closest);
            }

            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    // TODO: 15/01/2017 THIS IS A TEST
    /** test - sign to notifications */
    public void registerToPushNotification(Context context){
        JSONObject activeDeviceRoot = new JSONObject();
        JSONObject activeDevice = new JSONObject();
        try {
            String token = preferenceManager.getString(getString(R.string.key_prefs_notification_token), null);
            activeDevice.put("dev_uuid",CommonMethods.getDeviceUUID(context));
            if (token== null) {
                activeDevice.put("remote_notification_token", activeDevice.NULL);
            }else {
                activeDevice.put("remote_notification_token", token);
            }
            activeDevice.put("is_ios", false);
            activeDevice.put("last_location_latitude", preferenceManager.getString(getString(R.string.key_prefs_user_lat), String.valueOf(CommonConstants.LATLNG_ERROR)));
            activeDevice.put("last_location_longitude", preferenceManager.getString(getString(R.string.key_prefs_user_lng),String.valueOf(CommonConstants.LATLNG_ERROR)));
            activeDeviceRoot.put("active_device",activeDevice);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ServerMethods.activeDeviceNewUser(this,activeDeviceRoot.toString());
    }

    // TODO: 15/01/2017 TEST
    public void generateNotificationToken(){
        Thread t = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    String token = InstanceID.getInstance(MainActivity.this).getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                    SharedPreferences.Editor editor = preferenceManager.edit();
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
}