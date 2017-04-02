package com.roa.foodonetv3.commonMethods;

public class ReceiverConstants {

    /** Receiver ACTION */
    public static final String BROADCAST_FOODONET = "broadcastFoodonet";

    /** ACTION_TYPE */
    public static final String ACTION_TYPE = "action_type";

    /** Server Extras */
    public static final String ADDRESS_ARGS = "addressArgs";
    public static final String JSON_TO_SEND = "jsonToSend";
    public static final String SERVICE_ERROR = "serviceError";

    public static final String REQUEST_IDENTIFIER = "request_identifier";
    public static final String DATA = "data";
    /** Server types */
    public static final int ACTION_GET_PUBLICATIONS = 1;
    public static final int ACTION_ADD_PUBLICATION = 2;
    public static final int ACTION_EDIT_PUBLICATION = 3;
    public static final int ACTION_DELETE_PUBLICATION = 4;
    public static final int ACTION_GET_PUBLICATION = 5;
    public static final int ACTION_GET_REPORTS = 10;
    public static final int ACTION_ADD_REPORT = 11;
    public static final int ACTION_ADD_USER = 20;
    public static final int ACTION_UPDATE_USER = 21;
    public static final int ACTION_REGISTER_TO_PUBLICATION = 30;
//    public static final int ACTION_GET_PUBLICATION_REGISTERED_USERS = 31;
    public static final int ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS = 32;
    public static final int ACTION_UNREGISTER_FROM_PUBLICATION = 33;
    public static final int ACTION_ADD_GROUP = 40;
    public static final int ACTION_GET_GROUPS = 41;
    public static final int ACTION_ADD_GROUP_MEMBER = 50;
    public static final int ACTION_DELETE_GROUP_MEMBER = 51;
    public static final int ACTION_POST_FEEDBACK = 60;
    public static final int ACTION_ACTIVE_DEVICE_NEW_USER = 70;


    /** Local types */
    public static final int ACTION_FAB_CLICK = 100;
    public static final int ACTION_GOT_DATA = 200;
    public static final int ACTION_GET_DATA = 201;
    public static final int ACTION_GOT_NEW_REPORT = 202;
    public static final int ACTION_ADD_ADMIN_MEMBER = 210;
    public static final int ACTION_SIGN_OUT = 300;

    public static final String FAB_TYPE = "fab_type";
    public static final String MEMBER_ADDED = "memberAdded";
    public static final String GROUP_ID = "groupID";
    public static final String PUBLICATION_REGISTERED_USERS = "publicationRegisteredUsers";
    public static final String USER_EXITED_GROUP = "userExitedGroup";
    public static final String UPDATE_DATA = "updateData";

    /** Local Extras*/
    public static final int FAB_TYPE_NEW_GROUP_MEMBER = 1;
    public static final int FAB_TYPE_SAVE_NEW_PUBLICATION = 2;
    public static final int FAB_TYPE_ADD_NEW_PUBLICATION = 3;
    public static final int FAB_TYPE_EDIT_PUBLICATION = 4;
    public static final int FAB_TYPE_REGISTER_TO_PUBLICATION = 5;
}
