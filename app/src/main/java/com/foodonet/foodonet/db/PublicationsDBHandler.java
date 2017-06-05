package com.foodonet.foodonet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.foodonet.foodonet.asyncTasks.UpdatePublicationsTask;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.model.Publication;

import java.util.ArrayList;

public class PublicationsDBHandler {
    private Context context;

    public PublicationsDBHandler(Context context) {
        this.context = context;
    }

    /**
     * handles querying a specific publication by publication ID
     * @param publicationID Long the publication ID of the publication
     * @return Publication the queried publication or null if not found
     */
    public Publication getPublication(long publicationID){
        String where = String.format("%1$s = ?" ,FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN);
        String[] whereArgs = {String.valueOf(publicationID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,null,where,whereArgs,null);
        long publisherID, audience;
        int version;
        String title, subtitle, address, contactInfo, photoUrl, providerUserName, priceDesc, startingDate, endingDate;
        double lat, lng, price ;
        boolean isOnAir;
        short isOnAirSql, typeOfCollecting;
        Publication publication = null;

        if(c!= null && c.moveToNext()){
            publicationID = c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN));
            version = c.getInt(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN));
            title = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.TITLE_COLUMN));
            subtitle = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.DETAILS_COLUMN));
            address = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.ADDRESS_COLUMN));
            typeOfCollecting = c.getShort(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.TYPE_OF_COLLECTING_COLUMN));
            lat = c.getDouble(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.LATITUDE_COLUMN));
            lng = c.getDouble(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.LONGITUDE_COLUMN));
            startingDate = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.STARTING_TIME_COLUMN));
            endingDate = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.ENDING_TIME_COLUMN));
            contactInfo = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.CONTACT_PHONE_COLUMN));
            isOnAirSql = c.getShort(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN));
            isOnAir = isOnAirSql == CommonConstants.VALUE_TRUE;
            photoUrl = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PHOTO_URL_COLUMN));
            publisherID = c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN));
            audience = c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.AUDIENCE_COLUMN));
            providerUserName = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PROVIDER_USER_NAME_COLUMN));
            price = c.getDouble(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PRICE_COLUMN));
            priceDesc = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PRICE_DESC_COLUMN));

            publication = new Publication(publicationID,version,title,subtitle,address,typeOfCollecting,lat,lng,startingDate,endingDate,contactInfo,isOnAir,
                    null,photoUrl,publisherID,audience,providerUserName,price,priceDesc);
        }
        if(c!=null){
            c.close();
        }
        return publication;
    }

    /**
     * queries and returns all publication in the db that are online, not ended and non user created (checked by userID and not active device UUID) sorted by choice of latest or closest
     * @param sortType sort order to be returned - either closest (CommonConstants.PUBLICATION_SORT_TYPE_CLOSEST) or latest (CommonConstants.PUBLICATION_SORT_TYPE_RECENT)
     * @return ArrayList of online, non ended, non user created publications by the sort order
     */
    public ArrayList<Publication> getOnlineNonEndedNonUserPublications(int sortType){
        long userID = CommonMethods.getMyUserID(context);
        String where = String.format("%1$s != ? AND %2$s > ? AND %3$s = ?" ,
                FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN,FoodonetDBProvider.PublicationsDB.ENDING_TIME_COLUMN,
                FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN);
        String[] whereArgs = {String.valueOf(userID), CommonMethods.getCurrentTimeSecondsString(),String.valueOf(CommonConstants.VALUE_TRUE)};
        return getPublications(where,whereArgs,sortType);
    }

    /**
     * queries and returns all user created publications in the db (checked by userID and not active device UUID) sorted by choice of latest or closest
     * the result will contain both online, offline and ended publications
     * @param sortType sort order to be returned - either closest (CommonConstants.PUBLICATION_SORT_TYPE_CLOSEST) or latest (CommonConstants.PUBLICATION_SORT_TYPE_RECENT)
     * @return ArrayList of all user publications - may be offline or ended as well
     */
    public ArrayList<Publication> getUserPublications(int sortType){
        long userID = CommonMethods.getMyUserID(context);
        String where = String.format("%1$s = ?" ,
                FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN);
        String[] whereArgs = {String.valueOf(userID)};
        return getPublications(where,whereArgs,sortType);
    }

    /**
     * handles the queries of get publications, getting the query, and sorting by the sort order
     * @param selection the selection to query
     * @param selectionArgs selection args to query
     * @param sortType the sort to order either closest (CommonConstants.PUBLICATION_SORT_TYPE_CLOSEST) or latest (CommonConstants.PUBLICATION_SORT_TYPE_RECENT)
     * @return ArrayList of the queried, sorted publications
     */
    private ArrayList<Publication> getPublications(String selection, String[] selectionArgs, int sortType){
        ArrayList<Publication> publications = new ArrayList<>();
        String sortOrder = null;
        ArrayList<Double> distances = new ArrayList<>();
        LatLng userLocation = CommonMethods.getLastLocation(context);
        if(sortType == CommonConstants.PUBLICATION_SORT_TYPE_RECENT){
            sortOrder = FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN+" DESC";
        }
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,null,selection,selectionArgs,sortOrder);
        // declarations */
        long publicationID, publisherID, audience;
        int version;
        String title, subtitle, address, contactInfo, photoUrl, providerUserName, priceDesc, startingDate, endingDate;
        double lat, lng, price ;
        boolean isOnAir;
        short isOnAirSql, typeOfCollecting;

        while(c!= null && c.moveToNext()){
            publicationID = c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN));
            version = c.getInt(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN));
            title = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.TITLE_COLUMN));
            subtitle = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.DETAILS_COLUMN));
            address = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.ADDRESS_COLUMN));
            typeOfCollecting = c.getShort(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.TYPE_OF_COLLECTING_COLUMN));
            lat = c.getDouble(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.LATITUDE_COLUMN));
            lng = c.getDouble(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.LONGITUDE_COLUMN));
            if(sortType == CommonConstants.PUBLICATION_SORT_TYPE_CLOSEST){
                distances.add(CommonMethods.distance(userLocation.latitude,userLocation.longitude,lat,lng));
            }
            startingDate = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.STARTING_TIME_COLUMN));
            endingDate = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.ENDING_TIME_COLUMN));
            contactInfo = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.CONTACT_PHONE_COLUMN));
            isOnAirSql = c.getShort(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN));
            isOnAir = isOnAirSql == CommonConstants.VALUE_TRUE;
            photoUrl = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PHOTO_URL_COLUMN));
            publisherID = c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN));
            audience = c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.AUDIENCE_COLUMN));
            providerUserName = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PROVIDER_USER_NAME_COLUMN));
            price = c.getDouble(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PRICE_COLUMN));
            priceDesc = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PRICE_DESC_COLUMN));

            publications.add(new Publication(publicationID,version,title,subtitle,address,typeOfCollecting,lat,lng,startingDate,endingDate,contactInfo,isOnAir,
                    null,photoUrl,publisherID,audience,providerUserName,price,priceDesc));
        }
        if(c!=null){
            c.close();
        }
        if(sortType == CommonConstants.PUBLICATION_SORT_TYPE_CLOSEST){
            int[] sorted = CommonMethods.getListIndexSortedValues(distances);
            ArrayList<Publication> sortedPublications = new ArrayList<>();
            for(int i = 0; i < sorted.length; i++){
                sortedPublications.add(publications.get(sorted[i]));
            }
            return sortedPublications;
        }
        return publications;
    }

    /**
     * handles getting all online non ended publications IDs from the db, including user created and non user created
     * @return ArrayList of online non ended publications IDs in the db
     */
    public ArrayList<Long> getOnlineNonEndedPublicationsIDs(){
        ArrayList<Long> publicationsIDs = new ArrayList<>();
        String[] projection = {FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN};
        String where = String.format("%1$s = ? AND %2$s > ?",
                FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN,FoodonetDBProvider.PublicationsDB.ENDING_TIME_COLUMN);
        String[] whereArgs = {String.valueOf(CommonConstants.VALUE_TRUE),CommonMethods.getCurrentTimeSecondsString()};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,where,whereArgs,null);
        while(c!= null && c.moveToNext()){
            publicationsIDs.add(c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN)));
        }
        if(c!=null){
            c.close();
        }
        return publicationsIDs;
    }

    /**
     * handles getting all public publications IDs from the db (should not be offline or ended publications)
     * @return ArrayList of all public publications IDs
     */
    private ArrayList<Long> getPublicPublicationsIDs(){
        ArrayList<Long> publicationsIDs = new ArrayList<>();
        String[] projection = {FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN};
        String where = String.format("%1$s = ?", FoodonetDBProvider.PublicationsDB.AUDIENCE_COLUMN);
        String[] whereArgs = {String.valueOf(0)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,where,whereArgs,null);
        while(c!= null && c.moveToNext()){
            publicationsIDs.add(c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN)));
        }
        if(c!=null){
            c.close();
        }
        return publicationsIDs;
    }

    /**
     * handles getting the IDs of all non public publications that will need to be checked manually one by one for an update or delete in the db
     * since the get all publications method in the foodonet server only returns public online publications, the non public publications needs to be checked individually
     * @return ArrayList of non public publications that may need an update
     */
    public ArrayList<Long> getNonPublicPublicationsToUpdateIDs(){
        ArrayList<Long> publications = new ArrayList<>();
        String[] projection = {FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN};
        String selection = String.format("%1$s != ?",
                FoodonetDBProvider.PublicationsDB.AUDIENCE_COLUMN);
        String[] selectionArgs = {"0"};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,selection,selectionArgs,null);
        while(c!= null && c.moveToNext()){
            publications.add(c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN)));
        }
        if(c!=null){
            c.close();
        }
        return publications;
    }

    /**
     * handles getting the file names of all publications in the db, which may include user created offline or ended publications as well
     * @return ArrayList of file names of all publications in db
     */
    public ArrayList<String> getPublicationsImagesFileNames(){
        ArrayList<String> fileNames = new ArrayList<>();
        String[] projection = {FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN,FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,null,null,null);
        long id;
        int version;
        while(c!= null && c.moveToNext()){
            id = c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN));
            version = c.getInt(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN));
            fileNames.add(CommonMethods.getFileNameFromPublicationID(id,version));
        }
        if(c!=null){
            c.close();
        }
        return fileNames;
    }

    /**
     * handles getting a specific publication version number from the db
     * @param publicationID the publication ID of the publication to query
     * @return int - version number of the queried publication
     */
    public int getPublicationVersion(long publicationID) {
        String[] projection = {FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN};
        String selection = String.format("%1$s = ?",FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN);
        String[] selectionArgs = {String.valueOf(publicationID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,selection,selectionArgs,null);
        int publicationVersion = -1;
        if(c!= null && c.moveToNext()){
            publicationVersion = c.getInt(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN));
        }
        if(c!= null){
            c.close();
        }
        return publicationVersion;
    }

    /**
     * handles getting a specific publication title from the db
     * @param publicationID the publication ID of the publication to query
     * @return String of the title of the queried publication
     */
    public String getPublicationTitle(long publicationID) {
        String[] projection = {FoodonetDBProvider.PublicationsDB.TITLE_COLUMN};
        String selection = String.format("%1$s = ?",FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN);
        String[] selectionArgs = {String.valueOf(publicationID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,selection,selectionArgs,null);
        String title = null;
        if(c != null && c.moveToNext()){
            title = c.getString(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.TITLE_COLUMN));
        }
        if(c != null){
            c.close();
        }
        return title;
    }

    /**
     * checks in the db for the publications and returns true if the user is the publisher
     * @param publicationID the ID of the publication to check
     * @return true if the user is the admin (publisher)
     */
    public boolean isUserAdmin(long publicationID){
        String[] projection = {FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN};
        String selection = String.format("%1$s = ? AND %2$s = ?",FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN,FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN);
        String[] selectionArgs = {String.valueOf(publicationID),String.valueOf(CommonMethods.getMyUserID(context))};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,selection,selectionArgs,null);
        boolean userAdmin = false;
        if(c!= null && c.moveToNext()){
            userAdmin = true;
        }
        if(c!= null){
             c.close();
        }
        return userAdmin;
    }

    /**
     * handles checking if a specific publication is online in the db, note that it may also be an ended user publication
     * @param publicationID id of the publication to check
     * @return true if the publication is online
     */
    public boolean isPublicationOnline(long publicationID) {
        String[] projection = {FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN};
        String selection = String.format("%1$s = ? AND %2$s = ?",
                FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN,FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN);
        String[] selectionArgs = {String.valueOf(publicationID),String.valueOf(CommonConstants.VALUE_TRUE)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,selection,selectionArgs,null);
        boolean isOnline = false;
        if(c != null && c.moveToNext()){
            isOnline = true;
        }
        if(c != null){
            c.close();
        }
        return isOnline;
    }


    /**
     * handles checking if there are any user publications in the db, they may be offline or ended
     * @return true if user's publications are available
     */
    public boolean areUserPublicationsAvailable(){
        String[] projection = {FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN};
        String selection = String.format("%1$s = ?",FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN);
        String[] selectionArgs = {String.valueOf(CommonMethods.getMyUserID(context))};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,projection,selection,selectionArgs,null);
        boolean publicationsAvailable = false;
        if(c!= null && c.moveToNext()){
            publicationsAvailable = true;
        }
        if(c!= null){
            c.close();
        }
        return publicationsAvailable;
    }

//    /** deletes the publications in the db and add new publications data
//     *  @deprecated since the get all publication method in the server only returns public publications, it is no longer possible to delete all and replace all
//     *  */
//    public void replaceAllPublications(ArrayList<Publication> publications){
//        /** delete all publications from db before adding the new ones */
//        deleteAllPublications();
//
//        ContentResolver resolver = context.getContentResolver();
//        /** declarations */
//        Publication publication;
//        ContentValues values;
//
//        for(int i = 0 ; i < publications.size(); i++){
//            publication = publications.get(i);
//            values = new ContentValues();
//            values.put(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN,publication.getId());
//            values.put(FoodonetDBProvider.PublicationsDB.TITLE_COLUMN,publication.getTitle());
//            values.put(FoodonetDBProvider.PublicationsDB.DETAILS_COLUMN,publication.getSubtitle());
//            values.put(FoodonetDBProvider.PublicationsDB.ADDRESS_COLUMN,publication.getAddress());
//            values.put(FoodonetDBProvider.PublicationsDB.TYPE_OF_COLLECTING_COLUMN,publication.getTypeOfCollecting());
//            values.put(FoodonetDBProvider.PublicationsDB.LATITUDE_COLUMN,publication.getLat());
//            values.put(FoodonetDBProvider.PublicationsDB.LONGITUDE_COLUMN,publication.getLng());
//            values.put(FoodonetDBProvider.PublicationsDB.STARTING_TIME_COLUMN,publication.getStartingDate());
//            values.put(FoodonetDBProvider.PublicationsDB.ENDING_TIME_COLUMN,publication.getEndingDate());
//            values.put(FoodonetDBProvider.PublicationsDB.CONTACT_PHONE_COLUMN,publication.getContactInfo());
//            values.put(FoodonetDBProvider.PublicationsDB.PHOTO_URL_COLUMN,publication.getPhotoURL());
//            if(publication.isOnAir()){
//                values.put(FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN,CommonConstants.VALUE_TRUE);
//            } else{
//                values.put(FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN,CommonConstants.VALUE_FALSE);
//            }
//            values.put(FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN,publication.getPublisherID());
//            values.put(FoodonetDBProvider.PublicationsDB.PRICE_COLUMN,publication.getPrice());
//            values.put(FoodonetDBProvider.PublicationsDB.AUDIENCE_COLUMN,publication.getAudience());
//            values.put(FoodonetDBProvider.PublicationsDB.PRICE_DESC_COLUMN,publication.getPriceDescription());
//            values.put(FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN,publication.getVersion());
//            values.put(FoodonetDBProvider.PublicationsDB.PROVIDER_USER_NAME_COLUMN,publication.getIdentityProviderUserName());
//
//            resolver.insert(FoodonetDBProvider.PublicationsDB.CONTENT_URI,values);
//        }
//    }

    /**
     * updates the db with new public publication available, updating different versions ones with the new data
     * afterwards, all remaining public publications not received in new data (might be user created or non user created offline or ended publications),
     * will be checked one by one through the update publications task to handle.
     * @param newPublications new public publications to set
     */
    public void updatePublicPublicationsData(ArrayList<Publication> newPublications){
        // get current public publications in db
        ArrayList<Long> existingPublicPublicationsIDs = getPublicPublicationsIDs();

        for(int i = 0 ; i < newPublications.size(); i++){
            long publicationID = newPublications.get(i).getId();
            // check if publication is already in db
            if(existingPublicPublicationsIDs.contains(publicationID)){
                // if in db - remove from existing list - no need to check afterwards
                existingPublicPublicationsIDs.remove(publicationID);
                // check if version is different
                if(getPublicationVersion(publicationID)!= newPublications.get(i).getVersion()){
                    // version is different, update
                    updatePublication(newPublications.get(i));
                }
            } else{
                // publication not in db, insert to db
                insertPublication(newPublications.get(i));
            }
        }
        // after updating all online non ended public publications, check for updates for the rest one by one in the update publications task
        UpdatePublicationsTask updatePublicationsTask = new UpdatePublicationsTask(context,true);
        updatePublicationsTask.execute(existingPublicPublicationsIDs);
    }

    /**
     * handles inserting a new publication to the db
     * @param publication the Publication to insert
     */
    public void insertPublication(Publication publication){
        ContentValues values = new ContentValues();
        values.put(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN,publication.getId());
        values.put(FoodonetDBProvider.PublicationsDB.TITLE_COLUMN,publication.getTitle());
        values.put(FoodonetDBProvider.PublicationsDB.DETAILS_COLUMN,publication.getSubtitle());
        values.put(FoodonetDBProvider.PublicationsDB.ADDRESS_COLUMN,publication.getAddress());
        values.put(FoodonetDBProvider.PublicationsDB.TYPE_OF_COLLECTING_COLUMN,publication.getTypeOfCollecting());
        values.put(FoodonetDBProvider.PublicationsDB.LATITUDE_COLUMN,publication.getLat());
        values.put(FoodonetDBProvider.PublicationsDB.LONGITUDE_COLUMN,publication.getLng());
        values.put(FoodonetDBProvider.PublicationsDB.STARTING_TIME_COLUMN,publication.getStartingDate());
        values.put(FoodonetDBProvider.PublicationsDB.ENDING_TIME_COLUMN,publication.getEndingDate());
        values.put(FoodonetDBProvider.PublicationsDB.CONTACT_PHONE_COLUMN,publication.getContactInfo());
        values.put(FoodonetDBProvider.PublicationsDB.PHOTO_URL_COLUMN,publication.getPhotoURL());
        if(publication.isOnAir()){
            values.put(FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN,CommonConstants.VALUE_TRUE);
        } else{
            values.put(FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN,CommonConstants.VALUE_FALSE);
        }
        values.put(FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN,publication.getPublisherID());
        values.put(FoodonetDBProvider.PublicationsDB.PRICE_COLUMN,publication.getPrice());
        values.put(FoodonetDBProvider.PublicationsDB.AUDIENCE_COLUMN,publication.getAudience());
        values.put(FoodonetDBProvider.PublicationsDB.PRICE_DESC_COLUMN,publication.getPriceDescription());
        values.put(FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN,publication.getVersion());
        values.put(FoodonetDBProvider.PublicationsDB.PROVIDER_USER_NAME_COLUMN,publication.getIdentityProviderUserName());
        context.getContentResolver().insert(FoodonetDBProvider.PublicationsDB.CONTENT_URI,values);
    }

    /**
     * handles deleting all publications from the db
     */
    public void deleteAllPublications(){
        context.getContentResolver().delete(FoodonetDBProvider.PublicationsDB.CONTENT_URI,null,null);
    }

    /**
     * handles deleting a specific publication from the db
     * @param publicationID the ID of the publication to delete
     */
    public void deletePublication(long publicationID){
        String where = String.format("%1$s = ?",FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN);
        String [] whereArgs = {String.valueOf(publicationID)};
        context.getContentResolver().delete(FoodonetDBProvider.PublicationsDB.CONTENT_URI,where,whereArgs);
    }

    /**
     * handles updating a specific publication in the db
     * @param publication the Publication object to update
     */
    public void updatePublication(Publication publication){
        String where = String.format("%1$s = ?",FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN);
        String[] whereArgs = {String.valueOf(publication.getId())};

        ContentValues values = new ContentValues();
        values.put(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN,publication.getId());
        values.put(FoodonetDBProvider.PublicationsDB.TITLE_COLUMN,publication.getTitle());
        values.put(FoodonetDBProvider.PublicationsDB.DETAILS_COLUMN,publication.getSubtitle());
        values.put(FoodonetDBProvider.PublicationsDB.ADDRESS_COLUMN,publication.getAddress());
        values.put(FoodonetDBProvider.PublicationsDB.TYPE_OF_COLLECTING_COLUMN,publication.getTypeOfCollecting());
        values.put(FoodonetDBProvider.PublicationsDB.LATITUDE_COLUMN,publication.getLat());
        values.put(FoodonetDBProvider.PublicationsDB.LONGITUDE_COLUMN,publication.getLng());
        values.put(FoodonetDBProvider.PublicationsDB.STARTING_TIME_COLUMN,publication.getStartingDate());
        values.put(FoodonetDBProvider.PublicationsDB.ENDING_TIME_COLUMN,publication.getEndingDate());
        values.put(FoodonetDBProvider.PublicationsDB.CONTACT_PHONE_COLUMN,publication.getContactInfo());
        values.put(FoodonetDBProvider.PublicationsDB.PHOTO_URL_COLUMN,publication.getPhotoURL());
        if(publication.isOnAir()){
            values.put(FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN,CommonConstants.VALUE_TRUE);
        } else{
            values.put(FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN,CommonConstants.VALUE_FALSE);
        }
        values.put(FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN,publication.getPublisherID());
        values.put(FoodonetDBProvider.PublicationsDB.PRICE_COLUMN,publication.getPrice());
        values.put(FoodonetDBProvider.PublicationsDB.AUDIENCE_COLUMN,publication.getAudience());
        values.put(FoodonetDBProvider.PublicationsDB.PRICE_DESC_COLUMN,publication.getPriceDescription());
        values.put(FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN,publication.getVersion());
        values.put(FoodonetDBProvider.PublicationsDB.PROVIDER_USER_NAME_COLUMN,publication.getIdentityProviderUserName());
        context.getContentResolver().update(FoodonetDBProvider.PublicationsDB.CONTENT_URI,values,where,whereArgs);
    }
}