package com.roa.foodonetv3.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
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
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.OnGotMyUserImageListener;
import com.roa.foodonetv3.commonMethods.OnReplaceFragListener;
import com.roa.foodonetv3.fragments.LatestFragment;
import com.roa.foodonetv3.fragments.ClosestFragment;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.services.GetLocationService;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,TabLayout.OnTabSelectedListener,
        OnReplaceFragListener, OnGotMyUserImageListener, View.OnClickListener {
    private static final String TAG = "MainActivity";


    private ViewPager viewPager;
    private CircleImageView circleImageView;
    private TextView headerTxt;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar set up */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        // set the drawer */
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

        // set the view pager */
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this,R.color.colorAccent));
        tabLayout.setSelectedTabIndicatorHeight((int) (5 * getResources().getDisplayMetrics().density));
        tabLayout.setTabTextColors(ContextCompat.getColor(this,R.color.fooGrey),ContextCompat.getColor(this,R.color.fooWhite));

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ViewHolderAdapter adapter = new ViewHolderAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

        tabLayout.setupWithViewPager(viewPager);

        // set the floating action button, since it only serves one fragment, no need to animate or change the view */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        //            @Override
//            public void onClick(View view) {
//                /** pressed on create new publication */
//                Intent i;
//                if(FirebaseAuth.getInstance().getCurrentUser()==null){
//                    /** no user logged in yet, open the sign in activity */
//                    i = new Intent(MainActivity.this,SignInActivity.class);
//                } else{
//                    /** a user is logged in, continue to open the activity and fragment of the add publication */
//                    i = new Intent(MainActivity.this,PublicationActivity.class);
//                    i.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION, PublicationActivity.ADD_PUBLICATION_TAG);
//                }
//                startActivity(i);
//            }
//        });

        // trying to update the location returns a 404, disabling for now
        // CommonMethods.updateUserLocationToServer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // set drawer header and image */
        if (CommonMethods.isMyUserInitialized(this)) {
            Glide.with(this).load(CommonMethods.getMyUserImageFilePath(this)).into(circleImageView);
            headerTxt.setText(CommonMethods.getMyUserName(this));
        } else {
            Glide.with(this).load(android.R.drawable.sym_def_app_icon).into(circleImageView);
            headerTxt.setText(getResources().getString(R.string.not_signed_in));
        }

        tabLayout.addOnTabSelectedListener(this);

        // check if the data last checked was more than the time specified, start getting new data if it did
        if (!CommonMethods.isDataUpToDate(this)){
            getNewLocation(true,GetLocationService.TYPE_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        tabLayout.removeOnTabSelectedListener(this);
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
        if(item.getItemId()!=R.id.nav_all_events){
            CommonMethods.navigationItemSelectedAction(this,item.getItemId());
        }
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
    public void onClick(View v) {
        // fab click
        Intent i;
        if(CommonMethods.getMyUserID(this)==-1){
            i = new Intent(this,SignInActivity.class);
        } else{
            /** a user is logged in, continue to open the activity and fragment of the add publication */
            i = new Intent(MainActivity.this,PublicationActivity.class);
            i.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION, PublicationActivity.ADD_PUBLICATION_TAG);
        }
        startActivity(i);
    }

    @Override
    public void onReplaceFrags(String openFragType, long id) {
        Intent i = new Intent(this, PublicationActivity.class);
        i.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION, openFragType);
        i.putExtra(Publication.PUBLICATION_KEY,id);
        this.startActivity(i);
    }

    @Override
    public void gotMyUserImage() {
        Glide.with(this).load(CommonMethods.getMyUserImageFilePath(this)).into(circleImageView);
        headerTxt.setText(CommonMethods.getMyUserName(this));
    }

    private void getNewLocation(boolean getNewData, int actionType){
        String locationType = CommonMethods.getAvailableLocationType(this);
        switch (locationType){
            case LocationManager.GPS_PROVIDER:
            case LocationManager.NETWORK_PROVIDER:
                if(CommonMethods.isLocationPermissionGranted(this)){
                    CommonMethods.startGetLocationService(this,getNewData, locationType, actionType);
//                    hText(this, "have permissions", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"have permissions");
                } else{
//                    Toast.makeText(this, "ask permissions", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"ask permissions");
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},CommonConstants.PERMISSION_REQUEST_LOCATION);
                }
                break;
            case CommonConstants.LOCATION_TYPE_LOCATION_DISABLED:
                // TODO: 13/05/2017 add user message
                Toast.makeText(this, "location disabled", Toast.LENGTH_SHORT).show();
                Log.d(TAG,"location disabled");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CommonConstants.PERMISSION_REQUEST_LOCATION:
                // in case of a request */
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // if granted, start getNewLocation service again
                    getNewLocation(true,GetLocationService.TYPE_NORMAL);
                } else {
                    // request denied, give the user a message */
                    CommonMethods.setLastUpdated(this);
//                    Toast.makeText(this, getResources().getString(R.string.toast_needs_location_permission), Toast.LENGTH_SHORT).show();
                    Log.d(TAG,getResources().getString(R.string.toast_needs_location_permission));
                }
                break;
        }
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
                    return new ClosestFragment();
                case 1:
                    return new LatestFragment();
            }
            return null;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.view_pager_tab_closest);
                case 1:
                    return getString(R.string.view_pager_tab_latest);
            }

            return null;
        }
        @Override
        public int getCount() {
            return 2;
        }
    }
}