package com.foodonet.foodonet.asyncTasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;
import com.foodonet.foodonet.db.PublicationsDBHandler;
import com.foodonet.foodonet.model.Publication;
import com.foodonet.foodonet.serverMethods.StartFoodonetServiceMethods;
import com.foodonet.foodonet.services.GetDataService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class UpdatePublicationsTask extends AsyncTask<ArrayList<Long>,Void,Void> {
    private static final String TAG = "UpdateNonPublicPubsTask";

    private static final int ACTION_DELETE = 1;
    private static final int ACTION_UPDATE = 2;
    private static final int ACTION_NO_CHANGE = 3;
    private Context context;
    private boolean isPublicPublications;

    public UpdatePublicationsTask(Context context,boolean isPublicPublications) {
        this.context = context;
        this.isPublicPublications = isPublicPublications;
    }

    private static final int TIMEOUT_TIME = 5000;
    @SafeVarargs
    @Override
    protected final Void doInBackground(ArrayList<Long>... params) {
        PublicationsDBHandler publicationsDBHandler = new PublicationsDBHandler(context);
        String[] args;
        ArrayList<Long> publications = params[0];
        for (int i = 0; i < publications.size(); i++) {
            long publicationID = publications.get(i);
            args = new String[]{String.valueOf(publicationID)};
            String urlAddress = StartFoodonetServiceMethods.getUrlAddress(context, ReceiverConstants.ACTION_GET_PUBLICATION, args);
            HttpsURLConnection connection = null;
            BufferedReader reader = null;
            URL url;
            StringBuilder builder = new StringBuilder();
            int action;
            try {
                url = new URL(urlAddress);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setConnectTimeout(TIMEOUT_TIME);
                int responseCode = connection.getResponseCode();
                if(responseCode == HttpsURLConnection.HTTP_NOT_FOUND){
                    // if publication not in server (returns a 404) - delete
                    publicationsDBHandler.deletePublication(publicationID);

                } else if (responseCode == HttpsURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    JSONObject publicationObject = new JSONObject(builder.toString());
                    int version = publicationObject.getInt("version");
                    boolean isOnAir = publicationObject.getBoolean("is_on_air");
                    long publisherID = publicationObject.getLong("publisher_id");
                    long audience = publicationObject.getLong("audience");
                    String endingDate = publicationObject.getString("ending_date");
                    if(publisherID == CommonMethods.getMyUserID(context)){
                        // admin
                        if(version != publicationsDBHandler.getPublicationVersion(publicationID)){
                            // admin publication with new version - update (may be offline or ended, should be still saved for actions as user)
                            action = ACTION_UPDATE;
                        } else{
                            // admin publication with the same version as local db - no change
                            action = ACTION_NO_CHANGE;
                        }
                    } else{
                        // non admin
                        if(publisherID == 0){
                            // non admin public publication
                            if(!isOnAir || Double.valueOf(endingDate) < CommonMethods.getCurrentTimeSeconds()){
                                // non admin public not relevant publication - delete from local db (they will be fetched by "get all publications" from server if relevant again)
                                action = ACTION_DELETE;
                            } else{
                                if(version != publicationsDBHandler.getPublicationVersion(publicationID)){
                                    // non admin public relevant publication with new version - update
                                    action = ACTION_UPDATE;
                                } else{
                                    // non admin public relevant publication with the same version as local db - no change
                                    action = ACTION_NO_CHANGE;
                                }
                            }
                        } else{
                            // non admin non public publication
                            if(version != publicationsDBHandler.getPublicationVersion(publicationID)){
                                // non admin non public publication with new version - update
                                action = ACTION_UPDATE;
                            } else{
                                // non admin non public publication with the same version as local db = no change
                                // (may be offline or ended, should still be saved if admin decides to make them online again)
                                action = ACTION_NO_CHANGE;
                            }
                        }
                    }
                    switch (action){
                        case ACTION_DELETE:
                            publicationsDBHandler.deletePublication(publicationID);
                            break;
                        case ACTION_NO_CHANGE:
                            break;
                        case ACTION_UPDATE:
                            String activeDeviceDevUUID = publicationObject.getString("active_device_dev_uuid");
                            long id = publicationObject.getLong("id");
                            String title = publicationObject.getString("title");
                            String subtitle = publicationObject.getString("subtitle");
                            String address = publicationObject.getString("address");
                            short typeOfCollecting = (short) publicationObject.getInt("type_of_collecting");
                            Double lat = publicationObject.getDouble("latitude");
                            Double lng = publicationObject.getDouble("longitude");
                            String startingDate = publicationObject.getString("starting_date");
                            String contactInfo = publicationObject.getString("contact_info");
                            String photoURL = publicationObject.getString("photo_url");
                            String identityProviderUserName = publicationObject.getString("identity_provider_user_name");
                            Double price = publicationObject.getDouble("price");
                            String priceDescription = publicationObject.getString("price_description");
                            Publication publication = new Publication(id, version, title, subtitle, address, typeOfCollecting, lat, lng, startingDate, endingDate, contactInfo, isOnAir,
                                    activeDeviceDevUUID, photoURL, publisherID, audience, identityProviderUserName, price, priceDescription);
                            publicationsDBHandler.updatePublication(publication);
                            break;
                    }
                }
            } catch (IOException e) {
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
        if(isPublicPublications){
            Intent getDataIntent = new Intent(context,GetDataService.class);
            getDataIntent.putExtra(ReceiverConstants.ACTION_TYPE,ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS);
            context.startService(getDataIntent);
            Log.d(TAG,"finished updating public publications");
        } else{
            Log.d(TAG,"finished updating non public publications");
        }
        return null;
    }
}
