package com.roa.foodonetv3.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.fragments.ActiveFragment;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.services.GetPublicationsService;

import java.util.ArrayList;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Publication> publications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        GetPublicationsReceiver receiver = new GetPublicationsReceiver();
        IntentFilter filter = new IntentFilter(GetPublicationsService.ACTION_SERVICE_GET_PUBLICATIONS);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

        // temp
        Intent i = new Intent(this, GetPublicationsService.class);
        i.putExtra(GetPublicationsService.QUERY_ARGS,"publications.json");
        startService(i);


    }

    @Override
    public void onResume() {
        super.onResume();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a publications markers
        for(int i = 0; i< publications.size(); i++){
            LatLng publicationTest = new LatLng(publications.get(i).getLat(), publications.get(i).getLng());
            mMap.addMarker(new MarkerOptions().position(publicationTest).title("Publication Marker"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(publicationTest));

        }
    }

    private class GetPublicationsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            publications = intent.getParcelableArrayListExtra(GetPublicationsService.QUERY_PUBLICATIONS);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(MapActivity.this);
        }
    }
}
