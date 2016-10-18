package com.roa.foodonetv3.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.roa.foodonetv3.R;
import com.roa.foodonetv3.model.Publication;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class AddPublicationService extends IntentService {

    public AddPublicationService() {
        super("AddPublicationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("AddPublicationService","entered service");
            HttpsURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder urlAddressBuilder = new StringBuilder(getResources().getString(R.string.foodonet_server));
            urlAddressBuilder.append("publications.json");
            try {
                URL url = new URL(urlAddressBuilder.toString());
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.addRequestProperty("Accept","application/json");
                connection.addRequestProperty("Content-Type","application/json");
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"utf-8"));
                Publication testPub = new Publication(-1,-1,"testAlon","testAlonSub","ניר עציון,ניר עציון",(short)2,32.6973198,34.9903382,"1476000120.0","1476172920.0","0506765833",
                        true,"8360c4c4be9e1398","",16,0,"Roi Shaul",0.0,"");
                JSONObject jsonObject = Publication.getPublicationJson(testPub);
                Log.d("ALONTEST",jsonObject.toString());
                writer.write(jsonObject.toString());
                writer.flush();
                writer.close();
                os.close();
                StringBuilder builder = new StringBuilder();
                if(connection.getResponseCode()!= HttpsURLConnection.HTTP_OK){
                    //do something
                }
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while((line = reader.readLine())!= null){
                    builder.append(line);
                }
                Log.d("SERVER RESPONSE", builder.toString());
            } catch (IOException e) {
                Log.e("AddPublicationService",e.getMessage());
            }
            finally {
                if(connection!= null){
                    connection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e("AddPublicationService",e.getMessage());
                    }
                }
            }
        }
    }
}
