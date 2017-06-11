package com.foodonet.foodonet.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.model.Publication;
import java.io.File;
import java.util.ArrayList;

/** recycler for horizontal overlay in the map of publication */
public class MapPublicationRecyclerAdapter extends RecyclerView.Adapter<MapPublicationRecyclerAdapter.PublicationHolder> {

    private static final String TAG = "MapPubsRecyclerAdapter";
    private Context context;
    private ArrayList<Publication> publications = new ArrayList<>();
    private TransferUtility transferUtility;
    private OnImageAdapterClickListener listener;


    public MapPublicationRecyclerAdapter(Context context){
        this.context = context;
        transferUtility = CommonMethods.getS3TransferUtility(context);
        listener = (OnImageAdapterClickListener) context;

    }

    public void updatePublications(ArrayList<Publication> publications){
        this.publications = publications;
        notifyDataSetChanged();
    }

    @Override
    public PublicationHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_map_publication, parent, false);
        return new PublicationHolder(v);
    }

    @Override
    public void onBindViewHolder(PublicationHolder holder, int position) {
        holder.bindPublication(publications.get(position));
    }

    @Override
    public int getItemCount() {
        return publications.size();
    }

    class PublicationHolder extends RecyclerView.ViewHolder implements TransferListener, View.OnClickListener {

        private ImageView mapRecyclerImageView;
        private File mCurrentPhotoFile;
        private int observerId, failCount;

        PublicationHolder(View itemView) {
            super(itemView);
            mapRecyclerImageView = (ImageView) itemView.findViewById(R.id.imagePublication);
            mapRecyclerImageView.setOnClickListener(this);
        }

        void bindPublication(Publication publication){
            failCount = 3;
            String mCurrentPhotoFileString = CommonMethods.getFilePathFromPublicationID(context,publication.getId(),publication.getVersion());
            if(mCurrentPhotoFileString!= null){
                mCurrentPhotoFile = new File(mCurrentPhotoFileString);
                if(mCurrentPhotoFile.isFile()){
                    // there's an image path, try to load from file */
                    Glide.with(context).load(mCurrentPhotoFile).centerCrop().into(mapRecyclerImageView);
                } else{
                    String s3FileName = CommonMethods.getFileNameFromPublicationID(publication.getId(), publication.getVersion());
                    TransferObserver observer = transferUtility.download(context.getResources().getString(R.string.amazon_publications_bucket),
                            s3FileName, mCurrentPhotoFile);
                    observer.setTransferListener(this);
                    observerId = observer.getId();
                    // load default image */
                    Glide.with(context).load(R.drawable.foodonet_image).centerCrop().into(mapRecyclerImageView);
                }
            } else{
                Glide.with(context).load(R.drawable.foodonet_image).centerCrop().into(mapRecyclerImageView);
            }
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d(TAG, "amazon onStateChanged " + id + " " + state.toString());
            if (state == TransferState.COMPLETED) {
                if (observerId == id && mapRecyclerImageView.isShown()) {
                    Glide.with(context).load(mCurrentPhotoFile).centerCrop().into(mapRecyclerImageView);
                } else if(state == TransferState.FAILED){
                    if(failCount >= 0){
                        failCount--;
                        transferUtility.resume(observerId);
                    }
                }
            }
        }
        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        }
        @Override
        public void onError(int id, Exception ex) {
        }

        @Override
        public void onClick(View v) {
            listener.onImageAdapterClicked(new LatLng(publications.get(getLayoutPosition()).getLat(),publications.get(getLayoutPosition()).getLng()));
        }
    }
    public interface OnImageAdapterClickListener{
        void onImageAdapterClicked(LatLng latLng);
    }
}
