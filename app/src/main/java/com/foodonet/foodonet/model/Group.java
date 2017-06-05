package com.foodonet.foodonet.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class Group implements Parcelable {
    private static final String TAG = "Group";

    public static final String GROUP = "group";
    public static final String USER_ID = "user_id";
    public static final String MEMBERS = "members";
    public static final String GROUP_ID = "group_id";
    public static final String GET_GROUP_NAME = "group_name";

    private static final String GROUP_MEMBERS = "group_members";
    private static final String ADD_GROUP_NAME = "name";

    private String groupName;
    private long userID, groupID;

    public Group(String groupName, long userID, long groupID) {
        this.groupName = groupName;
        this.userID = userID;
        this.groupID = groupID;
    }

    protected Group(Parcel in) {
        groupName = in.readString();
        userID = in.readLong();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    /** creates a json object to be sent to the server */
    public JSONObject getAddGroupJson(){
        JSONObject groupRoot = new JSONObject();
        JSONObject group = new JSONObject();
        try {
            group.put(ADD_GROUP_NAME,getGroupName());
            group.put(USER_ID,getUserID());

            groupRoot.put(GROUP,group);
        } catch (JSONException e) {
            Log.e(TAG,e.getMessage());
        }
        return groupRoot;
    }

    /** creates a json object to be sent to the server */
    public static JSONObject getAddGroupMembersJson(ArrayList<GroupMember> members){
        JSONObject groupMembersRoot = new JSONObject();
        JSONArray groupMembersArray = new JSONArray();
        GroupMember member;
        try {
            for (int i = 0; i < members.size(); i++) {
                member = members.get(i);
                JSONObject groupMember = member.getAddMemberJson();
                groupMembersArray.put(groupMember);
            }
            groupMembersRoot.put(GROUP_MEMBERS,groupMembersArray);
        } catch (JSONException e) {
            Log.e(TAG,e.getMessage());
        }
        return groupMembersRoot;
    }

    public String getGroupName() {
        return groupName;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeLong(userID);
        dest.writeLong(groupID);
    }
}
