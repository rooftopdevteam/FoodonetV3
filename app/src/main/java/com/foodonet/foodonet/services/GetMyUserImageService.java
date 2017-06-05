package com.foodonet.foodonet.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class GetMyUserImageService extends IntentService {
    private static final String TAG = "GetMyUserImageService";

    public static final String IMAGE_URL = "image_url";

    public GetMyUserImageService() {
        super("GetMyUserImageService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String url = intent.getStringExtra(IMAGE_URL);
            String userImagePath = CommonMethods.getMyUserImageFilePath(this);
            File userImageFile;
            HttpsURLConnection connection = null;
            if(userImagePath!= null && url != null){
                userImageFile = new File(userImagePath);
                try {
                    URL imageURL = new URL(url);
                    connection = (HttpsURLConnection) imageURL.openConnection();
                    Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                    FileOutputStream fileOutputStream = new FileOutputStream(userImageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    bitmap.recycle();
                    Intent finishedIntent = new Intent(ReceiverConstants.BROADCAST_FOODONET);
                    finishedIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_SAVE_USER_IMAGE);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
                } catch (IOException e) {
                    Log.e(TAG,e.getMessage());
                } finally {
                    if(connection!= null){
                        connection.disconnect();
                    }
                }
            }
        }
    }
}
