package com.roa.foodonetv3.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

public class FoodonetDBProvider extends ContentProvider {
    private static final String TAG = "FoodonetDBProvider";
    private static final String AUTHORITY = "com.roa.foodonetv3.db.FoodonetDBProvider";
    private FoodonetDBHelper helper;

    /** declarations of the different tables */
    public static class PublicationsDB{
        public static final short TYPE_GET_USER_PUBLICATIONS = 1;
        public static final short TYPE_GET_NON_USER_PUBLICATIONS = 2;

        static final String TABLE_NAME = "publications";
        static final String PUBLICATION_ID_COLUMN = "publication_id";
        static final String TITLE_COLUMN = "title";
        static final String DETAILS_COLUMN = "details";
        static final String ADDRESS_COLUMN = "address";
        static final String TYPE_OF_COLLECTING_COLUMN = "type_of_collecting";
        static final String LATITUDE_COLUMN = "latitude";
        static final String LONGITUDE_COLUMN = "longitude";
        static final String STARTING_TIME_COLUMN = "starting_time";
        static final String ENDING_TIME_COLUMN = "ending_time";
        static final String CONTACT_PHONE_COLUMN = "contact_phone";
        static final String PHOTO_URL_COLUMN = "photo_url";
        static final String IS_ON_AIR_COLUMN = "is_on_air";
        static final String PUBLISHER_ID_COLUMN = "publisher_id";
        static final String PRICE_COLUMN = "price";
        static final String AUDIENCE_COLUMN = "audience";
        static final String PRICE_DESC_COLUMN = "price_desc";
        static final String PUBLICATION_VERSION_COLUMN = "publication_version";
        static final String PROVIDER_USER_NAME_COLUMN = "provider_user_name";
        static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
    }

    public static class ReportsDB{
        static final String TABLE_NAME = "reports";
        static final String REPORT_ID_COLUMN = "report_id";
        static final String PUBLICATION_ID_COLUMN = "publication_id";
        static final String PUBLICATION_VERSION_COLUMN = "publication_version";
        static final String REPORT_COLUMN = "report";
        static final String TIME_OF_REPORT_COLUMN = "time_of_report";
        static final String REPORT_RATING_COLUMN = "report_rating";
        static final String USER_ID_COLUMN = "user_id";
        static final String USER_NAME_COLUMN = "user_name";
        static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
    }

    public static class GroupsDB{
        static final String TABLE_NAME = "groups";
        static final String GROUP_ID_COLUMN = "group_id";
        static final String GROUP_NAME_COLUMN = "group_name";
        static final String ADMIN_ID_COLUMN = "admin_id";
        static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
    }

    public static class MembersDB{
        static final String TABLE_NAME = "users";
        static final String _ID_COLUMN = "_id";
        static final String GROUP_ID_COLUMN = "group_id";
        static final String USER_ID_COLUMN = "user_id";
        static final String USER_NAME_COLUMN = "user_name";
        static final String USER_PHONE_COLUMN = "user_phone";
        static final String IS_ADMIN_COLUMN = "is_admin";
        static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
    }

    public static class LatestPlacesDB{
        static final String TABLE_NAME = "latest_places";
        static final String _ID_COLUMN = "_id";
        static final String POSITION_COLUMN = "position";
        static final String ADDRESS_COLUMN = "address";
        static final String LAT_COLUMN = "lat";
        static final String LNG_COLUMN = "lng";
        static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
    }

    public static class RegisteredUsersDB{
        static final String TABLE_NAME = "registered_users";
        static final String _ID_COLUMN = "_id";
        static final String PUBLICATION_ID_COLUMN = "publication_id";
        static final String PUBLICATION_VERSION_COLUMN = "publication_version";
        static final String REGISTERED_USER_ID_COLUMN = "registered_user_id";
        static final String REGISTERED_USER_ACTIVE_DEVICE_UUID = "registered_user_device_uuid";
        static final String REGISTERED_USER_PHONE = "registered_user_phone";
        static final String REGISTERED_USER_NAME = "registered_user_name";
        static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
    }

    public static class NotificationsDB{
        static final String TABLE_NAME = "notifications";
        static final String _ID_COLUMN = "_id";
        static final String ITEM_ID = "item_id";
        static final String NOTIFICATION_TYPE = "notification_type";
        static final String NOTIFICATION_NAME = "notification_name";
        static final String NOTIFICATION_RECEIVED_TIME = "notification_received_time";
        static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+TABLE_NAME);
    }

    @Override
    public boolean onCreate() {
        helper = new FoodonetDBHelper(getContext());
        return true;
    }

    public String getTableName(Uri uri){
        List<String> pathSegment = uri.getPathSegments();
        return pathSegment.get(0);
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.query(getTableName(uri),projection,selection,selectionArgs,null,null,sortOrder);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(getTableName(uri),null,values);
        if(id != -1){
            return ContentUris.withAppendedId(uri,id);
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(getTableName(uri),selection,selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(getTableName(uri),values,selection,selectionArgs);
    }
}
