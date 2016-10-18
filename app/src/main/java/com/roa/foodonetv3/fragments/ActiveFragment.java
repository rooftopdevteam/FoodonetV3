package com.roa.foodonetv3.fragments;

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
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.adapters.PublicationsRecyclerAdapter;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.services.AddPublicationService;
import com.roa.foodonetv3.services.GetPublicationsService;
import java.util.ArrayList;

public class ActiveFragment extends Fragment implements View.OnClickListener {
    private PublicationsRecyclerAdapter adapter;

    public ActiveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetPublicationsReceiver receiver = new GetPublicationsReceiver();
        IntentFilter filter = new IntentFilter(GetPublicationsService.ACTION_SERVICE_GET_PUBLICATIONS);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_active, container, false);

        RecyclerView activePubRecycler = (RecyclerView) v.findViewById(R.id.activePubRecycler);
        activePubRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PublicationsRecyclerAdapter(getContext());
        activePubRecycler.setAdapter(adapter);

        // temp
        v.findViewById(R.id.btnAddPub).setOnClickListener(this);
        //

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // temp
        Intent i = new Intent(getContext(), GetPublicationsService.class);
        i.putExtra(GetPublicationsService.QUERY_ARGS,"publications.json");
        getContext().startService(i);
    }

    @Override
    public void onClick(View v) {
        /** temporary button to add a test publication to the server */
        Intent i = new Intent(getContext(), AddPublicationService.class);
        getContext().startService(i);
    }


    public class GetPublicationsReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Publication> publications = intent.getParcelableArrayListExtra(GetPublicationsService.QUERY_PUBLICATIONS);
            adapter.updatePublications(publications);
        }
    }
}

