package com.roa.foodonetv3.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.GroupsActivity;
import com.roa.foodonetv3.activities.PublicationActivity;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.db.NotificationsDBHandler;
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.model.NotificationFoodonet;
import com.roa.foodonetv3.model.Publication;

import java.io.File;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.NotificationHolder> {

    private static final String TAG = "NotifRecyclerAdapter";

    private ArrayList<NotificationFoodonet> notifications;
    private Context context;
    private TransferUtility transferUtility;
    private NotificationsDBHandler notificationsDBHandler;
    private PublicationsDBHandler publicationsDBHandler;

    public NotificationsRecyclerAdapter(Context context) {
        this.context = context;
        notifications = new ArrayList<>();
        transferUtility = CommonMethods.getS3TransferUtility(context);
        notificationsDBHandler = new NotificationsDBHandler(context);
        publicationsDBHandler = new PublicationsDBHandler(context);
    }

    public void updateNotifications(){
        notifications.clear();
        notifications = notificationsDBHandler.getAllNotifications();
        notifyDataSetChanged();
    }

    public void clearNotifications() {
        notifications.clear();
        notifyDataSetChanged();
    }

    @Override
    public NotificationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_notifications,parent,false);
        return new NotificationHolder(v);
    }

    @Override
    public void onBindViewHolder(NotificationHolder holder, int position) {
        holder.bindNotification(position);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationHolder extends RecyclerView.ViewHolder implements TransferListener, View.OnClickListener {
        private CircleImageView imageNotification, imageNotificationType;
        private TextView textNotificationType, textNotificationName, textNotificationTime;
        private NotificationFoodonet notification;
        private File mCurrentPhotoFile;
        private int observerId, publicationVersion;
        private boolean isItemOnline;

        NotificationHolder(View itemView) {
            super(itemView);
            imageNotification = (CircleImageView) itemView.findViewById(R.id.imageNotification);
            imageNotificationType = (CircleImageView) itemView.findViewById(R.id.imageNotificationType);
            textNotificationType = (TextView) itemView.findViewById(R.id.textNotificationType);
            textNotificationName = (TextView) itemView.findViewById(R.id.textNotificationName);
            textNotificationTime = (TextView) itemView.findViewById(R.id.textNotificationTime);
            itemView.setOnClickListener(this);
        }

        void bindNotification(int position){
            mCurrentPhotoFile = null;
            publicationVersion = -1;
            isItemOnline = false;
            notification = notifications.get(position);
            textNotificationType.setText(notification.getTypeNotificationString(context));
            textNotificationType.setTextColor(ContextCompat.getColor(context,notification.getNotificationTextColor()));
            textNotificationName.setText(notification.getNameNotification());
            String timeAgo = CommonMethods.getTimeDifference(context,notification.getReceivedTime(),CommonMethods.getCurrentTimeSeconds(), CommonConstants.TIME_TYPE_AGO);
            textNotificationTime.setText(timeAgo);
            Glide.with(context).load(notification.getNotificationTypeImageResource()).into(imageNotificationType);
            switch (notification.getTypeNotification()){
                case NotificationFoodonet.NOTIFICATION_TYPE_NEW_PUBLICATION:
                case NotificationFoodonet.NOTIFICATION_TYPE_NEW_REGISTERED_USER:
                case NotificationFoodonet.NOTIFICATION_TYPE_PUBLICATION_DELETED:
                case NotificationFoodonet.NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT:
                    publicationVersion = publicationsDBHandler.getPublicationVersion(notification.getItemID());
                    isItemOnline = publicationVersion != -1;
                    String mCurrentPhotoFileString = CommonMethods.getFilePathFromPublicationID(context, notification.getItemID(), publicationVersion);
//                    if(!isItemOnline){
//                        Glide.with(context).load(R.drawable.camera_xxh).centerCrop().into(imageNotification);
//                    } else
                    if (mCurrentPhotoFileString!= null){
                        Glide.with(context).load(R.drawable.camera_xxh).centerCrop().into(imageNotification);
                        mCurrentPhotoFile = new File(mCurrentPhotoFileString);
                        if (mCurrentPhotoFile.isFile()) {
                            Glide.with(context).load(mCurrentPhotoFile).centerCrop().into(imageNotification);
                        }
                    } else {
                        Glide.with(context).load(R.drawable.camera_xxh).centerCrop().into(imageNotification);
                        String s3FileName = CommonMethods.getFileNameFromPublicationID(notification.getItemID(), publicationVersion);
                        TransferObserver observer = transferUtility.download(context.getResources().getString(R.string.amazon_publications_bucket),
                                s3FileName, mCurrentPhotoFile);
                        observer.setTransferListener(this);
                        observerId = observer.getId();
                    }
                    break;
                case NotificationFoodonet.NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP:
                    Glide.with(context).load(R.drawable.group_blue).centerCrop().into(imageNotification);
                    break;
            }
        }

        @Override
        public void onClick(View v) {
            switch (notification.getTypeNotification()){
                case NotificationFoodonet.NOTIFICATION_TYPE_NEW_PUBLICATION:
                case NotificationFoodonet.NOTIFICATION_TYPE_NEW_REGISTERED_USER:
                case NotificationFoodonet.NOTIFICATION_TYPE_NEW_PUBLICATION_REPORT:
                    if(isItemOnline){
                        Intent newPublicationIntent = new Intent(context, PublicationActivity.class);
                        newPublicationIntent.putExtra(PublicationActivity.ACTION_OPEN_PUBLICATION,PublicationActivity.PUBLICATION_DETAIL_TAG);
                        newPublicationIntent.putExtra(Publication.PUBLICATION_KEY,notification.getItemID());
                        newPublicationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(newPublicationIntent);
                    } else{
                        Toast.makeText(context, R.string.event_no_longer_online, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case NotificationFoodonet.NOTIFICATION_TYPE_PUBLICATION_DELETED:
                    Toast.makeText(context, R.string.event_no_longer_online, Toast.LENGTH_SHORT).show();
                    break;
                case NotificationFoodonet.NOTIFICATION_TYPE_NEW_ADDED_IN_GROUP:
                    Intent openGroupIntent = new Intent(context, GroupsActivity.class);
                    openGroupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(openGroupIntent);
                    break;
            }
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            /** listener for the s3 server download */
            Log.d(TAG, "amazon onStateChanged " + id + " " + state.toString());
            if (state == TransferState.COMPLETED) {
                if (observerId == id) {
                    Glide.with(context).load(mCurrentPhotoFile).centerCrop().into(imageNotification);
                }
            }
        }
        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        }
        @Override
        public void onError(int id, Exception ex) {
        }
    }
}
