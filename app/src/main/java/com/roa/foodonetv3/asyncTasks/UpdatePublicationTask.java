package com.roa.foodonetv3.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.ReceiverConstants;
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.model.Publication;
import com.roa.foodonetv3.serverMethods.StartFoodonetServiceMethods;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class UpdatePublicationTask extends AsyncTask<ArrayList<Long>,Void,Void> {
    private static final String TAG = "UpdatePublicationTask";
    private Context context;

    public UpdatePublicationTask(Context context) {
        this.context = context;
    }

    private static final int TIMEOUT_TIME = 5000;
    @Override
    protected Void doInBackground(ArrayList<Long>... params) {
        boolean serviceError = false;
//        long publicationID = params[0];
//        String[] args = new String[] {String.valueOf(publicationID)};
        String[] args;
        ArrayList<Long> publications = params[0];
        for (int i = 0; i < publications.size(); i++) {
            args = new String[]{String.valueOf(publications.get(i))};
            int actionType = ReceiverConstants.ACTION_GET_PUBLICATION;
            String urlAddress = StartFoodonetServiceMethods.getUrlAddress(context, ReceiverConstants.ACTION_GET_PUBLICATION, args);
            PublicationsDBHandler publicationsDBHandler = new PublicationsDBHandler(context);
            HttpsURLConnection connection = null;
            BufferedReader reader = null;
            URL url;
            StringBuilder builder = new StringBuilder();
            try {
                url = new URL(urlAddress);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setConnectTimeout(TIMEOUT_TIME);
                // TODO: 28/11/2016 add logic for timeout
                int httpType = StartFoodonetServiceMethods.getHTTPType(actionType);
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpsURLConnection.HTTP_OK && responseCode != HttpsURLConnection.HTTP_CREATED
                        // right now deleting the last member from a group gives response 500 from the server, though still deleting the member,
                        // in order to operate adding this logic here for now
                        && responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                    serviceError = true;
                } else {
                    // right now deleting the last member from a group gives response 500 from the server, though still deleting the member,
                    // in order to operate adding this logic here for now
                    if (responseCode != HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                        // TODO: 10/05/2017 add declarations
                        JSONObject publicationObject = new JSONObject(builder.toString());
                        String activeDeviceDevUUID = publicationObject.getString("active_device_dev_uuid");
                        long audience = publicationObject.getLong("audience");
                        long publisherID = publicationObject.getLong("publisher_id");
                        long id = publicationObject.getLong("id");
                        int version = publicationObject.getInt("version");
                        String title = publicationObject.getString("title");
                        String subtitle = publicationObject.getString("subtitle");
                        String address = publicationObject.getString("address");
                        short typeOfCollecting = (short) publicationObject.getInt("type_of_collecting");
                        Double lat = publicationObject.getDouble("latitude");
                        Double lng = publicationObject.getDouble("longitude");
                        String startingDate = publicationObject.getString("starting_date");
                        String endingDate = publicationObject.getString("ending_date");
                        String contactInfo = publicationObject.getString("contact_info");
                        boolean isOnAir = publicationObject.getBoolean("is_on_air");
                        String photoURL = publicationObject.getString("photo_url");
                        String identityProviderUserName = publicationObject.getString("identity_provider_user_name");
                        Double price = publicationObject.getDouble("price");
                        String priceDescription = publicationObject.getString("price_description");
                        Publication publication = new Publication(id, version, title, subtitle, address, typeOfCollecting, lat, lng, startingDate, endingDate, contactInfo, isOnAir,
                                activeDeviceDevUUID, photoURL, publisherID, audience, identityProviderUserName, price, priceDescription);
                        publicationsDBHandler.updatePublication(publication);
                    }
                }
            } catch (IOException e) {
                serviceError = true;
                Log.e(TAG, e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
        return null;
    }
}
