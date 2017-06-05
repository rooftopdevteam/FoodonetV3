package com.foodonet.foodonet.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.DrawerNavigation;
import com.foodonet.foodonet.commonMethods.OnGotMyUserImageListener;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnGotMyUserImageListener {
    private FoodonetReceiver receiver;
    private CircleImageView circleImageView;
    private TextView headerTxt;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.notifications));
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        circleImageView = (CircleImageView) hView.findViewById(R.id.headerCircleImage);
        headerTxt = (TextView) hView.findViewById(R.id.headerNavTxt);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        receiver = new FoodonetReceiver();
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);
        if (CommonMethods.isMyUserInitialized(this)) {
            Glide.with(this).load(CommonMethods.getMyUserImageFilePath(this)).into(circleImageView);
            headerTxt.setText(CommonMethods.getMyUserName(this));
        } else {
            Glide.with(this).load(android.R.drawable.sym_def_app_icon).into(circleImageView);
            headerTxt.setText(getResources().getString(R.string.not_signed_in));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawer.removeDrawerListener(toggle);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()!=R.id.nav_notifications){
            DrawerNavigation.navigationItemSelectedAction(this,item.getItemId());
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void gotMyUserImage() {
        Glide.with(this).load(CommonMethods.getMyUserImageFilePath(this)).into(circleImageView);
        headerTxt.setText(CommonMethods.getMyUserName(this));
    }

    /** local receiver */
    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)){
                case ReceiverConstants.ACTION_SAVE_USER_IMAGE:
                    Glide.with(context).load(CommonMethods.getMyUserImageFilePath(context)).into(circleImageView);
                    break;
            }
        }
    }
}
