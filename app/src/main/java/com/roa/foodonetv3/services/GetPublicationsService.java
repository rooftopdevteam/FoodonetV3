package com.roa.foodonetv3.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.roa.foodonetv3.R;
import com.roa.foodonetv3.model.Publication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class GetPublicationsService extends IntentService {
    public static final String ACTION_SERVICE_GET_PUBLICATIONS = "com.roa.foodonetv3.services.ACTION_SERVICE_GET_PUBLICATIONS";
    public static final String QUERY_ARGS = "query_args";
    public static final String QUERY_ERROR = "query_error";
    public static final String QUERY_PUBLICATIONS = "query_publications";

    public GetPublicationsService() {
        super("GetPublicationsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean queryError = false;
        ArrayList<Publication> publications = new ArrayList<>();
        if (intent != null) {
            StringBuilder urlAddressBuilder = new StringBuilder();
            urlAddressBuilder.append(getResources().getString(R.string.foodonet_server));
            urlAddressBuilder.append(intent.getStringExtra(QUERY_ARGS));
            HttpsURLConnection connection = null;
            BufferedReader reader = null;
            URL url;
            StringBuilder builder = new StringBuilder();
            try {
                url = new URL(urlAddressBuilder.toString());
                connection = (HttpsURLConnection) url.openConnection();
                if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                    queryError = true;
                }
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                JSONArray root = new JSONArray(builder.toString());
                // temp
                for (int i = 0; i < root.length(); i++) {
                    JSONObject publication = root.getJSONObject(i);
                    long id = publication.getLong("id");
                    int version = publication.getInt("version");
                    String title = publication.getString("title");
                    String subtitle = publication.getString("subtitle");
                    String address = publication.getString("address");
                    short typeOfCollecting = (short) publication.getInt("type_of_collecting");
                    Double lat = publication.getDouble("latitude");
                    Double lng = publication.getDouble("longitude");
                    String startingDate = publication.getString("starting_date");
                    String endingDate = publication.getString("ending_date");
                    String contactInfo = publication.getString("contact_info");
                    boolean isOnAir = publication.getBoolean("is_on_air");
                    String activeDeviceDevUUID = publication.getString("active_device_dev_uuid");
                    String photoURL = publication.getString("photo_url");
                    int publisherID = publication.getInt("publisher_id");
                    int audience = publication.getInt("audience");
                    String identityProviderUserName = publication.getString("identity_provider_user_name");
                    Double price = publication.getDouble("price");
                    String priceDescription = "price_description";
                    publications.add(new Publication(id, version, title, subtitle, address, typeOfCollecting, lat, lng, startingDate, endingDate, contactInfo, isOnAir,
                            activeDeviceDevUUID,photoURL,publisherID,audience,identityProviderUserName,price,priceDescription));
                }
                //

            } catch (IOException | JSONException e) {
                Log.e("GetPublicationsTask", e.getMessage());
                queryError = true;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("GetPublicationsTask", e.getMessage());
                    }
                }
            }
        }
        Intent i = new Intent(ACTION_SERVICE_GET_PUBLICATIONS);
        i.putExtra(QUERY_ERROR,queryError);
        i.putExtra(QUERY_PUBLICATIONS,publications);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }
}
