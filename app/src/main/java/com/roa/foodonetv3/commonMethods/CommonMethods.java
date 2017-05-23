package com.roa.foodonetv3.commonMethods;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.roa.foodonetv3.activities.NotificationActivity;
import com.roa.foodonetv3.db.NotificationsDBHandler;
import com.roa.foodonetv3.dialogs.ContactUsDialog;
import com.roa.foodonetv3.activities.GroupsActivity;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.AboutUsActivity;
import com.roa.foodonetv3.activities.MainActivity;
import com.roa.foodonetv3.activities.MapActivity;
import com.roa.foodonetv3.activities.PrefsActivity;
import com.roa.foodonetv3.activities.PublicationActivity;
import com.roa.foodonetv3.activities.SignInActivity;
import com.roa.foodonetv3.model.GroupMember;
import com.roa.foodonetv3.model.NotificationFoodonet;
import com.roa.foodonetv3.serverMethods.ServerMethods;
import com.roa.foodonetv3.services.GetDataService;
import com.roa.foodonetv3.services.GetLocationService;
import com.roa.foodonetv3.services.NotificationsDismissService;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;

import static android.content.Context.LOCATION_SERVICE;

public class CommonMethods {
    private static final String TAG = "CommonMethods";

    /**
     * we only need one instance of the clients and credentials provider
     */
    private static AmazonS3Client sS3Client;
    private static CognitoCachingCredentialsProvider sCredProvider;
    private static TransferUtility sTransferUtility;

    public static void navigationItemSelectedAction(Context context, int id) {
        /** handles the navigation actions from the drawer*/
        Intent intent;
        switch (id) {
            case R.id.nav_my_shares:
                intent = new Intent(context, PublicationActivity.class);
                if (!(context instanceof MainActivity)) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                intent.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION, PublicationActivity.MY_PUBLICATIONS_TAG);
                context.startActivity(intent);
                if (!(context instanceof MainActivity)) {
                    ((Activity) context).finish();
                }
                break;
            case R.id.nav_all_events:
                if (!(context instanceof MainActivity)) {
                    intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    break;
                }
                break;
            case R.id.nav_map_view:
                intent = new Intent(context, MapActivity.class);
                if (context instanceof MainActivity) {
                    context.startActivity(intent);
                } else {
                    context.startActivity(intent);
                    ((Activity) context).finish();

                }
                break;
            case R.id.nav_notifications:
                intent = new Intent(context, NotificationActivity.class);
//                if(context instanceof  NotificationActivity){
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    context.startActivity(intent);
//                } else{
                context.startActivity(intent);
//                }

                break;
            case R.id.nav_groups:
                intent = new Intent(context, GroupsActivity.class);
                if (context instanceof GroupsActivity || context instanceof MainActivity) {
                    // TODO: 06/12/2016 test
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                } else {
                    context.startActivity(intent);
                }
                break;
            case R.id.nav_settings:
                if (getMyUserID(context) == -1) {
                    /** if the user is not signed in yet, open the sign in activity */
                    intent = new Intent(context, SignInActivity.class);
                } else {
                    /** open the preferences fragment activity */
                    intent = new Intent(context, PrefsActivity.class);
                }
                context.startActivity(intent);
                break;
            case R.id.nav_contact_us:
                ContactUsDialog dialog = new ContactUsDialog(context);
                dialog.show();
                break;
            case R.id.nav_about:
                intent = new Intent(context, AboutUsActivity.class);
                context.startActivity(intent);
                break;
        }
    }

    /**
     * returns current epoch time in seconds(NOT MILLIS!)
     */
    public static double getCurrentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static String getCurrentTimeSecondsString(){
        return BigInteger.valueOf(System.currentTimeMillis()/1000).toString();
    }

    /**
     * returns a string of time difference between two times in epoch time seconds (NOT MILLIS!) with a changing perspective according to the duration
     */
    public static String getTimeDifference(Context context, Double earlierTimeInSeconds, Double laterTimeInSeconds, String suffix) {
        long timeDiff = (long) (laterTimeInSeconds - earlierTimeInSeconds) / 60; // minutes as start
        StringBuilder message = new StringBuilder();
        if (timeDiff < 0) {
            return "N/A";
        } else if (timeDiff < 1440) {
            /** hours, minutes */
            if (timeDiff / 60 != 0) {
                message.append(String.format(Locale.US, "%1$d%2$s ", timeDiff / 60, context.getResources().getString(R.string.h_hours)));
            }
            message.append(String.format(Locale.US, "%1$d%2$s", timeDiff % 60, context.getResources().getString(R.string.min_minutes)));
        } else {
            /** days, hours */
            long days = timeDiff / 1440;
            String daysString;
            if (days == 1) {
                daysString = context.getResources().getString(R.string.day);
            } else {
                daysString = context.getResources().getString(R.string.days);
            }
            message.append(String.format(Locale.US, "%1$d %2$s ", days, daysString));
            if (timeDiff < 10080) {
                /** only add hours if the difference is less than a week, otherwise just show days */
                message.append(String.format(Locale.US, "%1$d%2$s", (timeDiff % 1440) / 60, context.getResources().getString(R.string.h_hours)));
            }
        }
        if(suffix!= null){
            message.append(String.format(" %1$s", suffix));
        }
        return message.toString();
    }

    public static boolean isUserGroupAdmin(Context context, ArrayList<GroupMember> members) {
        long userID = getMyUserID(context);
        GroupMember member;
        for (int i = 0; i < members.size(); i++) {
            member = members.get(i);
            if (member.getUserID() == userID) {
                return member.isAdmin();
            }
        }
        return false;
    }

    /**
     * get the message according to the server specified report type
     */
    public static String getReportStringFromType(Context context, int typeOfReport) {
        switch (typeOfReport) {
            case CommonConstants.REPORT_TYPE_HAS_MORE:
                return context.getResources().getString(R.string.report_has_more_to_offer);
            case CommonConstants.REPORT_TYPE_TOOK_ALL:
                return context.getResources().getString(R.string.report_took_all);
            case CommonConstants.REPORT_TYPE_NOTHING_THERE:
                return context.getResources().getString(R.string.report_found_nothing_there);
        }
        return null;
    }

    public static void getNewData(Context context) {
        Intent getDataIntent = new Intent(context, GetDataService.class);
        getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE, ReceiverConstants.ACTION_GET_DATA);
        context.startService(getDataIntent);
        CommonMethods.setLastUpdated(context);
    }

    public static void signOffUser(Context context){
        Intent intent = new Intent(context,GetDataService.class);
        intent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_SIGN_OUT);
        context.startService(intent);
    }

    public static boolean isMyUserInitialized(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null && CommonMethods.getMyUserID(context) != CommonConstants.UNINITIALIZED_USER_ID){
            if(isMyUserImagePresent(context)){
                return true;
            } else{
                ServerMethods.getMyUserImage(context);
            }
        }
        return false;
    }

    public static boolean isMyUserImagePresent(Context context){
        String filePath = CommonMethods.getMyUserImageFilePath(context);
        if(filePath != null){
            File imageFile = new File(filePath);
            if(imageFile.isFile()){
                return true;
            }
        }
        return false;
    }

    /**
     * returns a UUID
     */
    public static String getDeviceUUID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_prefs_device_uuid), null);
    }

    /**
     * returns the userID from shared preferences
     */
    public static long getMyUserID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getLong(context.getString(R.string.key_prefs_user_id), CommonConstants.UNINITIALIZED_USER_ID);
    }

    /**
     * @return returns the userName from shared preferences
     */
    public static String getMyUserName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_prefs_user_name), "");
    }

    /**
     * saves the userID to shared preferences
     */
    public static void setMyUserID(Context context, long userID) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(context.getString(R.string.key_prefs_user_id), userID).apply();
    }

    public static String getMyUserPhone(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_prefs_user_phone), null);
    }

    public static void setLastLocation(Context context, LatLng latlng){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString(context.getString(R.string.key_prefs_user_lat),String.valueOf(latlng.latitude))
                .putString(context.getString(R.string.key_prefs_user_lng),String.valueOf(latlng.longitude))
                .apply();
    }

    public static void setLastUpdated(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putLong(context.getString(R.string.key_prefs_last_updated),System.currentTimeMillis()).apply();
    }

    public static boolean isDataUpToDate(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long lastUpdate = preferences.getLong(context.getString(R.string.key_prefs_last_updated),-1);
        return !(lastUpdate == -1 || System.currentTimeMillis() - lastUpdate > CommonConstants.UP_TO_DATE_PERIOD_MILLIS);
    }

    public static LatLng getLastLocation(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new LatLng(Double.valueOf(preferences.getString(context.getString(R.string.key_prefs_user_lat), String.valueOf(CommonConstants.LATLNG_ERROR))),
                Double.valueOf(preferences.getString(context.getString(R.string.key_prefs_user_lng), String.valueOf(CommonConstants.LATLNG_ERROR))));
    }

    public static boolean isNotificationTurnedOn(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(context.getString(R.string.key_prefs_get_notifications),true);
    }

    /**
     * should increment negatively for a unique id until the server gives us a server unique publication id to replace it
     */
    public static long getNewLocalPublicationID() {
        //todo add a check for available negative id, currently hard coded
        return (long) -1;
    }

    public static String getDigitsFromPhone(String origin) {
        return origin.replaceAll("[^0-9]", "");
    }

    public static boolean comparePhoneNumbers(String first, String second) {
        first = removeInternationalPhoneCode(getDigitsFromPhone(first));
        second = removeInternationalPhoneCode(getDigitsFromPhone(second));
        return PhoneNumberUtils.compare(first, second);
    }

    private static String removeInternationalPhoneCode(String phone) {
        if (phone.startsWith("972")) {
            phone = 0 + phone.substring(3);
        }
        return phone;
    }

    public static String getRoundedStringFromNumber(float num) {
        DecimalFormat df = new DecimalFormat("####0.00");
        return df.format(num);
    }

    public static String getRoundedStringFromNumber(double num) {
        DecimalFormat df = new DecimalFormat("####0.00");
        return df.format(num);
    }

    public static String getNoDecimalStringFromNumber(double num){
        DecimalFormat df = new DecimalFormat("#");
        df.setMaximumFractionDigits(0);
        return df.format(num);
    }

    /** sends all new messages from the db as notifications to the user */
    public static void sendNotification(Context context) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long loadNotificationsFromID = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.key_prefs_unread_notification_id), CommonConstants.NOTIFICATION_ID_CLEAR);
        NotificationsDBHandler notificationsDBHandler = new NotificationsDBHandler(context);
        ArrayList<NotificationFoodonet> notificationsToDisplay = notificationsDBHandler.getUnreadNotification(loadNotificationsFromID);

        Intent resultIntent = new Intent(context, NotificationsDismissService.class);
        resultIntent.setAction(CommonConstants.NOTIF_ACTION_OPEN);
        PendingIntent resultPendingIntent = PendingIntent.getService(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissNotificationsIntent = new Intent(context, NotificationsDismissService.class);
        dismissNotificationsIntent.setAction(CommonConstants.NOTIF_ACTION_DISMISS);
        PendingIntent dismissNotificationsPendingIntent = PendingIntent.getService(context, 0, dismissNotificationsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        int notificationsToDisplaySize = notificationsToDisplay.size();
        String contentText;
        if(notificationsToDisplaySize >1){
            contentText = String.format("%1$s %2$s",String.valueOf(notificationsToDisplaySize),context.getString(R.string.new_messages));
        } else{
            contentText = context.getString(R.string.new_message);
        }
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.foodonet))
                .setContentText(contentText)
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setDeleteIntent(dismissNotificationsPendingIntent)
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.drawable.status_bar)
                .setGroup("foodonet")
                .setGroupSummary(true);
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle(context.getString(R.string.foodonet))
                .setSummaryText(contentText);
        NotificationFoodonet notification;
        for(int i = 0; i < notificationsToDisplaySize && i < 7; i++){
            if(i== 6){
                inboxStyle.addLine(String.format("+ %1$s",String.valueOf(notificationsToDisplaySize - 6)));
            } else{
                notification = notificationsToDisplay.get(i);
                inboxStyle.addLine(notification.getNotificationMessage(context));
            }
        }
        mBuilder.setStyle(inboxStyle);

        mNotificationManager.notify(1, mBuilder.build());
    }

    /** updates the shared preferences value of the latest unread notification, or clear it (all read)
     * @param _id - local _id variable to update, CommonConstants.NOTIFICATION_ID_CLEAR to set as cleared
     */
    public static void updateUnreadNotificationID(Context context, long _id){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String keyPrefsUnreadNotificationID = context.getString(R.string.key_prefs_unread_notification_id);
        if(_id == CommonConstants.NOTIFICATION_ID_CLEAR ||
                sharedPreferences.getLong(keyPrefsUnreadNotificationID,CommonConstants.NOTIFICATION_ID_CLEAR) == CommonConstants.NOTIFICATION_ID_CLEAR){
            sharedPreferences.edit().putLong(keyPrefsUnreadNotificationID,_id).apply();
        }
    }

    /** @deprecated currently trying to update returns a 404, disabling for now */
    public static void updateUserLocationToServer(Context context){
        JSONObject activeDeviceRoot = new JSONObject();
        JSONObject activeDevice = new JSONObject();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            activeDevice.put("dev_uuid",CommonMethods.getDeviceUUID(context));
            activeDevice.put("last_location_latitude", preferences.getString(context.getString(R.string.key_prefs_user_lat), String.valueOf(CommonConstants.LATLNG_ERROR)));
            activeDevice.put("last_location_longitude", preferences.getString(context.getString(R.string.key_prefs_user_lng),String.valueOf(CommonConstants.LATLNG_ERROR)));
            activeDeviceRoot.put("active_device",activeDevice);
            ServerMethods.activeDeviceUpdateLocation(context,activeDeviceRoot.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference.
     * Uses Haversine method as its base. Distance in Meters
     */
    public static double distance(double lat1, double lng1, double lat2, double lng2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (double) R * c;
    }

    public static int[] getListIndexSortedValues(ArrayList<Double> list){
        int length = list.size();
        TreeMap<Integer,Double> listMap = new TreeMap<>();
        for (int i = 0; i < length; i++) {
            listMap.put(i,list.get(i));
        }
        double num;
        double lastMin = -1;
        double min;
        int minIndex;
        int lastMinIndex = -1;
        int[] sortedIndex = new int[length];
        for (int i = 0; i < length; i++) {
            min = -1;
            minIndex = -1;
            for (int j = 0; j < length; j++) {
                num = listMap.get(j);
                if(min == -1 && num > lastMin){
                    min = num;
                    minIndex = j;
                }
                if(num == lastMin){
                    if(j > lastMinIndex){
                        minIndex = j;
                        min = lastMin;
                        break;
                    }
                }
                if(num < min && num > lastMin){
                    min = num;
                    minIndex = j;
                }
            }
            lastMin = min;
            lastMinIndex = minIndex;
            sortedIndex[i] = minIndex;
        }
        return sortedIndex;
    }

    /** @return File Creates a local image file name for taking the picture with the camera */
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(CommonConstants.FILE_TYPE_PUBLICATIONS);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */);
    }

    /** @return String file name from publicationID */
    public static String getFileNameFromPublicationID(long publicationID,int version){
        return String.format(Locale.US,"%1$d.%2$d.jpg",
                publicationID,version);
    }

    /** @return String file name from userID */
    public static String getFileNameFromUserID(Context context, long userID){
        return String.format(Locale.US,"%1$s%2$d.jpg",
                context.getString(R.string.amazon_user_image_prefix),userID);
    }

    public static String getFilePathFromFileName(Context context, String imageFileName, String fileType){
        File directory = (context.getExternalFilesDir(fileType));
        if(directory!= null && imageFileName != null){
            String storageDir = directory.getPath();
            return String.format(Locale.US,"%1$s/%2$s",storageDir,imageFileName);
        }
        return null;
    }

    /** Creates a local image file name for downloaded images from s3 server of a specific publication */
    public static String getFilePathFromPublicationID(Context context, long publicationID, int version) {
        String imageFileName = getFileNameFromPublicationID(publicationID,version);
        File directoryPictures = (context.getExternalFilesDir(CommonConstants.FILE_TYPE_PUBLICATIONS));
        if(directoryPictures!= null){
            String storageDir = directoryPictures.getPath();
            return String.format(Locale.US,"%1$s/%2$s",storageDir,imageFileName);
        }
        return null;
    }

    /** Creates a local image file name for a specific userID to work with s3 server */
    public static String getFilePathFromUserID(Context context, long userID){
        String imageFileName = getFileNameFromUserID(context,userID);
        File directoryUsers = context.getExternalFilesDir(CommonConstants.FILE_TYPE_USERS);
        if(directoryUsers != null){
            String storageDir = directoryUsers.getPath();
            return String.format(Locale.US,"%1$s/%2$s",storageDir,imageFileName);
        }
        return null;
    }

    public static String getMyUserImageFilePath(Context context){
        String imageFileName = CommonConstants.MY_USER_ID_IMAGE_FILE_NAME;
        File directoryUsers = context.getExternalFilesDir(CommonConstants.FILE_TYPE_USERS);
        if(directoryUsers != null){
            String storageDir = directoryUsers.getPath();
            return String.format(Locale.US,"%1$s/%2$s",storageDir,imageFileName);
        }
        return null;
    }

    public static String compressImage(Context context, Uri uri, String photoPath) throws FileNotFoundException {
        /** ratio - 16:9 */
        final float ratio = 16 / 9f;
        final int WANTED_HEIGHT = 560;
        final int WANTED_WIDTH = (int) (WANTED_HEIGHT * ratio);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if(uri != null){
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),null,options);
        } else{
            BitmapFactory.decodeFile(photoPath,options);
        }
        int originHeight = options.outHeight;
        int originWidth = options.outWidth;
        int scale = 1;
        while(true) {
            if(originWidth / 2 < WANTED_WIDTH || originHeight / 2 < WANTED_HEIGHT)
                break;
            originWidth /= 2;
            originHeight /= 2;
            scale *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = scale;
        Bitmap bitmap;
        if(uri!= null){
            bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri),null,options);
        } else{
            bitmap = BitmapFactory.decodeFile(photoPath,options);
        }

        /** cut the image to display as a 16:9 image */
        if (bitmap.getHeight() * ratio < bitmap.getWidth()) {
            /** full height of the image, cut the width*/
            bitmap = Bitmap.createBitmap(
                    bitmap,
                    (int) ((bitmap.getWidth() - (bitmap.getHeight() * ratio)) / 2),
                    0,
                    (int) (bitmap.getHeight() * ratio),
                    bitmap.getHeight()
            );
        } else {
            /** full width of the image, cut the height*/
            bitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    (int) ((bitmap.getHeight() - (bitmap.getWidth() / ratio)) / 2),
                    bitmap.getWidth(),
                    (int) (bitmap.getWidth() / ratio)
            );
        }
        /** scale the image down*/
        bitmap = Bitmap.createScaledBitmap(bitmap, WANTED_WIDTH, WANTED_HEIGHT, false);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(photoPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return photoPath;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return photoPath;
    }

    /**
     * Gets an instance of CognitoCachingCredentialsProvider which is
     * constructed using the given Context.
     *
     * @param context An Context instance.
     * @return A default credential provider.
     */
    private static CognitoCachingCredentialsProvider getCredProvider(Context context) {
        if (sCredProvider == null) {
            sCredProvider = new CognitoCachingCredentialsProvider(
                    context.getApplicationContext(),
                    context.getResources().getString(R.string.amazon_aws_account_id),
                    context.getResources().getString(R.string.amazon_pool_id),
                    context.getResources().getString(R.string.amazon_unauthorized),
                    context.getResources().getString(R.string.amazon_authorized),
                    Regions.US_EAST_1);
        }
        return sCredProvider;
    }

    /**
     * Gets an instance of a S3 client which is constructed using the given
     * Context.
     *
     * @param context An Context instance.
     * @return A default S3 client.
     */
    public static AmazonS3Client getS3Client(Context context) {
        if (sS3Client == null) {
            sS3Client = new AmazonS3Client(getCredProvider(context.getApplicationContext()));
        }
        return sS3Client;
    }

    /**
     * Gets an instance of the TransferUtility which is constructed using the
     * given Context
     *
     * @param context An Context instance.
     * @return a TransferUtility instance
     */
    public static TransferUtility getS3TransferUtility(Context context) {
        if (sTransferUtility == null) {
            sTransferUtility = new TransferUtility(getS3Client(context.getApplicationContext()),
                    context.getApplicationContext());
        }

        return sTransferUtility;
    }


    // TODO: 17/01/2017 not tested yet
    public static boolean isInternetEnabled(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                Toast.makeText(context, activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            // not connected to the internet
            return false;
        }
        return false;
    }

    public static String getAvailableLocationType(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        String locationType;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationType = LocationManager.NETWORK_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationType = LocationManager.GPS_PROVIDER;
        } else {
            locationType = CommonConstants.LOCATION_TYPE_LOCATION_DISABLED;
        }
        return locationType;
    }

    public static boolean isLocationPermissionGranted(Context context){
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void startGetLocationService(Context context, boolean getNewData, String locationType, int actionType){
        Intent getLocationIntent = new Intent(context, GetLocationService.class);
        getLocationIntent.putExtra(GetLocationService.LOCATION_TYPE,locationType);
        getLocationIntent.putExtra(GetLocationService.GET_DATA,getNewData);
        getLocationIntent.putExtra(GetLocationService.ACTION_TYPE,actionType);
        context.startService(getLocationIntent);
    }

    public static boolean isEventInNotificationRadius(Context context, LatLng notificationLatLng){
        LatLng userLocation = CommonMethods.getLastLocation(context);
        String keyListNotificationRadius = context.getString(R.string.key_prefs_list_notification_radius);
        String[] notificationRadiusListKMValues = context.getResources().getStringArray(R.array.prefs_notification_radius_values_km);
        String currentValueNotificationRadiusListKM = PreferenceManager.getDefaultSharedPreferences(context).getString(keyListNotificationRadius,
                notificationRadiusListKMValues[CommonConstants.DEFAULT_NOTIFICATION_RADIUS_ITEM]);
        return (currentValueNotificationRadiusListKM.equals("-1") ||
                CommonMethods.distance(userLocation.latitude,userLocation.longitude,notificationLatLng.latitude,notificationLatLng.longitude)
                        <= Integer.valueOf(currentValueNotificationRadiusListKM));
    }
}
