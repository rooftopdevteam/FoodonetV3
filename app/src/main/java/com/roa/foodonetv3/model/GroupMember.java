package com.roa.foodonetv3.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class GroupMember implements Parcelable{
    private static final String TAG = "GroupMember";

    public static final String KEY = "members";

    public static final String UNIQUE_ID = "id";
    public static final String GROUP_ID = "Group_id";   //in ios capital G
    public static final String USER_ID = "user_id";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String NAME = "name";
    public static final String IS_ADMIN = "is_admin";

    private long groupID, userID, uniqueID;
    private String name, phoneNumber;
    private boolean isAdmin;

    public GroupMember(long uniqueID, long groupID, long userID, String phoneNumber, String name, boolean isAdmin) {
        this.groupID = groupID;
        this.userID = userID;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.isAdmin = isAdmin;
        this.uniqueID = uniqueID;
    }

    protected GroupMember(Parcel in) {
        groupID = in.readLong();
        userID = in.readLong();
        uniqueID = in.readLong();
        name = in.readString();
        phoneNumber = in.readString();
        isAdmin = in.readByte() != 0;
    }

    public static final Creator<GroupMember> CREATOR = new Creator<GroupMember>() {
        @Override
        public GroupMember createFromParcel(Parcel in) {
            return new GroupMember(in);
        }

        @Override
        public GroupMember[] newArray(int size) {
            return new GroupMember[size];
        }
    };

    /** creates a json object to be sent to the server */
    public JSONObject getAddMemberJson(){
        JSONObject member = new JSONObject();
        try {
            member.put(GROUP_ID,getGroupID());
            member.put(USER_ID,getUserID());
            member.put(PHONE_NUMBER,getPhoneNumber());
            member.put(NAME,getName());
            member.put(IS_ADMIN,isAdmin);
        } catch (JSONException e) {
            Log.e(TAG,e.getMessage());
        }
        return member;
    }

    public long getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(long uniqueID) {
        this.uniqueID = uniqueID;
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(groupID);
        dest.writeLong(userID);
        dest.writeLong(uniqueID);
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeByte((byte) (isAdmin ? 1 : 0));
    }
}
