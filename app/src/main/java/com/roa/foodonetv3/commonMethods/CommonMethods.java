package com.roa.foodonetv3.commonMethods;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.maps.model.LatLng;
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
import com.roa.foodonetv3.services.GetDataService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CommonMethods {
    private static final String TAG = "CommonMethods";

    /** we only need one instance of the clients and credentials provider */
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

                break;
            case R.id.nav_groups:
                intent = new Intent(context, GroupsActivity.class);
                if(context instanceof GroupsActivity || context instanceof MainActivity){
                    // TODO: 06/12/2016 test
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                } else{
                    context.startActivity(intent);
                }
                break;
            case R.id.nav_settings:
                if(getMyUserID(context)==-1){
                    /** if the user is not signed in yet, open the sign in activity */
                    intent = new Intent(context,SignInActivity.class);
                } else{
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

    /** returns current epoch time in seconds(NOT MILLIS!) */
    public static double getCurrentTimeSeconds() {
        long currentTime = System.currentTimeMillis()/1000;
        return currentTime;
    }

    /** returns a string of time difference between two times in epoch time seconds (NOT MILLIS!) with a changing perspective according to the duration */
    public static String getTimeDifference(Context context, Double earlierTimeInSeconds, Double laterTimeInSeconds) {
        long timeDiff = (long) (laterTimeInSeconds - earlierTimeInSeconds) / 60; // minutes as start
        StringBuilder message = new StringBuilder();
        if (timeDiff < 0) {
            return "N/A";
        } else if(timeDiff < 1440){
            /** hours, minutes */
            if(timeDiff /60 != 0){
                message.append(String.format(Locale.US,"%1$d%2$s ", timeDiff / 60 , context.getResources().getString(R.string.h_hours)));
            }
            message.append(String.format(Locale.US,"%1$d%2$s", timeDiff % 60, context.getResources().getString(R.string.min_minutes)));
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
        return message.toString();
    }

    public static boolean isUserGroupAdmin(Context context, ArrayList<GroupMember> members){
        long userID = getMyUserID(context);
        GroupMember member;
        for(int i = 0; i < members.size(); i++){
            member = members.get(i);
            if(member.getUserID() == userID){
                return member.isAdmin();
            }
        }
        return false;
    }

    /** get the message according to the server specified report type */
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

    public static void getNewData(Context context){
        Intent getDataIntent = new Intent(context, GetDataService.class);
        getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_DATA);
        context.startService(getDataIntent);
    }

    /** returns a UUID */
    public static String getDeviceUUID(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_prefs_device_uuid), null);
    }

    /** returns the userID from shared preferences */
    public static long getMyUserID(Context context) {
        long userID = PreferenceManager.getDefaultSharedPreferences(context).getLong(context.getString(R.string.key_prefs_user_id),(long) -1);
        return userID;
    }

    /** @return returns the userName from shared preferences */
    public static String getMyUserName(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_prefs_user_name),"");
    }

    /** saves the userID to shared preferences */
    public static void setMyUserID(Context context, long userID) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(context.getString(R.string.key_prefs_user_id), userID).apply();
    }

    public static String getMyUserPhone(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.key_prefs_user_phone), null);
    }

    public static LatLng getLastLocation(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new LatLng(Double.valueOf(preferences.getString(context.getString(R.string.key_prefs_user_lat),String.valueOf(CommonConstants.LATLNG_ERROR))),
                Double.valueOf(preferences.getString(context.getString(R.string.key_prefs_user_lng),String.valueOf(CommonConstants.LATLNG_ERROR))));
    }

    /** should increment negatively for a unique id until the server gives us a server unique publication id to replace it */
    public static long getNewLocalPublicationID() {
        //todo add a check for available negative id, currently hard coded
        return (long)-1;
    }

    public static String getDigitsFromPhone(String origin){
        return origin.replaceAll("[^0-9]", "");
    }

    public static boolean comparePhoneNumbers(String first, String second){
        first = removeInternationalPhoneCode(getDigitsFromPhone(first));
        second = removeInternationalPhoneCode(getDigitsFromPhone(second));
        return PhoneNumberUtils.compare(first,second);
    }

    private static String removeInternationalPhoneCode(String phone){
        if(phone.startsWith("972")){
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
        double distance = R * c;
        return distance;
    }

    /** Creates a local image file name for taking the picture with the camera */
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */);
    }

    /** Creates a local image file name for downloaded images from s3 server of a specific publication */
    public static File createImageFile(Context context, long publicationID) throws IOException {
        String imageFileName = "PublicationID." + publicationID;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        return File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */);

        File newFile = new File(storageDir.getPath() + "/" + imageFileName + ".jpg");
        Log.d(TAG, "newFile = " + newFile.getPath());
        return newFile;
    }

    /** @return file name from publicationID */
    public static String getFileNameFromPublicationID(long publicationID,int version){
        return String.format(Locale.US,"%1$d.%2$d.jpg",
                publicationID,version);
    }

    /** returns the file name without the path */
    public static String getFileNameFromPath(String path) {
        String[] segments = path.split("/");
        return segments[segments.length - 1];
    }

    /** returns the file name without the path */
    public static String getPublicationIDFromFile(String path) {
        String[] segments = path.split(".");
        if (segments.length > 1) {
            return segments[segments.length - 2];
        } else {
            return "n";
        }
    }

    /** Creates a local image file name for downloaded images from s3 server of a specific publication */
    public static String getPhotoPathByID(Context context, long publicationID, int version) {
        String imageFileName = getFileNameFromPublicationID(publicationID,version);
        String storageDir = (context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        String newFile = storageDir + "/" + imageFileName;
        Log.d(TAG, "newFile = " + newFile);
        return newFile;
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
    public static TransferUtility getTransferUtility(Context context) {
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

    public static boolean isGpsEnabled(Context context){
        final LocationManager manager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );

        if (manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            return true;
        }else {
            return false;
        }
    }


}
