package com.roa.foodonetv3.commonMethods;

/** various common constants */
public class CommonConstants {
    public static final long FAB_ANIM_DURATION = 600;

    public static final short VALUE_FALSE = 0;
    public static final short VALUE_TRUE = 1;

    public static final long NON_FOODONET_MEMBER_ID = 0;

    public static final double LATLNG_ERROR = -9999;

    public static final int SPLASH_CAMERA_TIME = 1500;

    public static final int NUMBER_OF_LATEST_SEARCHES = 5;

    public static final short REPORT_TYPE_HAS_MORE = 1;
    public static final short REPORT_TYPE_TOOK_ALL = 3;
    public static final short REPORT_TYPE_NOTHING_THERE = 5;

    /** HTTP_TYPES */
    public static final int HTTP_GET = 1;
    public static final int HTTP_POST = 2;
    public static final int HTTP_PUT = 3;
    public static final int HTTP_DELETE = 4;

    /** map zoom */
    public static final float ZOOM_OUT = 9;
    public static final float ZOOM_IN = 13.6f;

    public static final int DEFAULT_NOTIFICATION_RADIUS_ITEM = 2;
    public static final double CLEAR_NOTIFICATIONS_TIME_SECONDS = 604800; // 1 week 604800
    public static final long NOTIFICATION_ID_CLEAR = -1;

    public static final long UNINITIALIZED_USER_ID = -1;
    // as named by server push
    public static final String NOTIF_TYPE_NEW_PUBLICATION = "new_publication";
    public static final String NOTIF_TYPE_DELETED_PUBLICATION = "deleted_publication";
    public static final String NOTIF_TYPE_REGISTRATION_FOR_PUBLICATION = "registration_for_publication";
    public static final String NOTIF_TYPE_PUBLICATION_REPORT = "publication_report";
    public static final String NOTIF_TYPE_GROUP_MEMBERS = "group_members";
    public static final String NOTIF_ACTION_DISMISS = "notif_action_dismiss";
    public static final String NOTIF_ACTION_OPEN = "notif_action_open";

    public static final int PUBLICATION_SORT_TYPE_RECENT = 1;
    public static final int PUBLICATION_SORT_TYPE_CLOSEST = 2;
    public static final String MY_USER_ID_IMAGE_FILE_NAME = "User.jpg";

    // file provider folders, they are also hard coded in the file_paths.xml, if changing one, change the other as well
    public static final String FILE_TYPE_PUBLICATIONS = "publications images";
    public static final String FILE_TYPE_USERS = "users images";
    public static final int TIME_TYPE_REMAINING = 1;
    public static final int TIME_TYPE_AGO = 2;
    public static final String LOCATION_TYPE_LOCATION_DISABLED = "location_disabled";
    public static final String LOCATION_TYPE_NEEDS_PERMISSION = "location_permission_needed";

    public static final int PERMISSION_REQUEST_LOCATION = 1;

    public static final long UP_TO_DATE_PERIOD_MILLIS = 300000; //5 minutes
    public static final long TIME_SWITCH_TO_FUSED_MILLIS = 1000; // 1 second
}
