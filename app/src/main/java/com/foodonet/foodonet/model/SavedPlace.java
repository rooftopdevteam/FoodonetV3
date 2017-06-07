package com.foodonet.foodonet.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SavedPlace implements Parcelable {
    private String address;
    private double lat, lng;

    public SavedPlace(String address, double lat, double lng) {
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    private SavedPlace(Parcel in) {
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public static final Creator<SavedPlace> CREATOR = new Creator<SavedPlace>() {
        @Override
        public SavedPlace createFromParcel(Parcel in) {
            return new SavedPlace(in);
        }

        @Override
        public SavedPlace[] newArray(int size) {
            return new SavedPlace[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(address);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
    }
}
