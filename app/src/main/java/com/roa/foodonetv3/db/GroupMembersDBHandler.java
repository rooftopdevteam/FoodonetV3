package com.roa.foodonetv3.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.util.LongSparseArray;
import android.telephony.PhoneNumberUtils;

import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.GroupMember;

import java.util.ArrayList;

public class GroupMembersDBHandler {
    private Context context;

    public GroupMembersDBHandler(Context context) {
        this.context = context;
    }

    /** get all groups members */
    public ArrayList<GroupMember> getAllGroupsMembers(){
        ArrayList<GroupMember> members = new ArrayList<>();
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.MembersDB.CONTENT_URI,null,null,null,null);

        long uniqueID, groupID, userID;
        String name, phoneNumber;
        boolean isAdmin;
        while(c!=null && c.moveToNext()){
            uniqueID = c.getLong(c.getColumnIndex(FoodonetDBProvider.MembersDB._ID_COLUMN));
            groupID = c.getLong(c.getColumnIndex(FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN));
            userID = c.getLong(c.getColumnIndex(FoodonetDBProvider.MembersDB.USER_ID_COLUMN));
            phoneNumber = c.getString(c.getColumnIndex(FoodonetDBProvider.MembersDB.USER_PHONE_COLUMN));
            name = c.getString(c.getColumnIndex(FoodonetDBProvider.MembersDB.USER_NAME_COLUMN));
            isAdmin = c.getShort(c.getColumnIndex(FoodonetDBProvider.MembersDB.IS_ADMIN_COLUMN)) == CommonConstants.VALUE_TRUE;

            members.add(new GroupMember(uniqueID,groupID,userID,phoneNumber,name,isAdmin));
        }
        if(c!=null){
            c.close();
        }
        return members;
    }

    /** @return an array with the groupID as key and value of the number of members of that group */
    public LongSparseArray<Integer> getAllGroupsMembersCount(){
        String[] selection = {FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.MembersDB.CONTENT_URI,selection,null,null,null);
        LongSparseArray<Integer> groupsMembers = new LongSparseArray<>();
        long groupID;
        Integer count;

        while(c!=null && c.moveToNext()){
            groupID = c.getLong(c.getColumnIndex(FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN));
            count = groupsMembers.get(groupID);
            if(count==null){
                groupsMembers.put(groupID,1);
            } else{
                groupsMembers.put(groupID,count+1);
            }
        }
        if(c!=null){
            c.close();
        }
        return groupsMembers;
    }

    /** get a specific group members */
    public ArrayList<GroupMember> getGroupMembers(long groupID){
        ArrayList<GroupMember> members = new ArrayList<>();
        String where = String.format("%1$s = ?",
                FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN);
        String[] whereArgs = {String.valueOf(groupID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.MembersDB.CONTENT_URI,null,where,whereArgs,null);
        long uniqueID,userID;
        String name, phoneNumber;
        boolean isAdmin;
        while(c!=null && c.moveToNext()){
            uniqueID = c.getLong(c.getColumnIndex(FoodonetDBProvider.MembersDB._ID_COLUMN));
            userID = c.getLong(c.getColumnIndex(FoodonetDBProvider.MembersDB.USER_ID_COLUMN));
            phoneNumber = c.getString(c.getColumnIndex(FoodonetDBProvider.MembersDB.USER_PHONE_COLUMN));
            name = c.getString(c.getColumnIndex(FoodonetDBProvider.MembersDB.USER_NAME_COLUMN));
            isAdmin = c.getShort(c.getColumnIndex(FoodonetDBProvider.MembersDB.IS_ADMIN_COLUMN)) == CommonConstants.VALUE_TRUE;

            members.add(new GroupMember(uniqueID,groupID,userID,phoneNumber,name,isAdmin));
        }
        if(c!=null){
            c.close();
        }
        return members;
    }


    public long getUserUniqueID(long groupID) {
        String[] projection = {FoodonetDBProvider.MembersDB._ID_COLUMN};
        String where = String.format("%1$s = ? AND %2$s = ?",FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN,FoodonetDBProvider.MembersDB.USER_ID_COLUMN);
        String[] whereArgs = {String.valueOf(groupID),String.valueOf(CommonMethods.getMyUserID(context))};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.MembersDB.CONTENT_URI,projection,where,whereArgs,null);
        long uniqueID = -1;
        if(c!=null && c.moveToNext()){
            uniqueID = c.getLong(c.getColumnIndex(FoodonetDBProvider.MembersDB._ID_COLUMN));
        }
        if(c!=null){
            c.close();
        }
        return uniqueID;
    }

    /** @return true if the member is in the group, false if not */
    public boolean isMemberInGroup(long groupID, long memberID){
        String[] selection = {FoodonetDBProvider.MembersDB.USER_ID_COLUMN};
        String where = String.format("%1$s = ? AND %2$s = ?",
                FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN,FoodonetDBProvider.MembersDB.USER_ID_COLUMN);
        String[] whereArgs = {String.valueOf(groupID),String.valueOf(memberID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.MembersDB.CONTENT_URI,selection,where,whereArgs,null);
        boolean inGroup = c != null && c.moveToNext();
        if(c!=null){
            c.close();
        }
        return inGroup;
    }

    public boolean isUserGroupAdmin(Context context, long groupID) {
        String[] selection = {FoodonetDBProvider.MembersDB.USER_ID_COLUMN};
        String where = String.format("%1$s = ? AND %2$s = ? AND %3$s = ?",
                FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN,
                FoodonetDBProvider.MembersDB.USER_ID_COLUMN,
                FoodonetDBProvider.MembersDB.IS_ADMIN_COLUMN);
        String[] whereArgs = {String.valueOf(groupID),String.valueOf(CommonMethods.getMyUserID(context)),String.valueOf(CommonConstants.VALUE_TRUE)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.MembersDB.CONTENT_URI,selection,where,whereArgs,null);
        boolean found = false;
        if(c!=null && c.moveToNext()){
            found = true;
        }
        if(c!=null){
            c.close();
        }
        return found;
    }

    public boolean isMemberInGroup(long groupID, String newPhone){
        String[] selection = {FoodonetDBProvider.MembersDB.USER_PHONE_COLUMN};
        String where = String.format("%1$s = ?",
                FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN);
        String[] whereArgs = {String.valueOf(groupID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.MembersDB.CONTENT_URI,selection,where,whereArgs,null);

        boolean inGroup = false;
        String phoneNumber;

        while(c!= null && c.moveToNext()){
            phoneNumber = c.getString(c.getColumnIndex(FoodonetDBProvider.MembersDB.USER_PHONE_COLUMN));
            if (CommonMethods.comparePhoneNumbers(newPhone,phoneNumber)){
                inGroup = true;
                break;
            }
        }
        if(c!=null){
            c.close();
        }
        return inGroup;
    }

    /** @return true if successfully added, false if the user was already in the group */
    public boolean insertMemberToGroup(long groupID, GroupMember member){
        /** first, check if the user is already in the group before adding */
        if(isMemberInGroup(groupID,member.getUserID())){
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(FoodonetDBProvider.MembersDB._ID_COLUMN,member.getUniqueID());
        values.put(FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN,member.getGroupID());
        values.put(FoodonetDBProvider.MembersDB.USER_ID_COLUMN,member.getUserID());
        values.put(FoodonetDBProvider.MembersDB.USER_NAME_COLUMN,member.getName());
        values.put(FoodonetDBProvider.MembersDB.USER_PHONE_COLUMN,member.getPhoneNumber());
        if(member.isAdmin()){
            values.put(FoodonetDBProvider.MembersDB.IS_ADMIN_COLUMN,CommonConstants.VALUE_TRUE);
        } else{
            values.put(FoodonetDBProvider.MembersDB.IS_ADMIN_COLUMN,CommonConstants.VALUE_FALSE);
        }
        context.getContentResolver().insert(FoodonetDBProvider.MembersDB.CONTENT_URI,values);
        return true;
    }

    /** replaces all groups members in the db */
    public void replaceAllGroupsMembers(ArrayList<GroupMember> members){
        /** first, delete all existing members */
        deleteAllGroupsMembers();

        ContentResolver resolver = context.getContentResolver();
        ContentValues values;
        GroupMember member;
        for (int i = 0; i < members.size(); i++) {
            values = new ContentValues();
            member = members.get(i);
            values.put(FoodonetDBProvider.MembersDB._ID_COLUMN,member.getUniqueID());
            values.put(FoodonetDBProvider.MembersDB.GROUP_ID_COLUMN,member.getGroupID());
            values.put(FoodonetDBProvider.MembersDB.USER_ID_COLUMN,member.getUserID());
            values.put(FoodonetDBProvider.MembersDB.USER_NAME_COLUMN,member.getName());
            values.put(FoodonetDBProvider.MembersDB.USER_PHONE_COLUMN,member.getPhoneNumber());
            if(member.isAdmin()){
                values.put(FoodonetDBProvider.MembersDB.IS_ADMIN_COLUMN,CommonConstants.VALUE_TRUE);
            } else{
                values.put(FoodonetDBProvider.MembersDB.IS_ADMIN_COLUMN,CommonConstants.VALUE_FALSE);
            }

            resolver.insert(FoodonetDBProvider.MembersDB.CONTENT_URI,values);
        }
    }

    /** deletes all groups members from db */
    public void deleteAllGroupsMembers(){
        context.getContentResolver().delete(FoodonetDBProvider.MembersDB.CONTENT_URI,null,null);
    }

    public void deleteGroupMember(long uniqueID){
        String where = String.format("%1$s = ?",FoodonetDBProvider.MembersDB._ID_COLUMN);
        String[] whereArgs = {String.valueOf(uniqueID)};
        context.getContentResolver().delete(FoodonetDBProvider.MembersDB.CONTENT_URI,where,whereArgs);
    }
}
