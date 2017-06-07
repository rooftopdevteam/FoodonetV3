package com.foodonet.foodonet.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.LongSparseArray;

import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.model.RegisteredUser;

import java.util.ArrayList;

public class RegisteredUsersDBHandler {
    private Context context;

    public RegisteredUsersDBHandler(Context context) {
        this.context = context;
    }

    /** @return an array with the publicationID as key and value of the number of registered users for that publication */
    public LongSparseArray<Integer> getAllRegisteredUsersCount(){
        String[] projection = {FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.RegisteredUsersDB.CONTENT_URI,projection,null,null,null);
        LongSparseArray<Integer> registeredUsers = new LongSparseArray<>();
        long publicationID;
        Integer count;

        while(c!=null && c.moveToNext()){
            publicationID = c.getLong(c.getColumnIndex(FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN));
            count = registeredUsers.get(publicationID);
            if(count==null){
                registeredUsers.put(publicationID,1);
            } else{
                registeredUsers.put(publicationID,count+1);
            }
        }
        if(c!=null){
            c.close();
        }
        return registeredUsers;
    }

    /**
     * handles getting the user count for a specific publication from the db
     * @param publicationID the ID of the publication to get the user count for
     * @return the count of registered users the publication has
     */
    public int getPublicationRegisteredUsersCount(long publicationID){
        String[] projection = {FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ID_COLUMN};
        String where = String.format("%1$s = ?",FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN);
        String[] whereArgs = {String.valueOf(publicationID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.RegisteredUsersDB.CONTENT_URI,projection,where,whereArgs,null);
        int count = 0;
        while(c!=null && c.moveToNext()){
            count++;
        }
        if(c!=null){
            c.close();
        }
        return count;
    }

    /**
     * handles getting the list of registered users of a specific publication in the db
     * @param publicationID the ID of the publication to get the user list for
     * @return ArrayList of the registered users of the publication */
    public ArrayList<RegisteredUser> getPublicationRegisteredUsers(long publicationID){
        String where = String.format("%1$s = ?",FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN);
        String[] whereArgs = {String.valueOf(publicationID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.RegisteredUsersDB.CONTENT_URI,null,where,whereArgs,null);
        ArrayList<RegisteredUser> registeredUsers = new ArrayList<>();
        long registeredUserID;
        int publicationVersion;
        String activeDeviceUUID, userName, userPhone;

        while(c!=null && c.moveToNext()){
            publicationID = c.getLong(c.getColumnIndex(FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN));
            publicationVersion = c.getInt(c.getColumnIndex(FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_VERSION_COLUMN));
            registeredUserID = c.getLong(c.getColumnIndex(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ID_COLUMN));
            activeDeviceUUID = c.getString(c.getColumnIndex(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ACTIVE_DEVICE_UUID_COLUMN));
            userName = c.getString(c.getColumnIndex(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_NAME_COLUMN));
            userPhone = c.getString(c.getColumnIndex(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_PHONE_COLUMN));

            registeredUsers.add(new RegisteredUser(publicationID,(double)-1,activeDeviceUUID,publicationVersion,userName,userPhone,registeredUserID));
        }
        if(c!=null){
            c.close();
        }
        return registeredUsers;
    }

    /**
     * handles checking if the user is registered to a specific publication in the db
     * @param publicationID ID of the publication to check
     * @return true if the user is registered for this publication */
    public boolean isUserRegistered(long publicationID){
        long userID = CommonMethods.getMyUserID(context);
        String[] projection = {FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ID_COLUMN};
        String where = String.format("%1$s = ? AND %2$s = ?",FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN,FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ID_COLUMN);
        String[] whereArgs = {String.valueOf(publicationID),String.valueOf(userID)};

        Cursor c = context.getContentResolver().query(FoodonetDBProvider.RegisteredUsersDB.CONTENT_URI,projection,where,whereArgs,null);
        boolean found = false;
        if(c!=null && c.moveToNext()){
            found = true;
        }
        if(c!=null){
            c.close();
        }
        return found;
    }

    /** handles inserting a new registered user for a publication (typically after the user registered for a publication)
     * @param registeredUser the new RegisteredUser object to insert into the db
     * */
    public void insertRegisteredUser(RegisteredUser registeredUser){
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN,registeredUser.getPublicationID());
        values.put(FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_VERSION_COLUMN,registeredUser.getPublicationVersion());
        values.put(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ID_COLUMN,registeredUser.getCollectorUserID());
        values.put(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ACTIVE_DEVICE_UUID_COLUMN,registeredUser.getActiveDeviceDevUUID());
        values.put(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_NAME_COLUMN,registeredUser.getCollectorName());
        values.put(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_PHONE_COLUMN,registeredUser.getCollectorContactInfo());

        resolver.insert(FoodonetDBProvider.RegisteredUsersDB.CONTENT_URI,values);
    }

    /**
     * handles replacing all registered users in db by deleting the old data, then inserting the new data, happens whenever new data is queried from the server
     * @param registeredUsers new ArrayList of the registered users data received from the foodonet server
     * */
    public void replaceAllRegisteredUsers(ArrayList<RegisteredUser> registeredUsers){
        // first, delete all data
        deleteAllRegisteredUsers();

        ContentResolver resolver = context.getContentResolver();
        ContentValues values;
        RegisteredUser registeredUser;

        for(int i = 0; i < registeredUsers.size(); i++) {
            values = new ContentValues();
            registeredUser = registeredUsers.get(i);

            values.put(FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN,registeredUser.getPublicationID());
            values.put(FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_VERSION_COLUMN,registeredUser.getPublicationVersion());
            values.put(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ID_COLUMN,registeredUser.getCollectorUserID());
            values.put(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ACTIVE_DEVICE_UUID_COLUMN,registeredUser.getActiveDeviceDevUUID());
            values.put(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_NAME_COLUMN,registeredUser.getCollectorName());
            values.put(FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_PHONE_COLUMN,registeredUser.getCollectorContactInfo());

            resolver.insert(FoodonetDBProvider.RegisteredUsersDB.CONTENT_URI,values);
        }
    }

    /**
     * delete the user from the registered users db of the publication specified (after un-registering)
     * @param publicationID the ID of the publication to remove the user from
     * */
    public void deleteRegisteredUser(long publicationID){
        ContentResolver resolver = context.getContentResolver();
        String where = String.format("%1$s = ? AND %2$s = ?",
                FoodonetDBProvider.RegisteredUsersDB.PUBLICATION_ID_COLUMN,
                FoodonetDBProvider.RegisteredUsersDB.REGISTERED_USER_ID_COLUMN);
        String[] whereArgs = {String.valueOf(publicationID),String.valueOf(CommonMethods.getMyUserID(context))};
        resolver.delete(FoodonetDBProvider.RegisteredUsersDB.CONTENT_URI,where,whereArgs);
    }

    /** deletes all registered users from the db*/
    private void deleteAllRegisteredUsers(){
        context.getContentResolver().delete(FoodonetDBProvider.RegisteredUsersDB.CONTENT_URI,null,null);
    }
}
