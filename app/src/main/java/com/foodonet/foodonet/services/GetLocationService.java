package com.foodonet.foodonet.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import java.util.Timer;
import java.util.TimerTask;

public class GetLocationService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GetLocationService";

    public static final String ACTION_TYPE = "action_type";
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_GET_FUSED = 2;
    public static final String LOCATION_TYPE = "location_type";
    public static final String GET_DATA = "get_data";
    private static final String TIMER = "timer";

    private LocationManager locationManager;
    private GoogleApiClient mGoogleApiClient;
    private String locationType;
    private boolean getData, gotLocation;
    private Timer timer;
    private int actionType;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.d(TAG,"onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(locationManager!= null){
            locationManager.removeUpdates(this);
        }
        getData = intent.getBooleanExtra(GET_DATA, false);
        locationType = intent.getStringExtra(LOCATION_TYPE);
        actionType = intent.getIntExtra(ACTION_TYPE,-1);
        gotLocation = false;
        startGetLocation();
        Log.d(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mGoogleApiClient!= null){
            mGoogleApiClient.disconnect();
        }
        Log.d(TAG,"onDestroy");
    }

    /**
     * handles getting the location, according to the type wanted,
     * it will either start by trying to get the data through the network,
     * then, if not received after a second (time is a constant variable),
     * move to get the location through the getLastLocation from fused location API
     */
    public void startGetLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            switch (actionType) {
                case TYPE_NORMAL:
                    timer = new Timer(TIMER);
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            Log.i(TAG,"entered delayed task");
                            if(!gotLocation){
                                locationManager.removeUpdates(GetLocationService.this);
                                connectToLocationAPI();
                            }
                        }
                    };
                    locationManager.requestLocationUpdates(locationType, 1000, 100, this);
                    timer.schedule(timerTask,CommonConstants.TIME_SWITCH_TO_FUSED_MILLIS);
                    break;

                case TYPE_GET_FUSED:
                    connectToLocationAPI();
                    break;
            }
        } else{
            stopSelf();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        gotLocation = true;
        timer.cancel();
        CommonMethods.setLastLocation(this,new LatLng(location.getLatitude(),location.getLongitude()));
        locationManager.removeUpdates(this);
        getNewData();
        Log.i(TAG,"got location from location manager");
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
        stopSelf();
    }


    private void connectToLocationAPI(){
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            gotLocation = true;
            if(lastLocation != null){
                CommonMethods.setLastLocation(this,new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude()));
            }
            getNewData();
            Log.i(TAG,"got location from fused location");
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stopSelf();
    }

    private void getNewData(){
        broadcastGotLocation();
        if(getData){
            CommonMethods.getNewData(this);
        }
        stopSelf();
    }

    private void broadcastGotLocation(){
        Intent intent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
        intent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GOT_NEW_LOCATION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
