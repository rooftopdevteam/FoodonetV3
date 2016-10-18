package com.roa.foodonetv3.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class Publication implements Parcelable {
    private long id;
    private int version, publisherID, audience;
    private String title,subtitle,address,startingDate,endingDate,contactInfo,activeDeviceDevUUID,photoURL,identityProviderUserName,priceDescription;
    private short typeOfCollecting;
    private double lat, lng,price;
    private boolean isOnAir;

    public static final String PUBLICATION_KEY = "publication";
    public static final String PUBLICATION_UNIQUE_ID_KEY = "_id";
    public static final String PUBLICATION_PUBLISHER_UUID_KEY = "active_device_dev_uuid";
    public static final String PUBLICATION_UNIQUE_ID_KEY_JSON = "id";
    public static final String PUBLICATION_VERSION_KEY = "version";
    public static final String PUBLICATION_TITLE_KEY = "title";
    public static final String PUBLICATION_SUBTITLE_KEY = "subtitle";
    public static final String PUBLICATION_ADDRESS_KEY = "address";
    public static final String PUBLICATION_TYPE_OF_COLLECTION_KEY = "type_of_collecting";
    public static final String PUBLICATION_LATITUDE_KEY = "latitude";
    public static final String PUBLICATION_LONGITUDE_KEY = "longitude";
    public static final String PUBLICATION_STARTING_DATE_KEY = "starting_date";
    public static final String PUBLICATION_ENDING_DATE_KEY = "ending_date";
    public static final String PUBLICATION_CONTACT_INFO_KEY = "contact_info";
    public static final String PUBLICATION_PHOTO_URL = "photo_url";
    public static final String PUBLICATION_COUNT_OF_REGISTER_USERS_KEY = "pulbicationCountOfRegisteredUsersKey";
    public static final String PUBLICATION_IS_ON_AIR_KEY = "is_on_air";
    public static final String PUBLICATION_PUBLISHER_USER_NAME_KEY = "identity_provider_user_name";
    public static final String PUBLICATION_PUBLISHER_ID_KEY = "publisher_id";
    public static final String PUBLICATION_PRICE_KEY = "price";
    public static final String PUBLICATION_PRICE_DESCRIPTION_KEY = "price_description";
    public static final String PUBLICATION_USER_RATING_KEY = "user_rating";
    public static final String PUBLICATION_AUDIENCE_KEY = "audience";
    public static final String PUBLICATION_JSON_SEND_PUBLISHER_USER_NAME_KEY = "publisher_user_name";

    public Publication(long id, int version, String title, String subtitle, String address, short typeOfCollecting,
                       double lat, double lng, String startingDate, String endingDate, String contactInfo, boolean isOnAir,
                       String activeDeviceDevUUID, String photoURL, int publisherID, int audience, String identityProviderUserName,
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
        publisherID = in.readInt();
        audience = in.readInt();
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

    public static JSONObject getPublicationJson(Publication publication){
        JSONObject publicationJsonRoot = new JSONObject();
        JSONObject publicationJson = new JSONObject();
        try {
            publicationJson.put(PUBLICATION_LATITUDE_KEY, publication.getLat());
            publicationJson.put(PUBLICATION_LONGITUDE_KEY, publication.getLng());
            publicationJson.put(PUBLICATION_STARTING_DATE_KEY, publication.getStartingDate());
            publicationJson.put(PUBLICATION_ADDRESS_KEY, publication.getAddress());
            publicationJson.put(PUBLICATION_ENDING_DATE_KEY, publication.getEndingDate());
            publicationJson.put(PUBLICATION_CONTACT_INFO_KEY, publication.getContactInfo());
            publicationJson.put(PUBLICATION_SUBTITLE_KEY, publication.getSubtitle());
            publicationJson.put(PUBLICATION_TITLE_KEY, publication.getTitle());
            publicationJson.put(PUBLICATION_TYPE_OF_COLLECTION_KEY, publication.getTypeOfCollecting()); // was +1
            publicationJson.put(PUBLICATION_IS_ON_AIR_KEY, publication.isOnAir());
            publicationJson.put(PUBLICATION_PHOTO_URL, publication.getPhotoURL());
            publicationJson.put(PUBLICATION_PUBLISHER_UUID_KEY, publication.getPublisherID());
            publicationJson.put(PUBLICATION_JSON_SEND_PUBLISHER_USER_NAME_KEY, publication.getIdentityProviderUserName());
            publicationJson.put(PUBLICATION_PUBLISHER_ID_KEY, publication.getPublisherID());
            publicationJson.put(PUBLICATION_PRICE_KEY, publication.getPrice());
            publicationJson.put(PUBLICATION_AUDIENCE_KEY, publication.getAudience());
            publicationJson.put(PUBLICATION_PRICE_DESCRIPTION_KEY, publication.getPriceDescription());

            publicationJsonRoot.put(PUBLICATION_KEY,publicationJson);
        } catch (JSONException e) {
            Log.e("getPublicationJson", e.getMessage());
        }
        return publicationJsonRoot;
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

    public int getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(int publisherID) {
        this.publisherID = publisherID;
    }

    public int getAudience() {
        return audience;
    }

    public void setAudience(int audience) {
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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeInt(version);
        dest.writeInt(publisherID);
        dest.writeInt(audience);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(address);
        dest.writeString(startingDate);
        dest.writeString(endingDate);
        dest.writeString(contactInfo);
        dest.writeString(activeDeviceDevUUID);
        dest.writeString(photoURL);
        dest.writeString(identityProviderUserName);
        dest.writeString(priceDescription);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeDouble(price);
        dest.writeByte((byte) (isOnAir ? 1 : 0));
    }
}

