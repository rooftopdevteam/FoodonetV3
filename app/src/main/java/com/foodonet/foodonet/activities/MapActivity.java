package com.foodonet.foodonet.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.adapters.MapPublicationRecyclerAdapter;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.db.PublicationsDBHandler;
import com.foodonet.foodonet.model.Publication;
import com.foodonet.foodonet.services.GetLocationService;
import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapPublicationRecyclerAdapter.OnImageAdapterClickListener, View.OnClickListener, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "MapActivity";

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
        // get last known user location
        userLocation = CommonMethods.getLastLocation(this);

        // get non user publications from db
        PublicationsDBHandler handler = new PublicationsDBHandler(this);
        publications = handler.getOnlineNonEndedNonUserPublications(CommonConstants.PUBLICATION_SORT_TYPE_CLOSEST);
        adapter.updatePublications(publications);

        // set the broadcast receiver for future stuff
        IntentFilter filter = new IntentFilter(ReceiverConstants.BROADCAST_FOODONET);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

        startMap();

        getNewLocation(false,GetLocationService.TYPE_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void startMap(){
        // get to the onMapReady when done
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
        // Add publications markers
        Publication publication;
        for(int i = 0; i< publications.size(); i++){
            publication = publications.get(i);
            MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
            LatLng publicationLatLng = new LatLng(publication.getLat(), publication.getLng());
            marker = mMap.addMarker(markerOptions.position(publicationLatLng).title(publication.getTitle()));
            marker.setTag(publication.getId());
        }

        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onImageAdapterClicked(LatLng latLng) {
        //move the camera to publication location
        if(mMap!=null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, CommonConstants.ZOOM_IN));
        }
    }

    @Override
    public void onClick(View view) {
        // clicking on the user location icon moves the map to the user location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, CommonConstants.ZOOM_IN));
    }

    /** upon clicking on the info window the app will open the publication detail */
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

    /** updates the data and restart the map */
    private void updateMap(){
        PublicationsDBHandler handler = new PublicationsDBHandler(getBaseContext());
        publications = handler.getOnlineNonEndedNonUserPublications(CommonConstants.PUBLICATION_SORT_TYPE_CLOSEST);
        adapter.updatePublications(publications);
        startMap();
    }

    /**
     * method to get the user's location
     * @param getNewData true for running the get new data method after receiving the location
     * @param actionType GetLocationService.TYPE_NORMAL - try get normal gps/network location
     *                   GetLocationService.TYPE_GET_FUSED - try get the last location from the fused location API
     */
    private void getNewLocation(boolean getNewData, int actionType){
        String locationType = CommonMethods.getAvailableLocationType(this);
        switch (locationType){
            case LocationManager.GPS_PROVIDER:
            case LocationManager.NETWORK_PROVIDER:
                if(CommonMethods.isLocationPermissionGranted(this)){
                    CommonMethods.startGetLocationService(this,getNewData, locationType, actionType);
                    Log.d(TAG,"have permissions");
                } else{
                    Log.d(TAG,"ask permissions");
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},CommonConstants.PERMISSION_REQUEST_LOCATION);
                }
                break;
            case CommonConstants.LOCATION_TYPE_LOCATION_DISABLED:
                Toast.makeText(this, R.string.location_disabled, Toast.LENGTH_SHORT).show();
                Log.d(TAG,"location disabled");
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CommonConstants.PERMISSION_REQUEST_LOCATION:
                // in case of a request */
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // if granted, start getNewLocation service again
                    getNewLocation(true, GetLocationService.TYPE_NORMAL);
                } else {
                    // request denied, give the user a message */
                    CommonMethods.setLastUpdated(this);
                    Toast.makeText(this, getResources().getString(R.string.toast_needs_location_permission), Toast.LENGTH_SHORT).show();
                    Log.d(TAG,getResources().getString(R.string.toast_needs_location_permission));
                }
                break;
        }
    }

    /** localReceiver */
    private class FoodonetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra(ReceiverConstants.ACTION_TYPE,-1)){
                case ReceiverConstants.ACTION_GET_PUBLICATION:
                    if(intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)){
                        Toast.makeText(context, R.string.toast_something_went_wrong_please_try_again, Toast.LENGTH_SHORT).show();
                    } else{
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            updateMap();
                        }
                    }
                    break;
                case ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE:
                    if (!intent.getBooleanExtra(ReceiverConstants.SERVICE_ERROR,false)) {
                        if(intent.getBooleanExtra(ReceiverConstants.UPDATE_DATA,true)){
                            updateMap();
                        }
                    }
                    break;
                case ReceiverConstants.ACTION_GOT_NEW_LOCATION:
                    userLocation = CommonMethods.getLastLocation(MapActivity.this);
                    break;
            }
        }
    }
}
