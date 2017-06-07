package com.foodonet.foodonet.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import com.foodonet.foodonet.adapters.PublicationsRecyclerAdapter;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.OnGotMyUserImageListener;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.db.FoodonetDBProvider;
import com.foodonet.foodonet.db.PublicationsDBHandler;
import com.foodonet.foodonet.model.User;

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
        adapter = new PublicationsRecyclerAdapter(getContext(), CommonConstants.PUBLICATION_SORT_TYPE_RECENT,this);
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
        if(publicationsDBHandler.areUserPublicationsAvailable()){
            recyclerMyPublications.setVisibility(View.VISIBLE);
            layoutInfo.setVisibility(View.GONE);
            adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS);
        } else{
            recyclerMyPublications.setVisibility(View.GONE);
            layoutInfo.setVisibility(View.VISIBLE);
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
                case ReceiverConstants.ACTION_GET_NEW_PUBLICATION:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true) &&
                                intent.getLongExtra(User.IDENTITY_PROVIDER_USER_ID,-1)== CommonMethods.getMyUserID(getContext())){
                            adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS);
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS);
                        }
                    }
                    break;

                case ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        // if got new registered users update the adapter
                        adapter.updatePublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS);
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
