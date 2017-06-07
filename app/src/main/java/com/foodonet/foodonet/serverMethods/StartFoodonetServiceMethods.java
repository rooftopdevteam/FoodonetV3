package com.foodonet.foodonet.serverMethods;

import android.content.Context;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonConstants;
import com.foodonet.foodonet.commonMethods.ReceiverConstants;

import java.util.Locale;

public class StartFoodonetServiceMethods {

    /**
     * handles getting the url for the requested server method
     * @param actionType int of the type of action from ReceiverConstants
     * @param args additional arguments for the creation of the url, see in ServerMethods for details
     * @return String of the url to use in the server request
     */
    public static String getUrlAddress(Context context, int actionType, String[] args) {
        // prepares the url address according to the action intended */
        StringBuilder builder = new StringBuilder();

        // add the server to foodonet */
        builder.append(context.getResources().getString(R.string.foodonet_server));
        switch (actionType){
            case ReceiverConstants.ACTION_GET_ONLINE_PUBLIC_PUBLICATIONS:
                builder.append(context.getString(R.string.foodonet_publications));
                break;
            case ReceiverConstants.ACTION_ADD_PUBLICATION:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_EDIT_PUBLICATION: // not tested
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_DELETE_PUBLICATION:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_GET_NEW_PUBLICATION:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_REPUBLISH_PUBLICATION:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_GET_REPORTS:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string.foodonet_reports_version));
                builder.append(args[1]);
                break;
            case ReceiverConstants.ACTION_ADD_REPORT: // not tested
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string.foodonet_publication_reports));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_ADD_USER:
                builder.append(context.getResources().getString(R.string.foodonet_users));
                break;
            case ReceiverConstants.ACTION_UPDATE_USER:
                builder.append(context.getResources().getString(R.string.foodonet_users));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                break;
            case ReceiverConstants.ACTION_REGISTER_TO_PUBLICATION:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string.foodonet_registered_user_for_publications));
                builder.append(context.getResources().getString(R.string._json));
                break;
//            case ReceiverConstants.ACTION_GET_PUBLICATION_REGISTERED_USERS:
//                builder.append(context.getResources().getString(R.string.foodonet_publications));
//                builder.append(String.format(Locale.US,"/%1$s",args[0]));
//                builder.append(context.getResources().getString(R.string.foodonet_registered_user_for_publications));
//                builder.append(context.getResources().getString(R.string._json));
//                break;
            case ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append("/1");
                builder.append(context.getResources().getString(R.string.foodonet_registered_user_for_publications));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_UNREGISTER_FROM_PUBLICATION:
                builder.append(context.getResources().getString(R.string.foodonet_publications));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string.foodonet_registered_user_for_publications));
                builder.append("/1?");
                builder.append(context.getResources().getString(R.string.foodonet_publication_version));
                builder.append(String.format("%1$s&%2$s=%3$s",
                        args[1],context.getResources().getString(R.string.foodonet_active_device_dev_uuid),args[2]));
                break;
            case ReceiverConstants.ACTION_ADD_GROUP:
                builder.append(context.getResources().getString(R.string.foodonet_groups));
                break;
            case ReceiverConstants.ACTION_GET_GROUPS:
                builder.append(context.getResources().getString(R.string.foodonet_users));
                builder.append(String.format(Locale.US,"/%1$s",args[0]));
                builder.append(context.getResources().getString(R.string.foodonet_groups));
                break;
            case ReceiverConstants.ACTION_POST_FEEDBACK:
                builder.append(context.getResources().getString(R.string.foodonet_feedback));
                break;
            case ReceiverConstants.ACTION_ADD_GROUP_MEMBER:
                builder.append(context.getResources().getString(R.string.foodonet_group_members));
                break;
            case ReceiverConstants.ACTION_DELETE_GROUP_MEMBER:
                builder.append(context.getResources().getString(R.string.foodonet_group_members));
                builder.append(String.format("/%1$s",args[0]));
                break;
            case ReceiverConstants.ACTION_ACTIVE_DEVICE_NEW_USER:
                builder.append(context.getResources().getString(R.string.foodonet_active_devices));
                builder.append(context.getResources().getString(R.string._json));
                break;
            case ReceiverConstants.ACTION_ACTIVE_DEVICE_UPDATE_USER_LOCATION:
                builder.append(context.getResources().getString(R.string.foodonet_active_devices_put));
                break;
            case ReceiverConstants.ACTION_GET_GROUP_ADMIN_IMAGE:
                builder.append(context.getString(R.string.foodonet_groups));
                builder.append(String.format("/%1$s/",args[0]));
                builder.append(context.getResources().getString(R.string.foodonet_group_members));
                break;
        }
        return builder.toString();
    }

    /** gets the appropriate https type
     * @param actionType int from ReceiverConstants
     * @return int (GET,POST,DELETE,PUT)*/
    public static int getHTTPType(int actionType){
        switch (actionType){
            case ReceiverConstants.ACTION_GET_ONLINE_PUBLIC_PUBLICATIONS:
                return CommonConstants.HTTP_GET;
            case ReceiverConstants.ACTION_ADD_PUBLICATION:
                return CommonConstants.HTTP_POST;
            case ReceiverConstants.ACTION_EDIT_PUBLICATION:
                return CommonConstants.HTTP_PUT;
            case ReceiverConstants.ACTION_DELETE_PUBLICATION:
                return CommonConstants.HTTP_DELETE;
            case ReceiverConstants.ACTION_TAKE_PUBLICATION_OFFLINE:
                return CommonConstants.HTTP_PUT;
            case ReceiverConstants.ACTION_GET_NEW_PUBLICATION:
                return CommonConstants.HTTP_GET;
            case ReceiverConstants.ACTION_REPUBLISH_PUBLICATION:
                return CommonConstants.HTTP_POST;
            case ReceiverConstants.ACTION_GET_REPORTS:
                return CommonConstants.HTTP_GET;
            case ReceiverConstants.ACTION_ADD_REPORT:
                return CommonConstants.HTTP_POST;
            case ReceiverConstants.ACTION_ADD_USER:
                return CommonConstants.HTTP_POST;
            case ReceiverConstants.ACTION_UPDATE_USER:
                return CommonConstants.HTTP_PUT;
            case ReceiverConstants.ACTION_REGISTER_TO_PUBLICATION:
                return CommonConstants.HTTP_POST;
//            case ReceiverConstants.ACTION_GET_PUBLICATION_REGISTERED_USERS:
//                return CommonConstants.HTTP_GET;
            case ReceiverConstants.ACTION_GET_ALL_PUBLICATIONS_REGISTERED_USERS:
                return CommonConstants.HTTP_GET;
            case ReceiverConstants.ACTION_UNREGISTER_FROM_PUBLICATION:
                return CommonConstants.HTTP_DELETE;
            case ReceiverConstants.ACTION_ADD_GROUP:
                return CommonConstants.HTTP_POST;
            case ReceiverConstants.ACTION_GET_GROUPS:
                return CommonConstants.HTTP_GET;
            case ReceiverConstants.ACTION_POST_FEEDBACK:
                return CommonConstants.HTTP_POST;
            case ReceiverConstants.ACTION_ADD_GROUP_MEMBER:
                return CommonConstants.HTTP_POST;
            case ReceiverConstants.ACTION_DELETE_GROUP_MEMBER:
                return CommonConstants.HTTP_DELETE;
            case ReceiverConstants.ACTION_ACTIVE_DEVICE_NEW_USER:
                return CommonConstants.HTTP_POST;
            case ReceiverConstants.ACTION_ACTIVE_DEVICE_UPDATE_USER_LOCATION:
                return CommonConstants.HTTP_PUT;
            case ReceiverConstants.ACTION_GET_GROUP_ADMIN_IMAGE:
                return CommonConstants.HTTP_GET;
        }
        return -1;
    }
}
