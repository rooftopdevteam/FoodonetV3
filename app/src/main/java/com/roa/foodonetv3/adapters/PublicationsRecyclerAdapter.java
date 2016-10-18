package com.roa.foodonetv3.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.model.Publication;
import java.util.ArrayList;

/**
 * Created by Owner on 05/10/2016.
 */

public class PublicationsRecyclerAdapter extends RecyclerView.Adapter<PublicationsRecyclerAdapter.PublicationHolder> {
    private Context context;
    private ArrayList<Publication> publications = new ArrayList<>();

    public PublicationsRecyclerAdapter(Context context) {
        this.context = context;
    }

    public void updatePublications(ArrayList<Publication> publications){
        this.publications = publications;
        notifyDataSetChanged();
    }

    @Override
    public PublicationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.publication_list_item,parent,false);
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

    class PublicationHolder extends RecyclerView.ViewHolder{
        private Publication publication;
        private TextView textId, textTitle, textSubtitle, textAddress;

        PublicationHolder(View itemView) {
            super(itemView);
            textId = (TextView) itemView.findViewById(R.id.textPublicationItemId);
            textTitle = (TextView) itemView.findViewById(R.id.textPublicationItemTitle);
            textSubtitle = (TextView) itemView.findViewById(R.id.textPublicationItemSubtitle);
            textAddress = (TextView) itemView.findViewById(R.id.textPublicationItemAddress);
        }

        private void bindPublication(Publication publication) {
            this.publication = publication;
            textId.setText("ID: " + publication.getId());
            textTitle.setText("Title: " + publication.getTitle());
            textSubtitle.setText("Subtitle: " + publication.getSubtitle());
            textAddress.setText("Address: " + publication.getAddress());
        }
    }
}

