package com.roa.foodonetv3.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.roa.foodonetv3.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Publication implements Parcelable {
    private static final String TAG = "Publication";

    public static final String PUBLICATION_KEY = "publication";
    public static final String PUBLICATION_COUNT_OF_REGISTER_USERS_KEY = "publicationCountOfRegisteredUsersKey";

    public static final String PUBLICATION_ID = "publication_id";
    public static final String PUBLICATION_VERSION = "publication_version";

    private static final String PUBLICATION_PUBLISHER_UUID_KEY = "active_device_dev_uuid";
    private static final String PUBLICATION_TITLE_KEY = "title";
    private static final String PUBLICATION_SUBTITLE_KEY = "subtitle";
    private static final String PUBLICATION_ADDRESS_KEY = "address";
    private static final String PUBLICATION_TYPE_OF_COLLECTION_KEY = "type_of_collecting";
    private static final String PUBLICATION_LATITUDE_KEY = "latitude";
    private static final String PUBLICATION_LONGITUDE_KEY = "longitude";
    private static final String PUBLICATION_STARTING_DATE_KEY = "starting_date";
    private static final String PUBLICATION_ENDING_DATE_KEY = "ending_date";
    private static final String PUBLICATION_CONTACT_INFO_KEY = "contact_info";
    private static final String PUBLICATION_PHOTO_URL = "photo_url";
    public static final String PUBLICATION_IS_ON_AIR_KEY = "is_on_air";
    private static final String PUBLICATION_PUBLISHER_ID_KEY = "publisher_id";
    private static final String PUBLICATION_PRICE_KEY = "price";
    private static final String PUBLICATION_PRICE_DESCRIPTION_KEY = "price_description";
    private static final String PUBLICATION_AUDIENCE_KEY = "audience";
    private static final String PUBLICATION_JSON_SEND_PUBLISHER_USER_NAME_KEY = "publisher_user_name";

    private long id, publisherID, audience;
    private int version;

    private String title,subtitle,address,startingDate,endingDate,contactInfo,activeDeviceDevUUID,photoURL,identityProviderUserName,priceDescription;
    private short typeOfCollecting;
    private double lat, lng, price;
    private boolean isOnAir;

    public Publication(long id, int version, String title, String subtitle, String address, short typeOfCollecting,
                       double lat, double lng, String startingDate, String endingDate, String contactInfo, boolean isOnAir,
                       String activeDeviceDevUUID, String photoURL, long publisherID, long audience, String identityProviderUserName,
                       double price, String priceDescription) {
        this.id = id;
        this.version = version;
        this.title = title;
        this.subtitle = subtitle;
        this.address = address;
        this.typeOfCollecting = typeOfCollecting;
        this.lat = lat;
        this.lng = lng;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
        this.contactInfo = contactInfo;
        this.isOnAir = isOnAir;
        this.activeDeviceDevUUID = activeDeviceDevUUID;
        this.photoURL = photoURL;
        this.publisherID = publisherID;
        this.audience = audience;
        this.identityProviderUserName = identityProviderUserName;
        this.price = price;
        this.priceDescription = priceDescription;


    }

    protected Publication(Parcel in) {
        id = in.readLong();
        version = in.readInt();
        publisherID = in.readLong();
        audience = in.readLong();
        title = in.readString();
        subtitle = in.readString();
        address = in.readString();
        startingDate = in.readString();
        endingDate = in.readString();
        contactInfo = in.readString();
        activeDeviceDevUUID = in.readString();
        photoURL = in.readString();
        identityProviderUserName = in.readString();
        priceDescription = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        price = in.readDouble();
        isOnAir = in.readByte() != 0;
    }

    public static final Creator<Publication> CREATOR = new Creator<Publication>() {
        @Override
        public Publication createFromParcel(Parcel in) {
            return new Publication(in);
        }

        @Override
        public Publication[] newArray(int size) {
            return new Publication[size];
        }
    };

    public JSONObject getPublicationJson(){
        /** creates a json object from the publication as to be sent to the server */
        JSONObject publicationJsonRoot = new JSONObject();
        JSONObject publicationJson = new JSONObject();
        try {
            publicationJson.put(PUBLICATION_TITLE_KEY, getTitle());
            publicationJson.put(PUBLICATION_SUBTITLE_KEY, getSubtitle());
            publicationJson.put(PUBLICATION_TYPE_OF_COLLECTION_KEY, getTypeOfCollecting()); // was +1
            publicationJson.put(PUBLICATION_ADDRESS_KEY, getAddress());
            publicationJson.put(PUBLICATION_STARTING_DATE_KEY, getStartingDate());
            publicationJson.put(PUBLICATION_ENDING_DATE_KEY, getEndingDate());
            publicationJson.put(PUBLICATION_CONTACT_INFO_KEY, getContactInfo());
            publicationJson.put(PUBLICATION_LATITUDE_KEY, getLat());
            publicationJson.put(PUBLICATION_LONGITUDE_KEY, getLng());
            publicationJson.put(PUBLICATION_IS_ON_AIR_KEY, isOnAir());
//            publicationJson.put(PUBLICATION_PHOTO_URL, getPhotoURL()); // to be in sync with the ios team, not saving photo url
            publicationJson.put(PUBLICATION_PHOTO_URL,"");
            publicationJson.put(PUBLICATION_PUBLISHER_UUID_KEY, getActiveDeviceDevUUID());
            publicationJson.put(PUBLICATION_JSON_SEND_PUBLISHER_USER_NAME_KEY, getIdentityProviderUserName());
            publicationJson.put(PUBLICATION_PUBLISHER_ID_KEY, getPublisherID());
            publicationJson.put(PUBLICATION_PRICE_KEY, getPrice());
            publicationJson.put(PUBLICATION_AUDIENCE_KEY, getAudience());
            publicationJson.put(PUBLICATION_PRICE_DESCRIPTION_KEY, getPriceDescription());

            publicationJsonRoot.put(PUBLICATION_KEY,publicationJson);
        } catch (JSONException e) {
            Log.e(TAG,e.getMessage());
        }
        return publicationJsonRoot;
    }

    public int getGroupImageResource(){
        int imageGroupResource;
        if(getAudience() == 0){
            if(getPrice()==0){
                imageGroupResource = R.drawable.public_blue;
            } else{
                imageGroupResource = R.drawable.public_yellow;
            }
        } else{
            if(getPrice() == 0){
                imageGroupResource = R.drawable.group_blue;
            } else{
                imageGroupResource = R.drawable.group_yellow;
            }
        }
        return imageGroupResource;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getAddress() {
        if(address==null){
            return "";
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(String startingDate) {
        this.startingDate = startingDate;
    }

    public String getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(String endingDate) {
        this.endingDate = endingDate;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public short getTypeOfCollecting() {
        return typeOfCollecting;
    }

    public void setTypeOfCollecting(short typeOfCollecting) {
        this.typeOfCollecting = typeOfCollecting;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isOnAir() {
        return isOnAir;
    }

    public void setOnAir(boolean onAir) {
        isOnAir = onAir;
    }

    public String getActiveDeviceDevUUID() {
        return activeDeviceDevUUID;
    }

    public void setActiveDeviceDevUUID(String activeDeviceDevUUID) {
        this.activeDeviceDevUUID = activeDeviceDevUUID;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public long getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(long publisherID) {
        this.publisherID = publisherID;
    }

    public long getAudience() {
        return audience;
    }

    public void setAudience(long audience) {
        this.audience = audience;
    }

    public String getIdentityProviderUserName() {
        return identityProviderUserName;
    }

    public void setIdentityProviderUserName(String identityProviderUserName) {
        this.identityProviderUserName = identityProviderUserName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPriceDescription() {
        return priceDescription;
    }

    public void setPriceDescription(String priceDescription) {
        this.priceDescription = priceDescription;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeInt(version);
        parcel.writeLong(publisherID);
        parcel.writeLong(audience);
        parcel.writeString(title);
        parcel.writeString(subtitle);
        parcel.writeString(address);
        parcel.writeString(startingDate);
        parcel.writeString(endingDate);
        parcel.writeString(contactInfo);
        parcel.writeString(activeDeviceDevUUID);
        parcel.writeString(photoURL);
        parcel.writeString(identityProviderUserName);
        parcel.writeString(priceDescription);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeDouble(price);
        parcel.writeByte((byte) (isOnAir ? 1 : 0));
    }
}

