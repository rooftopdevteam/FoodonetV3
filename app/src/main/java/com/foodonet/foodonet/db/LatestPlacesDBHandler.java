package com.foodonet.foodonet.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.model.SavedPlace;

import java.util.ArrayList;

public class LatestPlacesDBHandler {
    private Context context;

    public LatestPlacesDBHandler(Context context) {
        this.context = context;
    }

    /**
     * @return ArrayList<SavedPlace> list of latest saved places sorted by insert time
     */
    public ArrayList<SavedPlace> getAllPlaces(){
        ArrayList<SavedPlace> savedPlaces = new ArrayList<>();
        String sortOrder = String.format("%1$s ASC",FoodonetDBProvider.LatestPlacesDB.POSITION_COLUMN);
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.LatestPlacesDB.CONTENT_URI,null,null,null,sortOrder);

        String address;
        double lat, lng;
        while(c!=null && c.moveToNext()){
            address = c.getString(c.getColumnIndex(FoodonetDBProvider.LatestPlacesDB.ADDRESS_COLUMN));
            lat = c.getDouble(c.getColumnIndex(FoodonetDBProvider.LatestPlacesDB.LAT_COLUMN));
            lng = c.getDouble(c.getColumnIndex(FoodonetDBProvider.LatestPlacesDB.LNG_COLUMN));

            savedPlaces.add(new SavedPlace(address,lat,lng));
        }
        if(c!=null){
            c.close();
        }
        return savedPlaces;
    }

    /** add a new savedPlace to the top of the list of last searched places
     * @param savedPlace the place to be saved into the db */
    public void addLatestPlace(SavedPlace savedPlace){
        ContentResolver resolver = context.getContentResolver();
        ContentValues values;

        // change the position of each of the places, and change the values of the last one which will become the first with the data that we received
        int arraySize = CommonConstants.NUMBER_OF_LATEST_SEARCHES;

        for (int i = arraySize -1; i >= -1 ; i--) {
            values = new ContentValues();
            String where = String.format("%1$s = ?",FoodonetDBProvider.LatestPlacesDB.POSITION_COLUMN);
            String[] whereArgs = {String.valueOf(i)};

            // put the values in the last position and change it's position to -1 temporarily
            if(i == arraySize -1){
                values.put(FoodonetDBProvider.LatestPlacesDB.ADDRESS_COLUMN, savedPlace.getAddress());
                values.put(FoodonetDBProvider.LatestPlacesDB.LAT_COLUMN, savedPlace.getLat());
                values.put(FoodonetDBProvider.LatestPlacesDB.LNG_COLUMN, savedPlace.getLng());
                values.put(FoodonetDBProvider.LatestPlacesDB.POSITION_COLUMN,-1);
            } else{
                values.put(FoodonetDBProvider.LatestPlacesDB.POSITION_COLUMN, i +1);
            }
            resolver.update(FoodonetDBProvider.LatestPlacesDB.CONTENT_URI,values,where,whereArgs);
        }
    }

    /** empties all places, doesn't delete the rows themselves, only resets the address, lat and lng rows */
    public void deleteAllPlaces(){
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(FoodonetDBProvider.LatestPlacesDB.ADDRESS_COLUMN,"");
        values.put(FoodonetDBProvider.LatestPlacesDB.LAT_COLUMN,CommonConstants.LATLNG_ERROR);
        values.put(FoodonetDBProvider.LatestPlacesDB.LNG_COLUMN,CommonConstants.LATLNG_ERROR);

        resolver.update(FoodonetDBProvider.LatestPlacesDB.CONTENT_URI,values,null,null);
    }
}
