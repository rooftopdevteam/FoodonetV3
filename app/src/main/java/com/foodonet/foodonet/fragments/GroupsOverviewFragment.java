package com.foodonet.foodonet.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.foodonet.foodonet.R;
import com.foodonet.foodonet.adapters.GroupsRecyclerAdapter;
import com.foodonet.foodonet.commonMethods.OnGotMyUserImageListener;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.db.GroupsDBHandler;
import com.foodonet.foodonet.model.Group;
import java.util.ArrayList;

public class GroupsOverviewFragment extends Fragment {

    private GroupsRecyclerAdapter adapter;
    private TextView textInfo;
    private View layoutInfo;
    private GroupsDBHandler groupsDBHandler;

    private FoodonetReceiver receiver;

    public GroupsOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        receiver = new FoodonetReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_groups_overview, container, false);

        //set title
        getActivity().setTitle(R.string.drawer_groups);

        // set recycler for publications
        RecyclerView recyclerGroupsOverview = (RecyclerView) v.findViewById(R.id.recyclerGroupsOverview);
        recyclerGroupsOverview.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupsRecyclerAdapter(getContext());
        recyclerGroupsOverview.setAdapter(adapter);

        // set info screen for when there are no groups
        layoutInfo = v.findViewById(R.id.layoutInfo);
        layoutInfo.setVisibility(View.GONE);
        textInfo = (TextView) v.findViewById(R.id.textInfo);
        textInfo.setText(R.string.you_dont_have_any_groups_yet);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // register receiver
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        groupsDBHandler = new GroupsDBHandler(getContext());
        ArrayList<Group> groups = groupsDBHandler.getAllGroups();
        if(groups.size() == 0){
            layoutInfo.setVisibility(View.VISIBLE);
            textInfo.setText(R.string.you_dont_have_any_groups_yet);
        } else{
            layoutInfo.setVisibility(View.GONE);
            adapter.updateGroups(groups);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // receiver for reports got from the service
            int action = intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1);
            switch (action){
                case ReceiverConstants.ACTION_ADD_GROUP:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        layoutInfo.setVisibility(View.GONE);
                        adapter.updateGroups(groupsDBHandler.getAllGroups());
                    }
                    break;
                case ReceiverConstants.ACTION_ADD_GROUP_MEMBER:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        adapter.updateGroups(groupsDBHandler.getAllGroups());
                    }
                    break;

                case ReceiverConstants.ACTION_GET_GROUPS:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else {
                        ArrayList<Group> groups = groupsDBHandler.getAllGroups();
                        if(groups.size()!=0){
                            layoutInfo.setVisibility(View.GONE);
                        }
                        adapter.updateGroups(groups);
                    }
                    break;
                case ReceiverConstants.ACTION_SAVE_USER_IMAGE:
                    OnGotMyUserImageListener onGotMyUserImageListener = (OnGotMyUserImageListener) getContext();
                    onGotMyUserImageListener.gotMyUserImage();
                    break;
            }
        }
    }
}
