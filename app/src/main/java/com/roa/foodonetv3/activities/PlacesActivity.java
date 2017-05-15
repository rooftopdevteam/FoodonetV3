package com.roa.foodonetv3.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.db.LatestPlacesDBHandler;
import com.roa.foodonetv3.model.SavedPlace;

import java.util.ArrayList;

public class PlacesActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    public static final int REQUEST_PLACE_PICKER = 10;
    private Intent intentForResult;
    private ArrayList<SavedPlace> places;
    private LatestPlacesDBHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        setTitle(R.string.locations);

        /** set the ListView */
        ListView recentPlacesList = (ListView) findViewById(R.id.recentPlacesList);
        recentPlacesList.setOnItemClickListener(this);
        findViewById(R.id.textPickNewLocation).setOnClickListener(this);

        /** load latest places from db, and make a new String[] for use of the ArrayAdapter */
        handler = new LatestPlacesDBHandler(this);
        places = handler.getAllPlaces();
        String[] placesNames = new String[places.size()];
        String address;
        SavedPlace place;

        for (int i = 0; i < places.size(); i++) {
            place = places.get(i);
            address = place.getAddress();
            if(address==null){
                address = "";
            }
            placesNames[i] = address;
        }
        recentPlacesList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, placesNames));
    }

    @Override
    public void onClick(View view) {
        startGooglePlacesPicker();
    }

    /** start the google places autocomplete widget */
    private void startGooglePlacesPicker(){
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, REQUEST_PLACE_PICKER);

            // Hide the pick option in the UI to prevent users from starting the picker
            // multiple times.
//                    showPickAction(false);

        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil
                    .getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // if no address is in the clicked item, open the google places picker */
        if(places.get(position)==null || places.get(position).getAddress()==null || places.get(position).getAddress().equals("")){
            startGooglePlacesPicker();
        } else{
            // return the clicked place to the AddEditPublicationFragment */
            SavedPlace place = places.get(position);
            intentForResult = new Intent();
            intentForResult.putExtra("place",place);
            setResult(RESULT_OK, intentForResult);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PLACE_PICKER:
                    /** returning from google widget, return the clicked place to the AddEditPublicationFragment after adding it to the db */
                    Place placeData = PlacePicker.getPlace(this, data);
                    SavedPlace place = new SavedPlace(String.valueOf(placeData.getAddress()),placeData.getLatLng().latitude,placeData.getLatLng().longitude);
                    SavedPlace checkPlace;
                    boolean foundPlace = false;
                    for(int i = 0; i < places.size(); i++){
                        checkPlace = places.get(i);
                        if(checkPlace.getAddress() != null && checkPlace.getAddress().equals(place.getAddress()) && checkPlace.getLat() == place.getLat() && checkPlace.getLng() == place.getLng()){
                            foundPlace = true;
                            break;
                        }
                    }
                    if(!foundPlace){
                        handler.addLatestPlace(place);
                    }
                    intentForResult = new Intent();
                    intentForResult.putExtra("place",place);
                    setResult(RESULT_OK, intentForResult);
                    finish();
                    break;
            }
        }
    }

}









