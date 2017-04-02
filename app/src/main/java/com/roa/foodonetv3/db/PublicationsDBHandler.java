package com.roa.foodonetv3.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.Publication;

import java.util.ArrayList;

public class PublicationsDBHandler {
    private Context context;

    public PublicationsDBHandler(Context context) {
        this.context = context;
    }

    /** get a specific publication with the use of publication id */
    public Publication getPublication(long publicationID){
        String where = String.format("%1$s = ?" ,FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN);
        String[] whereArgs = {String.valueOf(publicationID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,null,where,whereArgs,null);
        /** declarations */
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

    /** get all publications either of the user or not of the user
     * @param typeFilter TYPE_GET_USER_PUBLICATIONS or TYPE_GET_NON_USER_PUBLICATIONS from FoodonetDBProvider */
    public ArrayList<Publication> getPublications(int typeFilter){
        long userID = CommonMethods.getMyUserID(context);
        String filterString;
        if(typeFilter == FoodonetDBProvider.PublicationsDB.TYPE_GET_USER_PUBLICATIONS){
            filterString = "=";
        }else{
            filterString = "!=";
        }
        ArrayList<Publication> publications = new ArrayList<>();
        String where = String.format("%1$s %2$s ?" ,FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN,filterString);
        String[] whereArgs = {String.valueOf(userID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,null,where,whereArgs,null);
        /** declarations */
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
        return publications;
    }

    /** get publications IDs */
    public ArrayList<Long> getPublicationsIDs(){
        ArrayList<Long> publicationsIDs = new ArrayList<>();
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.PublicationsDB.CONTENT_URI,null,null,null,null);
        while(c!= null && c.moveToNext()){
            publicationsIDs.add(c.getLong(c.getColumnIndex(FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN)));
        }
        if(c!=null){
            c.close();
        }
        return publicationsIDs;
    }


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

    /** deletes the publications in the db and add new publications data */
    public void replaceAllPublications(ArrayList<Publication> publications){
        /** delete all publications from db before adding the new ones */
        deleteAllPublications();

        ContentResolver resolver = context.getContentResolver();
        /** declarations */
        Publication publication;
        ContentValues values;

        for(int i = 0 ; i < publications.size(); i++){
            publication = publications.get(i);
            values = new ContentValues();
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

            resolver.insert(FoodonetDBProvider.PublicationsDB.CONTENT_URI,values);
        }
    }

    /** inserts a new publication into the db */
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

    /** deletes all publications from the db */
    public void deleteAllPublications(){
        context.getContentResolver().delete(FoodonetDBProvider.PublicationsDB.CONTENT_URI,null,null);
    }

    /** deletes a specific publication */
    public void deletePublication(long publicationID){
        String where = String.format("%1$s = ?",FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN);
        String [] whereArgs = {String.valueOf(publicationID)};
        context.getContentResolver().delete(FoodonetDBProvider.PublicationsDB.CONTENT_URI,where,whereArgs);
    }

    /** updates an existing publication */
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


