package com.foodonet.foodonet.model;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private static final String TAG = "User";
    public static final String IDENTITY_PROVIDER_USER_ID = "identity_provider_user_id";
    private static final String USER_KEY = "user";
    private static final String IDENTITY_PROVIDER = "identity_provider";
    private static final String IDENTITY_PROVIDER_USER_TOKEN = "identity_provider_token";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String IDENTITY_PROVIDER_EMAIL = "identity_provider_email";
    private static final String IDENTITY_PROVIDER_USER_NAME = "identity_provider_user_name";
    private static final String IS_LOGGED_IN= "is_logged_in";
    private static final String ACTIVE_DEVICE_DEV_UUID= "active_device_dev_uuid";

    private String identityProvider, identityProviderUserUID,identityProviderToken,phoneNumber,identityProviderEmail,identityProviderUserName,activeDeviceDevUuid;
    private boolean isLoggedIn;

    public User(String identityProvider, String identityProviderUserUID, String identityProviderToken, String phoneNumber, String identityProviderEmail,
                String identityProviderUserName, boolean isLoggedIn, String activeDeviceDevUuid) {
        this.identityProvider = identityProvider;
        this.identityProviderUserUID = identityProviderUserUID;
        this.identityProviderToken = identityProviderToken;
        this.phoneNumber = phoneNumber;
        this.identityProviderEmail = identityProviderEmail;
        this.identityProviderUserName = identityProviderUserName;
        this.isLoggedIn = isLoggedIn;
        this.activeDeviceDevUuid = activeDeviceDevUuid;
    }

    /** creates a json object to be sent to the server */
    public JSONObject getUserJson(){
        JSONObject userJsonRoot = new JSONObject();
        JSONObject userJson = new JSONObject();
        try {
            userJson.put(IDENTITY_PROVIDER, getIdentityProvider());
            userJson.put(IDENTITY_PROVIDER_USER_ID, getIdentityProviderUserUID());
            userJson.put(IDENTITY_PROVIDER_USER_TOKEN, getIdentityProviderToken());
            userJson.put(PHONE_NUMBER, getPhoneNumber());
            userJson.put(IDENTITY_PROVIDER_EMAIL, getIdentityProviderEmail());
            userJson.put(IDENTITY_PROVIDER_USER_NAME, getIdentityProviderUserName());
            userJson.put(IS_LOGGED_IN, isLoggedIn());
            userJson.put(ACTIVE_DEVICE_DEV_UUID, getActiveDeviceDevUuid());

            userJsonRoot.put(USER_KEY,userJson);
        } catch (JSONException e) {
            Log.e(TAG,e.getMessage());
        }
        return userJsonRoot;
    }

    private String getIdentityProvider() {
        return identityProvider;
    }

    private String getIdentityProviderUserUID() {
        return identityProviderUserUID;
    }

    private String getIdentityProviderToken() {
        return identityProviderToken;
    }

    private String getPhoneNumber() {
        return phoneNumber;
    }

    private String getIdentityProviderEmail() {
        if(identityProviderEmail==null){
            return "";
        }
        return identityProviderEmail;
    }

    private String getIdentityProviderUserName() {
        return identityProviderUserName;
    }

    private String getActiveDeviceDevUuid() {
        return activeDeviceDevUuid;
    }

    private boolean isLoggedIn() {
        return isLoggedIn;
    }
}
