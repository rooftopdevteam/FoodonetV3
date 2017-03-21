package com.roa.foodonetv3.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.GroupsActivity;
import com.roa.foodonetv3.adapters.GroupMembersRecyclerAdapter;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.OnReplaceFragListener;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.GroupMembersDBHandler;
import com.roa.foodonetv3.model.Group;
import com.roa.foodonetv3.model.GroupMember;
import com.roa.foodonetv3.serverMethods.ServerMethods;
import static android.app.Activity.RESULT_OK;
import static com.roa.foodonetv3.activities.GroupsActivity.CONTACT_PICKER;

public class GroupFragment extends Fragment {
    private static final String TAG = "GroupFragment";
    private static final long UNKNOWN_USER_ID = 0;

    private GroupMembersRecyclerAdapter adapter;
//    private TextView textGroupName;
    private Group group;
    private FoodonetReceiver receiver;
    private OnReplaceFragListener onReplaceFragListener;
    private GroupMembersDBHandler groupMembersDBHandler;

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onReplaceFragListener = (OnReplaceFragListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupMembersDBHandler = new GroupMembersDBHandler(getContext());
        group = getArguments().getParcelable(Group.GROUP);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_group, container, false);

        /** set title */
        getActivity().setTitle(group.getGroupName());

        RecyclerView recyclerGroupMembers = (RecyclerView) v.findViewById(R.id.recyclerGroupMembers);
        recyclerGroupMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupMembersRecyclerAdapter(getContext());
        recyclerGroupMembers.setAdapter(adapter);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new FoodonetReceiver();
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        adapter.updateMembers(group.getGroupID());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.group_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_exit_group:
                long userUniqueID = groupMembersDBHandler.getUserUniqueID(group.getGroupID());
                ServerMethods.deleteGroupMember(getContext(),userUniqueID,true,group.getGroupID());
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e(TAG, getString(R.string.failed_to_pick_contact));
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String phone = null ;
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            if(cursor!=null){
                cursor.moveToFirst();
                // column index of the phone number
                int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                // column index of the contact name
                int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                phone = cursor.getString(phoneIndex);
                name = cursor.getString(nameIndex);
            }
            Log.d(TAG,"phone:"+phone+" ,name:"+name);
            phone = CommonMethods.getDigitsFromPhone(phone);
            if(!groupMembersDBHandler.isMemberInGroup(group.getGroupID(),phone)){
                GroupMember member = new GroupMember((long)-1,group.getGroupID(), UNKNOWN_USER_ID,phone,name,false);
                boolean error = false;
                if(phone==null || name == null){
                    error = true;
                }
                ServerMethods.addGroupMember(getContext(),member);
            } else{
                Toast.makeText(getContext(), R.string.toast_user_already_in_group, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        } finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    }

    private class FoodonetReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            /** receiver for reports got from the service */
            int action = intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1);
            switch (action){
                /** fab click, add new group member */
                case ReceiverConstants.ACTION_FAB_CLICK:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 18/12/2016 add logic if fails
                        Toast.makeText(context, "fab failed", Toast.LENGTH_SHORT).show();
                    } else {
                        if (intent.getIntExtra(ReceiverConstants.FAB_TYPE, -1) == ReceiverConstants.FAB_TYPE_NEW_GROUP_MEMBER) {
                            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                            startActivityForResult(contactPickerIntent, CONTACT_PICKER);
                        }
                    }
                    break;

                /** response from service of adding a new group member */
                case ReceiverConstants.ACTION_ADD_GROUP_MEMBER:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 14/12/2016 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        boolean added = intent.getBooleanExtra(ReceiverConstants.MEMBER_ADDED,false);
                        if(added){
                            adapter.updateMembers(group.getGroupID());
                        } else{
                            Toast.makeText(context, R.string.toast_user_already_in_group, Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_DELETE_GROUP_MEMBER:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 07/03/2017 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        boolean exitedGroup = intent.getBooleanExtra(ReceiverConstants.USER_EXITED_GROUP,false);
                        if(exitedGroup){
                            onReplaceFragListener.onReplaceFrags(GroupsActivity.BACK_IN_STACK_TAG,-1);
                        } else{
                            adapter.updateMembers(group.getGroupID());
                        }
                    }
                    break;
            }
        }
    }
}
