package com.foodonet.foodonet.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.model.NotificationFoodonet;
import java.util.ArrayList;

public class NotificationsDBHandler{
    private Context context;

    public NotificationsDBHandler(Context context) {
        this.context = context;
    }

    /**
     * queries all notifications in db
     * if a notification is no longer valid (older than two weeks) deletes it
     * @return list of all notifications in db
     */
    public ArrayList<NotificationFoodonet> getAllNotifications(){
        ArrayList<NotificationFoodonet> notifications = new ArrayList<>();
        ArrayList<Long> notificationsToDelete = new ArrayList<>();
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.NotificationsDB.CONTENT_URI,null,null,null,FoodonetDBProvider.NotificationsDB._ID_COLUMN+" DESC");
        long itemID, _id;
        int notificationType;
        String notificationName, notificationImageFileName;
        double notificationReceivedTime;
        while(c!= null && c.moveToNext()){
            notificationReceivedTime = c.getDouble(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_RECEIVED_TIME_COLUMN));
            _id = c.getLong(c.getColumnIndex(FoodonetDBProvider.NotificationsDB._ID_COLUMN));
            if(CommonMethods.getCurrentTimeSeconds() - notificationReceivedTime >= CommonConstants.CLEAR_NOTIFICATIONS_TIME_SECONDS){
                notificationsToDelete.add(_id);
            } else{
                itemID = c.getLong(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.ITEM_ID_COLUMN));
                notificationType = c.getInt(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_TYPE_COLUMN));
                notificationName = c.getString(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_NAME_COLUMN));
                notificationImageFileName = c.getString(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_IMAGE_FILE_NAME_COLUMN));

                notifications.add(new NotificationFoodonet(notificationType,itemID,notificationName,notificationReceivedTime,notificationImageFileName));
            }
        }
        if(notificationsToDelete.size() != 0){
            deleteNotifications(notificationsToDelete);
        }
        if(c!= null){
            c.close();
        }
        return notifications;
    }

    /**
     * get the notifications images file names of publication based notifications (not group member updates)
     * @return list of publications notifications images file names
     */
    public ArrayList<String> getNotificationPublicationImagesFileNames() {
        ArrayList<String> notificationsImagesFileNames = new ArrayList<>();
        String[] projection = {FoodonetDBProvider.NotificationsDB.NOTIFICATION_IMAGE_FILE_NAME_COLUMN};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.NotificationsDB.CONTENT_URI,projection,null,null,null);
        while(c!= null && c.moveToNext()){
            notificationsImagesFileNames.add(c.getString(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_IMAGE_FILE_NAME_COLUMN)));
        }
        if(c!= null){
            c.close();
        }
        return notificationsImagesFileNames;
    }

    /**
     * queries the notifications db for all unread notifications by issuing the last notification id that was read by the user, all later notifications will be returned
     * @param loadNotificationsFromID the last notification that was read (or dismissed) by the user, as saved in shared preferences
     * @return ArrayList of NotificationFoodonet of all unread notifications
     */
    public ArrayList<NotificationFoodonet> getUnreadNotifications(long loadNotificationsFromID) {
        ArrayList<NotificationFoodonet> notifications = new ArrayList<>();
        if(loadNotificationsFromID < 0){
            return notifications;
        }
        ArrayList<Long> notificationsToDelete = new ArrayList<>();
        String selection = String.format("%1$s >= ?", FoodonetDBProvider.NotificationsDB._ID_COLUMN);
        String[] selectionArgs = {String.valueOf(loadNotificationsFromID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.NotificationsDB.CONTENT_URI,
                null, selection, selectionArgs,FoodonetDBProvider.NotificationsDB._ID_COLUMN+" DESC");

        long itemID, _id;
        int notificationType;
        String notificationName, notificationImageFileName;
        double notificationReceivedTime;
        while(c!= null && c.moveToNext()){
            notificationReceivedTime = c.getDouble(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_RECEIVED_TIME_COLUMN));
            _id = c.getLong(c.getColumnIndex(FoodonetDBProvider.NotificationsDB._ID_COLUMN));
            if(CommonMethods.getCurrentTimeSeconds() - notificationReceivedTime >= CommonConstants.CLEAR_NOTIFICATIONS_TIME_SECONDS){
                notificationsToDelete.add(_id);
            } else{
                itemID = c.getLong(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.ITEM_ID_COLUMN));
                notificationType = c.getInt(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_TYPE_COLUMN));
                notificationName = c.getString(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_NAME_COLUMN));
                notificationImageFileName = c.getString(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_IMAGE_FILE_NAME_COLUMN));

                notifications.add(new NotificationFoodonet(notificationType,itemID,notificationName,notificationReceivedTime,notificationImageFileName));
            }
        }
        if(notificationsToDelete.size() != 0){
            deleteNotifications(notificationsToDelete);
        }
        if(c!= null){
            c.close();
        }
        return notifications;
    }

    /**
     * handles inserting new notification to the db
     * @param notification NotificationFoodonet notification to insert
     */
    public void insertNotification(NotificationFoodonet notification){
        ContentValues values = new ContentValues();
        values.put(FoodonetDBProvider.NotificationsDB.ITEM_ID_COLUMN,notification.getItemID());
        values.put(FoodonetDBProvider.NotificationsDB.NOTIFICATION_TYPE_COLUMN,notification.getTypeNotification());
        values.put(FoodonetDBProvider.NotificationsDB.NOTIFICATION_NAME_COLUMN,notification.getNameNotification());
        values.put(FoodonetDBProvider.NotificationsDB.NOTIFICATION_RECEIVED_TIME_COLUMN,notification.getReceivedTime());
        values.put(FoodonetDBProvider.NotificationsDB.NOTIFICATION_IMAGE_FILE_NAME_COLUMN,notification.getImageFileName());
        long _id = ContentUris.parseId(context.getContentResolver().insert(FoodonetDBProvider.NotificationsDB.CONTENT_URI,values));
        CommonMethods.updateUnreadNotificationID(context,_id);
    }

    /**
     * handles deleting a list of notifications from the db
     * @param notificationsToDelete ArrayList - Long of notifications IDs to delete
     */
    private void deleteNotifications(ArrayList<Long> notificationsToDelete){
        for(int i = 0; i < notificationsToDelete.size(); i++){
            deleteNotification(notificationsToDelete.get(i));
        }
    }

    /**
     * handles deleting a specific notification from the db
     * @param _id NotificationFoodonet the notification to delete
     */
    private void deleteNotification(long _id){
        String where= String.format("%1$s = ?",FoodonetDBProvider.NotificationsDB._ID_COLUMN);
        String[] whereArgs = {String.valueOf(_id)};
        context.getContentResolver().delete(FoodonetDBProvider.NotificationsDB.CONTENT_URI,where,whereArgs);
    }

    /**
     * handles deleting all notifications from the db
     */
    public void deleteAllNotification(){
        context.getContentResolver().delete(FoodonetDBProvider.NotificationsDB.CONTENT_URI,null,null);
    }
}
