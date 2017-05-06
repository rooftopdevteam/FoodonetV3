package com.roa.foodonetv3.adapters;

import android.content.Context;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.PublicationActivity;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.commonMethods.OnReplaceFragListener;
import com.roa.foodonetv3.db.PublicationsDBHandler;
import com.roa.foodonetv3.db.RegisteredUsersDBHandler;
import com.roa.foodonetv3.model.Publication;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/** recycler adapter for publications */
public class PublicationsRecyclerAdapter extends RecyclerView.Adapter<PublicationsRecyclerAdapter.PublicationHolder> {
    private static final String TAG = "PubsRecyclerAdapter";

    private static final int PUBLICATION_VIEW = 1;
    private static final int PUBLICATION_SPACER = 2;

    private Context context;
    private ArrayList<Publication> filteredPublications = new ArrayList<>();
    private ArrayList<Publication> publications = new ArrayList<>();
    private LongSparseArray<Integer> registeredUsersArray = new LongSparseArray<>();
    private TransferUtility transferUtility;
    private LatLng userLatLng;
    private PublicationsDBHandler publicationsDBHandler;
    private OnReplaceFragListener onReplaceFragListener;
    private int sortType;

    public PublicationsRecyclerAdapter(Context context, int sortType) {
        this.context = context;
        this.sortType = sortType;
        onReplaceFragListener = (OnReplaceFragListener) context;
        /** get the S3 utility */
        transferUtility = CommonMethods.getS3TransferUtility(context);
        userLatLng = CommonMethods.getLastLocation(context);
    }

    /** updates the recycler */
    public void updatePublications(int typePublicationFilter){
        if(publicationsDBHandler == null){
            publicationsDBHandler = new PublicationsDBHandler(context);
        }
        RegisteredUsersDBHandler registeredUsersDBHandler = new RegisteredUsersDBHandler(context);
        ArrayList<Publication> publications = publicationsDBHandler.getPublications(typePublicationFilter, sortType);
        registeredUsersArray = registeredUsersDBHandler.getAllRegisteredUsersCount();
        filteredPublications.clear();
        filteredPublications.addAll(publications);
        this.publications = publications;
        notifyDataSetChanged();
    }

    /** filter through the search in the action bar */
    public void filter(String text) {
        filteredPublications.clear();
        if(text.isEmpty()){
            filteredPublications.addAll(publications);
        } else{
            text = text.toLowerCase();
            for(Publication publication: publications){
                if(publication.getTitle().toLowerCase().contains(text) || publication.getAddress().toLowerCase().contains(text)){
                    filteredPublications.add(publication);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(position== filteredPublications.size()){
            return PUBLICATION_SPACER;
        }
        return PUBLICATION_VIEW;
    }

    @Override
    public PublicationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType == PUBLICATION_VIEW){
            return new PublicationHolder(inflater.inflate(R.layout.item_publication_list,parent,false),viewType);
        }
        return new PublicationHolder(inflater.inflate(R.layout.item_list_spacer, parent, false),viewType);

    }

    @Override
    public void onBindViewHolder(PublicationHolder holder, int position) {
        if(getItemViewType(position)== PUBLICATION_VIEW){
            holder.bindPublication(filteredPublications.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return filteredPublications.size()+1;
    }

    class PublicationHolder extends RecyclerView.ViewHolder implements TransferListener, View.OnClickListener {
        private Publication publication;
        private ImageView imagePublicationGroup;
        private CircleImageView imagePublication;
        private TextView textPublicationTitle, textPublicationAddressDistance, textPublicationUsers;
        private File mCurrentPhotoFile;
        private int observerId;
        private int publicationImageSize;


        PublicationHolder(View itemView, int viewType) {
            super(itemView);
            if (viewType == PUBLICATION_VIEW) {
                imagePublication = (CircleImageView) itemView.findViewById(R.id.imagePublication);
                imagePublicationGroup = (ImageView) itemView.findViewById(R.id.imagePublicationGroup);
                textPublicationTitle = (TextView) itemView.findViewById(R.id.textPublicationTitle);
                textPublicationAddressDistance = (TextView) itemView.findViewById(R.id.textPublicationAddressDistance);
                textPublicationUsers = (TextView) itemView.findViewById(R.id.textPublicationUsers);
                publicationImageSize = (int) context.getResources().getDimension(R.dimen.image_size_68);
                itemView.setOnClickListener(this);
            }
        }

        private void bindPublication(Publication publication) {
            this.publication = publication;
            textPublicationTitle.setText(publication.getTitle());
            if (userLatLng.latitude != CommonConstants.LATLNG_ERROR && userLatLng.longitude != CommonConstants.LATLNG_ERROR) {
                double distance = CommonMethods.distance(userLatLng.latitude, userLatLng.longitude, publication.getLat(), publication.getLng());
                String addressDistance = String.format(Locale.US, "%1$s %2$s", CommonMethods.getRoundedStringFromNumber(distance), context.getResources().getString(R.string.km));
                textPublicationAddressDistance.setText(addressDistance);
            } else {
                textPublicationAddressDistance.setText("");
            }
            Integer numberRegisteredUsers = registeredUsersArray.get(publication.getId());
            if (numberRegisteredUsers == null) {
                numberRegisteredUsers = 0;
            }
            String registeredUsers = String.format(Locale.US, "%1$d %2$s", numberRegisteredUsers, context.getResources().getString(R.string.users_joined));
            textPublicationUsers.setText(registeredUsers);
            String mCurrentPhotoFileString = CommonMethods.getFilePathFromPublicationID(context, publication.getId(), publication.getVersion());
            if(mCurrentPhotoFileString!= null){
                mCurrentPhotoFile = new File(mCurrentPhotoFileString);
                if (mCurrentPhotoFile.isFile()) {
                    Glide.with(context).load(mCurrentPhotoFile).centerCrop().into(imagePublication);
                } else {
                    String s3FileName = CommonMethods.getFileNameFromPublicationID(publication.getId(), publication.getVersion());
                    TransferObserver observer = transferUtility.download(context.getResources().getString(R.string.amazon_publications_bucket),
                            s3FileName, mCurrentPhotoFile);
                    observer.setTransferListener(this);
                    observerId = observer.getId();
                }
            }
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            /** listener for the s3 server download */
            Log.d(TAG, "amazon onStateChanged " + id + " " + state.toString());
            if (state == TransferState.COMPLETED) {
                if (observerId == id) {
                    Glide.with(context).load(mCurrentPhotoFile).centerCrop().into(imagePublication);
                }
            }
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        }

        @Override
        public void onError(int id, Exception ex) {
            Log.d(TAG, "amazon onError" + id + " " + ex.toString());
        }

        @Override
        public void onClick(View v) {
            onReplaceFragListener.onReplaceFrags(PublicationActivity.PUBLICATION_DETAIL_TAG, publication.getId());
        }
    }
}

