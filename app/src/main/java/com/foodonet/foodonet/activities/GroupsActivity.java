package com.foodonet.foodonet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.foodonet.foodonet.commonMethods.DrawerNavigation;
import com.foodonet.foodonet.commonMethods.OnGotMyUserImageListener;
import com.foodonet.foodonet.db.GroupsDBHandler;
import com.foodonet.foodonet.dialogs.NewGroupDialog;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.FabAnimation;
import com.foodonet.foodonet.commonMethods.OnReplaceFragListener;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.fragments.GroupFragment;
import com.foodonet.foodonet.fragments.GroupsOverviewFragment;
import com.foodonet.foodonet.model.Group;
import com.foodonet.foodonet.serverMethods.ServerMethods;
import java.util.EmptyStackException;
import java.util.Stack;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener ,
        OnReplaceFragListener,NewGroupDialog.OnNewGroupClickListener, OnGotMyUserImageListener {
    private static final String TAG = "GroupsActivity";

    public static final String GROUPS_OVERVIEW_TAG = "groupsOverviewFrag";
    public static final String ADMIN_GROUP_TAG = "groupFrag";
    public static final String NON_ADMIN_GROUP_TAG = "nonGroupFrag";
    public static final String BACK_IN_STACK_TAG = "backInStack";

    public static final int CONTACT_PICKER = 1;
    private static final String STATE_GROUP_FRAG_STACK = "stateGroupFragStack";
    private static final String STATE_ADMIN_GROUP_ID = "stateAdminGroupID";
    private static final String STATE_NON_ADMIN_GROUP_ID = "stateNonAdminGroupID";


    private Stack<String> fragStack;
    private NewGroupDialog newGroupDialog;
    private CircleImageView circleImageView;
    private TextView headerTxt;

    private FloatingActionButton fab;
    private FragmentManager fragmentManager;
    private GroupsDBHandler groupsDBHandler;
    private long adminGroupID,nonAdminGroupID;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set the fragment manager
        fragmentManager = getSupportFragmentManager();

        groupsDBHandler = new GroupsDBHandler(this);

        // set the drawer layout
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set the floating action button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        // set header imageView
        View hView = navigationView.getHeaderView(0);
        circleImageView = (CircleImageView) hView.findViewById(R.id.headerCircleImage);
        headerTxt = (TextView) hView.findViewById(R.id.headerNavTxt);


        if(savedInstanceState== null){
            // if new activity, open the overview group fragment
            adminGroupID = -1;
            nonAdminGroupID = -1;
            fragStack = new Stack<>();
            fragStack.push(GROUPS_OVERVIEW_TAG);
            replaceFrags(GROUPS_OVERVIEW_TAG,true,false);
        } else{
            adminGroupID = savedInstanceState.getLong(STATE_ADMIN_GROUP_ID);
            nonAdminGroupID = savedInstanceState.getLong(STATE_NON_ADMIN_GROUP_ID);
            fragStack = (Stack<String>) savedInstanceState.getSerializable(STATE_GROUP_FRAG_STACK);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // set drawer header and image
        if (CommonMethods.isMyUserInitialized(this)) {
            Glide.with(this).load(CommonMethods.getMyUserImageFilePath(this)).into(circleImageView);
            headerTxt.setText(CommonMethods.getMyUserName(this));
        }else{
            Glide.with(this).load(android.R.drawable.sym_def_app_icon).into(circleImageView);
            headerTxt.setText(getResources().getString(R.string.not_signed_in));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // dismiss the dialog if open
        if(newGroupDialog!= null){
            newGroupDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawer.removeDrawerListener(toggle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_GROUP_FRAG_STACK,fragStack);
        outState.putLong(STATE_ADMIN_GROUP_ID,adminGroupID);
        outState.putLong(STATE_NON_ADMIN_GROUP_ID,nonAdminGroupID);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            fragStack.pop();
            if(fragStack.isEmpty()){
                super.onBackPressed();
            } else{
                replaceFrags(fragStack.peek(),false,true);
            }
        }
    }

    /** handle the navigation actions in the common methods class */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.nav_groups){
            if(!fragStack.peek().equals(GROUPS_OVERVIEW_TAG)){
                fragStack = new Stack<>();
                fragStack.push(GROUPS_OVERVIEW_TAG);
                replaceFrags(GROUPS_OVERVIEW_TAG,false,true);
            }
        } else{
            DrawerNavigation.navigationItemSelectedAction(this,item.getItemId());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /** replace the fragment and animate the fab accordingly
     * @param   openFragType    the fragment to open.
     * @param   isAddNewFragment    whether to add fragment or replace.
     * @param   isBackPress     whether the new fragment was resulted from a back press
     * */
    public void replaceFrags(String openFragType, boolean isAddNewFragment, boolean isBackPress) {
        // get the values for the fab animation */
        long duration;
        if(isAddNewFragment){
            // if this is the first frag - don't make a long animation */
            duration = 1;
        } else{
            duration = CommonConstants.FAB_ANIM_DURATION;
        }
        switch (openFragType) {
            case GROUPS_OVERVIEW_TAG:
                GroupsOverviewFragment groupsOverviewFragment = new GroupsOverviewFragment();
                updateContainer(isAddNewFragment,groupsOverviewFragment,GROUPS_OVERVIEW_TAG,isBackPress);
                animateFab(openFragType, true, duration);
                break;
            case ADMIN_GROUP_TAG:
                GroupFragment groupFragment = new GroupFragment();
                Group adminGroup = groupsDBHandler.getGroup(adminGroupID);
                Bundle bundleAdmin = new Bundle();
                bundleAdmin.putParcelable(Group.GROUP,adminGroup);
                groupFragment.setArguments(bundleAdmin);
                updateContainer(isAddNewFragment, groupFragment, ADMIN_GROUP_TAG,isBackPress);
                animateFab(openFragType, true, duration);
                break;
            case NON_ADMIN_GROUP_TAG:
                GroupFragment groupFragment2 = new GroupFragment();
                Group nonAdminGroup = groupsDBHandler.getGroup(nonAdminGroupID);
                Bundle bundleNonAdmin = new Bundle();
                bundleNonAdmin.putParcelable(Group.GROUP,nonAdminGroup);
                groupFragment2.setArguments(bundleNonAdmin);
                updateContainer(isAddNewFragment, groupFragment2, ADMIN_GROUP_TAG,isBackPress);
                animateFab(openFragType, false, duration);
                break;
        }
    }

    /** updates the fragment container with new fragment and sets the animation for the transition
     * @param   isAddNewFragment    whether to add fragment or replace.
     * @param   isBackPress     whether the new fragment was resulted from a back press
     * @param fragment the fragment to load
     * @param fragmentTag the new fragment tag to load
     */
    private void updateContainer(boolean isAddNewFragment, Fragment fragment, String fragmentTag, boolean isBackPress){
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(isAddNewFragment || isBackPress){
            transaction.setCustomAnimations(R.anim.anim_slide_in_from_end, R.anim.anim_slide_out_from_start);
        } else{
            transaction.setCustomAnimations(R.anim.anim_slide_in_from_start, R.anim.anim_slide_out_from_end);
        }
        if(isAddNewFragment){
            transaction.add(R.id.containerGroups, fragment, fragmentTag);
        } else{
            transaction.replace(R.id.containerGroups, fragment, fragmentTag);
        }
        transaction.commit();
    }

    /**
     * animate the floating action button - according to the fragment loaded
     * @param fragmentTag the fragment to load - get the fab view
     * @param setVisible whether to show or hide the fab
     * @param duration duration of the transition
     */
    private void animateFab(String fragmentTag, boolean setVisible, long duration){
        int imgResource = -1;
        int color = -1;
        switch (fragmentTag){
            case GROUPS_OVERVIEW_TAG:
                imgResource = R.drawable.fab_plus;
                color = ContextCompat.getColor(this,R.color.fooGreen);
                break;
            case ADMIN_GROUP_TAG:
                imgResource = R.drawable.fab_plus_user;
                color = ContextCompat.getColor(this,R.color.colorPrimary);
                break;
            case NON_ADMIN_GROUP_TAG:
                break;
        }
        FabAnimation.animateFAB(this,fab,duration,imgResource,color,setVisible);
    }

    /** after a user creates a new group from the dialog, run the service to create the group
     *  @param groupName new group name
     *  */
    @Override
    public void onNewGroupClick(String groupName){
        Group newGroup = new Group(groupName, CommonMethods.getMyUserID(this),(long)-1);
        ServerMethods.addGroup(this,newGroup);
    }

    /** handles the floating action button presses from the different fragments of GroupsActivity */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                if(!fragStack.isEmpty()){
                    String currentFrag = fragStack.peek();
                    switch (currentFrag){
                        case GROUPS_OVERVIEW_TAG:
                            // pressed on create a new group - shows the dialog of creating a new group
                            if(!CommonMethods.isUserSignedIn(this)){
                                Intent intent = new Intent(this,SignInActivity.class);
                                startActivity(intent);
                            } else{
                                newGroupDialog = new NewGroupDialog(this);
                                newGroupDialog.show();
                            }
                            break;
                        case ADMIN_GROUP_TAG:
                            // pressed on create a new user in a group the user is the admin of */
                            Toast.makeText(this, "add new member", Toast.LENGTH_SHORT).show();
                            Intent fabClickIntent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
                            fabClickIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_FAB_CLICK);
                            fabClickIntent.putExtra(ReceiverConstants.FAB_TYPE,ReceiverConstants.FAB_TYPE_NEW_GROUP_MEMBER);
                            LocalBroadcastManager.getInstance(this).sendBroadcast(fabClickIntent);
                            break;
                    }
                }
                break;
        }
    }

    @Override
    public void onReplaceFrags(String openFragType, long id) {
        boolean isBackPress = false;
        if (openFragType.equals(BACK_IN_STACK_TAG)) {
            isBackPress = true;
            fragStack.pop();
            try {
                openFragType = fragStack.peek();
            } catch (EmptyStackException e) {
                Log.d(TAG,"empty stack");
            }
        } else {
            fragStack.push(openFragType);
        }
        if (id != CommonConstants.ITEM_ID_EMPTY) {
            if (openFragType.equals(ADMIN_GROUP_TAG)) {
                adminGroupID = id;
            } else if (openFragType.equals(NON_ADMIN_GROUP_TAG)) {
                nonAdminGroupID = id;
            }
        }
        replaceFrags(openFragType, false,isBackPress);
    }

    @Override
    public void gotMyUserImage() {
        Glide.with(this).load(CommonMethods.getMyUserImageFilePath(this)).into(circleImageView);
        headerTxt.setText(CommonMethods.getMyUserName(this));
    }
}
