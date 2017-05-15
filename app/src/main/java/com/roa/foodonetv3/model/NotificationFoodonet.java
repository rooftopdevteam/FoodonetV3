package com.roa.foodonetv3.model;

import android.content.Context;

import com.roa.foodonetv3.R;

public class NotificationFoodonet {
    private static final String TAG = "NotificationFoodonet";

    public static final int NOTIFICATION_TYPE_NEW_PUBLICATION = 1;
    public static final int NOTIFICATION_TYPE_PUBLICATION_DELETED = 2;
    public static final int NOTIFICATION_TYPE_NEW_REGISTERED_USER = 3;
    public static final int NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT = 4;
    public static final int NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP = 5;

    private int typeNotification;
    private String nameNotification;
    private long itemID;
    private double receivedTime;

    public NotificationFoodonet(int typeNotification, long itemID, String nameNotification, double receivedTime) {
        this.typeNotification = typeNotification;
        this.itemID = itemID;
        this.nameNotification = nameNotification;
        this.receivedTime = receivedTime;
    }

    public int getTypeNotification() {
        return typeNotification;
    }

    public void setTypeNotification(int typeNotification) {
        this.typeNotification = typeNotification;
    }

    public long getItemID() {
        return itemID;
    }

    public void setItemID(long itemID) {
        this.itemID = itemID;
    }

    public String getNameNotification() {
        return nameNotification;
    }

    public void setNameNotification(String nameNotification) {
        this.nameNotification = nameNotification;
    }

    public double getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(double timeMillisNotification) {
        this.receivedTime = timeMillisNotification;
    }

    public String getTypeNotificationString(Context context){
        switch (typeNotification){
            case NOTIFICATION_TYPE_NEW_PUBLICATION:
                return context.getString(R.string.notification_new_event);
            case NOTIFICATION_TYPE_PUBLICATION_DELETED:
                return context.getString(R.string.notification_publication_deleted);
            case NOTIFICATION_TYPE_NEW_REGISTERED_USER:
                return context.getString(R.string.notification_new_registered_user);
            case NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT:
                return context.getString(R.string.notification_new_publication_report);
            case NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP:
                return context.getString(R.string.notification_new_added_in_group);
        }
        return "";
    }

    public String getNotificationMessage(Context context){
        switch (typeNotification){
            case NOTIFICATION_TYPE_NEW_PUBLICATION:
                return String.format("%1$s %2$s",context.getString(R.string.notification_new_event),nameNotification);
            case NOTIFICATION_TYPE_PUBLICATION_DELETED:
                return String.format("%1$s %2$s", context.getString(R.string.notification_publication_deleted), nameNotification);
            case NOTIFICATION_TYPE_NEW_REGISTERED_USER:
                return String.format("%1$s %2$s",context.getString(R.string.notification_new_registered_user),
                        nameNotification);
            case NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT:
                return String.format("%1$s %2$s, %3$s",context.getString(R.string.notification_new_publication_report),nameNotification,
                        context.getString(R.string.got_a_new_report));
            case NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP:
                return context.getString(R.string.notification_added_to_group);
        }
        return "";
    }

    public int getNotificationTypeImageResource(){
        switch (typeNotification){
            case NOTIFICATION_TYPE_NEW_PUBLICATION:
                return R.drawable.notif_new;
            case NOTIFICATION_TYPE_PUBLICATION_DELETED:
                return R.drawable.notif_ended;
            case NOTIFICATION_TYPE_NEW_REGISTERED_USER:
                return R.drawable.notif_join;
            case NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT:
                return R.drawable.notif_report;
            case NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP:
                return R.drawable.notif_group;
        }
        return -1;
    }

    public int getNotificationTextColor(){
        switch (typeNotification){
            case NOTIFICATION_TYPE_NEW_PUBLICATION:
                return R.color.fooLightBlue;
            case NOTIFICATION_TYPE_PUBLICATION_DELETED:
                return R.color.fooRed;
            case NOTIFICATION_TYPE_NEW_REGISTERED_USER:
                return R.color.fooGreen;
            case NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT:
                return R.color.fooPurple;
            case NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP:
                return R.color.fooYellow;
        }
        return -1;
    }
}
