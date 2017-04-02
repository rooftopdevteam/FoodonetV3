package com.roa.foodonetv3.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.roa.foodonetv3.R;
import com.roa.foodonetv3.adapters.PublicationsRecyclerAdapter;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.FoodonetDBProvider;
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.db.RegisteredUsersDBHandler;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.model.User;

import java.util.ArrayList;

public class MyPublicationsFragment extends Fragment{
    private PublicationsRecyclerAdapter adapter;
    private FoodonetReceiver receiver;
    private RecyclerView recyclerMyPublications;
    private View layoutInfo;

    public MyPublicationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_publications, container, false);

        // set title */
        getActivity().setTitle(R.string.drawer_my_shares);

        // set recycler view */
        recyclerMyPublications = (RecyclerView) v.findViewById(R.id.recyclerMyPublications);
        recyclerMyPublications.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PublicationsRecyclerAdapter(getContext());
        recyclerMyPublications.setAdapter(adapter);

        // set info screen for when there are no user publication yet */
        layoutInfo = v.findViewById(R.id.layoutInfo);
        layoutInfo.setVisibility(View.GONE);
        TextView textInfo = (TextView) v.findViewById(R.id.textInfo);
        textInfo.setText(getResources().getString(R.string.hi_what_would_you_like_to_share));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // set the broadcast receiver for getting all publications from the server */
        receiver = new FoodonetReceiver();
        IntentFilter filter =  new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        // update the recycler view from publications from the db */
        PublicationsDBHandler publicationsDBHandler = new PublicationsDBHandler(getContext());
        ArrayList<Publication> publications = publicationsDBHandler.getPublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS);
        RegisteredUsersDBHandler registeredUsersDBHandler = new RegisteredUsersDBHandler(getContext());
        LongSparseArray<Integer> registeredUsers = registeredUsersDBHandler.getAllRegisteredUsersCount();
        if(publications.size()==0){
            recyclerMyPublications.setVisibility(View.GONE);
            layoutInfo.setVisibility(View.VISIBLE);
        } else{
            recyclerMyPublications.setVisibility(View.VISIBLE);
            layoutInfo.setVisibility(View.GONE);
            adapter.updatePublications(publications, registeredUsers);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    /** receiver for reports got from the service */
    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(ReceiverConstants.ACTION_TYPE, -1);
            switch (action) {
                case ReceiverConstants.ACTION_GET_PUBLICATION:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 01/04/2017 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true) &&
                                intent.getLongExtra(User.IDENTITY_PROVIDER_USER_ID,-1)== CommonMethods.getMyUserID(getContext())){
                            adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS);
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_DELETE_PUBLICATION:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 01/04/2017 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS);
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 02/04/2017 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        // if got new registered users update the adapter
                        adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS);
                    }
            }
        }
    }
}
