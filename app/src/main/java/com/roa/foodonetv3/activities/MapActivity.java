package com.roa.foodonetv3.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.adapters.MapPublicationRecyclerAdapter;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.FoodonetDBProvider;
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.model.Publication;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapPublicationRecyclerAdapter.OnImageAdapterClickListener, View.OnClickListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private ArrayList<Publication> publications = new ArrayList<>();
    private LatLng userLocation;
    private FoodonetReceiver receiver;
    private MapPublicationRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        receiver = new FoodonetReceiver();
        RecyclerView mapRecycler = (RecyclerView) findViewById(R.id.mapRecycler);
        ImageView imageMyLocation = (ImageView) findViewById(R.id.imageMyLocation);
        imageMyLocation.setOnClickListener(this);

        mapRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new MapPublicationRecyclerAdapter(this);
        mapRecycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        /** get last known user location */
        userLocation = CommonMethods.getLastLocation(this);

        /** get non user publications from db */
        PublicationsDBHandler handler = new PublicationsDBHandler(this);
        publications = handler.getPublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_NON_USER_PUBLICATIONS);
        adapter.updatePublications(publications);

        /** set the broadcast receiver for future stuff */
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

        startMap();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void startMap(){
        /** get to the onMapReady when done */
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if(mapFragment!=null) {
            mapFragment.getMapAsync(MapActivity.this);
        }
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
        mMap.getUiSettings().setMapToolbarEnabled(false);
        Marker marker;

        if(userLocation!=null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, CommonConstants.ZOOM_OUT));
            marker = mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            marker.setTag((long)-1);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, CommonConstants.ZOOM_IN));
        }
        /** Add publications markers */
        Publication publication;
        for(int i = 0; i< publications.size(); i++){
            publication = publications.get(i);
            MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker_xh));
            LatLng publicationLatLng = new LatLng(publication.getLat(), publication.getLng());
            marker = mMap.addMarker(markerOptions.position(publicationLatLng).title(publication.getTitle()));
            marker.setTag(publication.getId());
        }

        mMap.setOnInfoWindowClickListener(this);

//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                long publicationID = (long) marker.getTag();
//                Toast.makeText(MapActivity.this, "id: "+publicationID, Toast.LENGTH_SHORT).show();
//                return true;
//            }
//        });
    }

    @Override
    public void onImageAdapterClicked(LatLng latLng) {
        /**move the camera to publication location*/
        if(mMap!=null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, CommonConstants.ZOOM_IN));
        }
    }

    @Override
    public void onClick(View view) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, CommonConstants.ZOOM_IN));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        long publicationID = (long) marker.getTag();
        if(publicationID!=-1){
            Intent detailsIntent = new Intent(this,PublicationActivity.class);
            detailsIntent.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION,PublicationActivity.PUBLICATION_DETAIL_TAG);
            detailsIntent.putExtra(Publication.PUBLICATION_KEY,publicationID);
            detailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(detailsIntent);
        }
    }

    private void updateMap(){
        PublicationsDBHandler handler = new PublicationsDBHandler(getBaseContext());
        publications = handler.getPublications(FoodonetDBProvider.PublicationsDB.TYPE_GET_NON_USER_PUBLICATIONS);
        adapter.updatePublications(publications);
        startMap();
    }

    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)){
                case ReceiverConstants.ACTION_GET_PUBLICATION:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 01/04/2017 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            updateMap();
                        }
                    }
                    break;
                case ReceiverConstants.ACTION_DELETE_PUBLICATION:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        // TODO: 01/04/2017 add logic if fails
                        Toast.makeText(context, "service failed", Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            updateMap();
                        }
                    }
                    break;
            }
        }
    }
}
