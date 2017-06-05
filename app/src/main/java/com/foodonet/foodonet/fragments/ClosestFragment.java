package com.foodonet.foodonet.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.adapters.PublicationsRecyclerAdapter;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.OnGotMyUserImageListener;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.db.FoodonetDBProvider;

public class ClosestFragment extends Fragment{
    private PublicationsRecyclerAdapter adapter;
    private FoodonetReceiver receiver;

    public ClosestFragment() {
        // Required empty public constructor
        receiver = new FoodonetReceiver();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_latest, container, false);

        // set the recycler view and adapter for all publications
        RecyclerView activePubRecycler = (RecyclerView) v.findViewById(R.id.activePubRecycler);
        activePubRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PublicationsRecyclerAdapter(getContext(), CommonConstants.PUBLICATION_SORT_TYPE_CLOSEST,this);
        activePubRecycler.setAdapter(adapter);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // set the broadcast receiver for getting all publications from the server
        IntentFilter filter =  new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);

        adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_NON_USER_PUBLICATIONS);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
    }

    /** set the menu, get it from the activity and add the specific items for this fragment */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = new SearchView(getContext());
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setActionView(item, searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)){
                case ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        // if getDataService finished, update the adapter as we received new groups, publications and registered users
                        adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_NON_USER_PUBLICATIONS);
                    }
                    break;
                case ReceiverConstants.ACTION_GET_PUBLICATION:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_NON_USER_PUBLICATIONS);
                        }
                    }
                    break;
                case ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_NON_USER_PUBLICATIONS);
                        }
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