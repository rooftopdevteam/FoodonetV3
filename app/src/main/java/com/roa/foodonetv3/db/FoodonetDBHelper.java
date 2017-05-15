package com.roa.foodonetv3.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;

class FoodonetDBHelper extends SQLiteOpenHelper {

    FoodonetDBHelper(Context context) {
        super(context, "foodonet.db", null, context.getResources().getInteger(R.integer.db_version));
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /** create the tables */
        String sql = String.format("CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY,%3$s TEXT,%4$s TEXT,%5$s TEXT,%6$s INTEGER,%7$s REAL,%8$s REAL,%9$s REAL,%10$s,%11$s TEXT,%12$s TEXT,%13$s INTEGER,%14$s INTEGER,%15$s REAL,%16$s INTEGER,%17$s TEXT,%18$s INTEGER,%19$s TEXT)",
                FoodonetDBProvider.PublicationsDB.TABLE_NAME,FoodonetDBProvider.PublicationsDB.PUBLICATION_ID_COLUMN,FoodonetDBProvider.PublicationsDB.TITLE_COLUMN,
                FoodonetDBProvider.PublicationsDB.DETAILS_COLUMN, FoodonetDBProvider.PublicationsDB.ADDRESS_COLUMN,FoodonetDBProvider.PublicationsDB.TYPE_OF_COLLECTING_COLUMN,
                FoodonetDBProvider.PublicationsDB.LATITUDE_COLUMN, FoodonetDBProvider.PublicationsDB.LONGITUDE_COLUMN,FoodonetDBProvider.PublicationsDB.STARTING_TIME_COLUMN,
                FoodonetDBProvider.PublicationsDB.ENDING_TIME_COLUMN, FoodonetDBProvider.PublicationsDB.CONTACT_PHONE_COLUMN,FoodonetDBProvider.PublicationsDB.PHOTO_URL_COLUMN,
                FoodonetDBProvider.PublicationsDB.IS_ON_AIR_COLUMN,FoodonetDBProvider.PublicationsDB.PUBLISHER_ID_COLUMN,FoodonetDBProvider.PublicationsDB.PRICE_COLUMN,
                FoodonetDBProvider.PublicationsDB.AUDIENCE_COLUMN,FoodonetDBProvider.PublicationsDB.PRICE_DESC_COLUMN,FoodonetDBProvider.PublicationsDB.PUBLICATION_VERSION_COLUMN,
                FoodonetDBProvider.PublicationsDB.PROVIDER_USER_NAME_COLUMN);
        sqLiteDatabase.execSQL(sql);
        sql = String.format("CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY,%3$s INTEGER,%4$s INTEGER,%5$s INTEGER,%6$s REAL,%7$s INTEGER,%8$s INTEGER,%9$s TEXT)",
                FoodonetDBProvider.ReportsDB.TABLE_NAME,FoodonetDBProvider.ReportsDB.REPORT_ID_COLUMN,FoodonetDBProvider.ReportsDB.PUBLICATION_ID_COLUMN,
                FoodonetDBProvider.ReportsDB.PUBLICATION_VERSION_COLUMN,FoodonetDBProvider.ReportsDB.REPORT_COLUMN,FoodonetDBProvider.ReportsDB.TIME_OF_REPORT_COLUMN,
                FoodonetDBProvider.ReportsDB.REPORT_RATING_COLUMN,FoodonetDBProvider.ReportsDB.USER_ID_COLUMN,FoodonetDBProvider.ReportsDB.USER_NAME_COLUMN);
        sqLiteDatabase.execSQL(sql);
        sql = String.format("CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY,%3$s TEXT,%4$s INTEGER)",
                FoodonetDBProvider.GroupsDB.TABLE_NAME,FoodonetDBProvider.GroupsDB.GROUP_ID_COLUMN,FoodonetDBProvider.GroupsDB.GROUP_NAME_COLUMN,
                FoodonetDBProvider.GroupsDB.ADMIN_ID_COLUMN);
        sqLiteDatabase.execSQL(sql);
        sql = String.format("CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT,%3$s INTEGER,%4$s INTEGER,%5$s TEXT,%6$s TEXT,%7$s INTEGER)",
                FoodonetDBProvider.MembersDB.TABLE_NAME,FoodonetDBProvider.MembersDB._ID_COLUMN,FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN,
                FoodonetDBProvider.MembersDB.USER_ID_COLUMN,FoodonetDBProvider.MembersDB.USER_NAME_COLUMN,FoodonetDBProvider.MembersDB.USER_PHONE_COLUMN,
                FoodonetDBProvider.MembersDB.IS_ADMIN_COLUMN);
        sqLiteDatabase.execSQL(sql);
        sql = String.format("CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT,%3$s INTEGER,%4$s TEXT,%5$s REAL,%6$s REAL)",
                FoodonetDBProvider.LatestPlacesDB.TABLE_NAME,FoodonetDBProvider.LatestPlacesDB._ID_COLUMN,FoodonetDBProvider.LatestPlacesDB.POSITION_COLUMN,
                FoodonetDBProvider.LatestPlacesDB.ADDRESS_COLUMN,FoodonetDBProvider.LatestPlacesDB.LAT_COLUMN,FoodonetDBProvider.LatestPlacesDB.LNG_COLUMN);
        sqLiteDatabase.execSQL(sql);
        sql = String.format("CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT,%3$s INTEGER,%4$s INTEGER,%5$s INTEGER, %6$s TEXT, %7$s TEXT, %8$s TEXT)",
                FoodonetDBProvider.RegisteredUsersDB.TABLE_NAME,FoodonetDBProvider.RegisteredUsersDB._ID_COLUMN,FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN,
                FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_VERSION_COLUMN,FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ID_COLUMN,
                FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ACTIVE_DEVICE_UUID,FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_NAME,
                FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_PHONE);
        sqLiteDatabase.execSQL(sql);
        sql = String.format("CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT,%3$s INTEGER,%4$s INTEGER,%5$s TEXT,%6$s INTEGER)",
                FoodonetDBProvider.NotificationsDB.TABLE_NAME,FoodonetDBProvider.NotificationsDB._ID_COLUMN,FoodonetDBProvider.NotificationsDB.ITEM_ID,
                FoodonetDBProvider.NotificationsDB.NOTIFICATION_TYPE,FoodonetDBProvider.NotificationsDB.NOTIFICATION_NAME,
                FoodonetDBProvider.NotificationsDB.NOTIFICATION_RECEIVED_TIME);
        sqLiteDatabase.execSQL(sql);

        /** add the rows of the latest places, since they won't be inserted again */
        ContentValues values;
        for (int i = 0; i < CommonConstants.NUMBER_OF_LATEST_SEARCHES; i++) {
            values = new ContentValues();
            /** initially, the positions are set as 0 - 4 */
            values.put(FoodonetDBProvider.LatestPlacesDB.POSITION_COLUMN,i);
            sqLiteDatabase.insert(FoodonetDBProvider.LatestPlacesDB.TABLE_NAME,null,values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
