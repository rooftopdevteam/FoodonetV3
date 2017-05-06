package com.roa.foodonetv3.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.Publication;
import java.io.File;
import java.util.ArrayList;

/** recycler for horizontal overlay in the map of publication */
public class MapPublicationRecyclerAdapter extends RecyclerView.Adapter<MapPublicationRecyclerAdapter.PublicationHolder> {

    private static final String TAG = "MapPubsRecyclerAdapter";
    // TODO: 25/01/2017 move to static class
    private static final int publicationImageSize = 120;
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

    class PublicationHolder extends RecyclerView.ViewHolder {

        private ImageView mapRecyclerImageView;
        private File mCurrentPhotoFile;
        private int observerId;

        PublicationHolder(View itemView) {
            super(itemView);
            mapRecyclerImageView = (ImageView) itemView.findViewById(R.id.mapRecyclerImageView);
            mapRecyclerImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onImageAdapterClicked(new LatLng(publications.get(getLayoutPosition()).getLat(),publications.get(getLayoutPosition()).getLng()));
                }
            });
        }

        void bindPublication(Publication publication){
            String mCurrentPhotoFileString = CommonMethods.getFilePathFromPublicationID(context,publication.getId(),publication.getVersion());
            if(mCurrentPhotoFileString!= null){
                File mCurrentPhotoFile = new File(mCurrentPhotoFileString);
                if(mCurrentPhotoFile.isFile()){
                    // there's an image path, try to load from file */
                    Log.d(TAG,"layout size: "+mapRecyclerImageView.getWidth()+","+mapRecyclerImageView.getHeight());
                    Glide.with(context).load(mCurrentPhotoFile).centerCrop().into(mapRecyclerImageView);
            }
            } else{
                // load default image */
                Glide.with(context).load(R.drawable.foodonet_image).centerCrop().into(mapRecyclerImageView);
            }
        }

//        @Override
//        public void onStateChanged(int id, TransferState state) {
//            /** listener for the s3 server download, needs to be class wide since it's currently keeps using the same image in different layout */
//            // TODO: 09/11/2016 check picasso adapter for the images and using the s3 observer on an adapter scale
//            Log.d(TAG,"amazon onStateChanged " + id + " "  + state.toString());
//            if(state == TransferState.COMPLETED){
//                if(observerId==id){
//                    Picasso.with(context)
//                            .load(mCurrentPhotoFile)
//                            .resize(publicationImageSize,publicationImageSize)
//                            .centerCrop()
//                            .into(mapRecyclerImageView);
//                }
//
//            }
//        }
//
//        @Override
//        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//        }
//        @Override
//        public void onError(int id, Exception ex) {
//            Log.d(TAG,"amazon onError" + id + " " + ex.toString());
//        }

    }
    public interface OnImageAdapterClickListener{
        void onImageAdapterClicked(LatLng latLng);
    }
}
