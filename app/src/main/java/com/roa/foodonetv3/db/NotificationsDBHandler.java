package com.roa.foodonetv3.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.NotificationFoodonet;
import java.util.ArrayList;

public class NotificationsDBHandler{

    private static final String TAG = "NotificationsDBHandler";

    private Context context;

    public NotificationsDBHandler(Context context) {
        this.context = context;
    }

    public ArrayList<NotificationFoodonet> getAllNotifications(){
        ArrayList<NotificationFoodonet> notifications = new ArrayList<>();
        ArrayList<Long> notificationsToDelete = new ArrayList<>();
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.NotificationsDB.CONTENT_URI,null,null,null,FoodonetDBProvider.NotificationsDB._ID_COLUMN+" DESC");
        long itemID, _id;
        int notificationType;
        String notificationName;
        double notificationReceivedTime;
        while(c!= null && c.moveToNext()){
            notificationReceivedTime = c.getDouble(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_RECEIVED_TIME));
            _id = c.getLong(c.getColumnIndex(FoodonetDBProvider.NotificationsDB._ID_COLUMN));
            if(CommonMethods.getCurrentTimeSeconds() - notificationReceivedTime >= CommonConstants.CLEAR_NOTIFICATIONS_TIME_SECONDS){
                notificationsToDelete.add(_id);
            } else{
                itemID = c.getLong(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.ITEM_ID));
                notificationType = c.getInt(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_TYPE));
                notificationName = c.getString(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_NAME));

                notifications.add(new NotificationFoodonet(notificationType,itemID,notificationName,notificationReceivedTime));
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

    public ArrayList<NotificationFoodonet> getUnreadNotification(long loadNotificationsFromID) {
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
        String notificationName;
        double notificationReceivedTime;
        while(c!= null && c.moveToNext()){
            notificationReceivedTime = c.getDouble(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_RECEIVED_TIME));
            _id = c.getLong(c.getColumnIndex(FoodonetDBProvider.NotificationsDB._ID_COLUMN));
            if(CommonMethods.getCurrentTimeSeconds() - notificationReceivedTime >= CommonConstants.CLEAR_NOTIFICATIONS_TIME_SECONDS){
                notificationsToDelete.add(_id);
            } else{
                itemID = c.getLong(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.ITEM_ID));
                notificationType = c.getInt(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_TYPE));
                notificationName = c.getString(c.getColumnIndex(FoodonetDBProvider.NotificationsDB.NOTIFICATION_NAME));

                notifications.add(new NotificationFoodonet(notificationType,itemID,notificationName,notificationReceivedTime));
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

    public void insertNotification(NotificationFoodonet notification){
        ContentValues values = new ContentValues();
        values.put(FoodonetDBProvider.NotificationsDB.ITEM_ID,notification.getItemID());
        values.put(FoodonetDBProvider.NotificationsDB.NOTIFICATION_TYPE,notification.getTypeNotification());
        values.put(FoodonetDBProvider.NotificationsDB.NOTIFICATION_NAME,notification.getNameNotification());
        values.put(FoodonetDBProvider.NotificationsDB.NOTIFICATION_RECEIVED_TIME,notification.getReceivedTime());
        long _id = ContentUris.parseId(context.getContentResolver().insert(FoodonetDBProvider.NotificationsDB.CONTENT_URI,values));
        CommonMethods.updateUnreadNotificationID(context,_id);
    }

    private void deleteNotifications(ArrayList<Long> notificationsToDelete){
        for(int i = 0; i < notificationsToDelete.size(); i++){
            deleteNotification(notificationsToDelete.get(i));
        }
    }

    private void deleteNotification(long _id){
        String where= String.format("%1$s = ?",FoodonetDBProvider.NotificationsDB._ID_COLUMN);
        String[] whereArgs = {String.valueOf(_id)};
        context.getContentResolver().delete(FoodonetDBProvider.NotificationsDB.CONTENT_URI,where,whereArgs);
    }

    public void deleteAllNotification(){
        context.getContentResolver().delete(FoodonetDBProvider.NotificationsDB.CONTENT_URI,null,null);
    }

}
