package com.foodonet.foodonet.model;

import android.content.Context;

import com.foodonet.foodonet.R;

public class NotificationFoodonet {

    public static final int NOTIFICATION_TYPE_NEW_PUBLICATION = 1;
    public static final int NOTIFICATION_TYPE_PUBLICATION_DELETED = 2;
    public static final int NOTIFICATION_TYPE_NEW_REGISTERED_USER = 3;
    public static final int NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT = 4;
    public static final int NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP = 5;

    private int typeNotification;
    private String nameNotification;
    private long itemID;
    private double receivedTime;
    private String imageFileName;

    public NotificationFoodonet(int typeNotification, long itemID, String nameNotification, double receivedTime, String imageFileName) {
        this.typeNotification = typeNotification;
        this.itemID = itemID;
        this.nameNotification = nameNotification;
        this.receivedTime = receivedTime;
        this.imageFileName = imageFileName;
    }

    public int getTypeNotification() {
        return typeNotification;
    }

    public long getItemID() {
        return itemID;
    }

    public String getNameNotification() {
        return nameNotification;
    }

    public double getReceivedTime() {
        return receivedTime;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    /**
     * handles getting the type of notification as string
     * @return String of the type of notification
     */
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
                return context.getString(R.string.notification_group_members);
        }
        return "";
    }

    /**
     * handles getting the notification message to display
     * @return String of the message to display
     */
    public String getNotificationMessage(Context context){
        switch (typeNotification){
            case NOTIFICATION_TYPE_NEW_PUBLICATION:
                return String.format("%1$s %2$s",
                        context.getString(R.string.notification_new_event),
                        nameNotification);
            case NOTIFICATION_TYPE_PUBLICATION_DELETED:
                return String.format("%1$s %2$s",
                        context.getString(R.string.notification_publication_deleted),
                        nameNotification);
            case NOTIFICATION_TYPE_NEW_REGISTERED_USER:
                return String.format("%1$s %2$s",
                        context.getString(R.string.notification_new_registered_user),
                        nameNotification);
            case NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT:
                return String.format("%1$s %2$s, %3$s",
                        context.getString(R.string.notification_new_publication_report),
                        nameNotification,
                        context.getString(R.string.got_a_new_report));
            case NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP:
                return String.format("%1$s %2$s",
                        context.getString(R.string.notification_group_members),
                        nameNotification);
        }
        return "";
    }

    /**
     * handles getting the image of the notification type
     * @return the resource of the drawable of the type of notification
     */
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

    /**
     * handles getting the notification text color
     * @return int of the color to use on the text
     */
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
