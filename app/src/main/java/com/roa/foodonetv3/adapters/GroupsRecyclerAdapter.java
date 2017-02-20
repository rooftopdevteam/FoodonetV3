package com.roa.foodonetv3.adapters;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.activities.GroupsActivity;
import com.roa.foodonetv3.commonMethods.OnReplaceFragListener;
import com.roa.foodonetv3.db.GroupMembersDBHandler;
import com.roa.foodonetv3.model.Group;
import java.util.ArrayList;

/** recycler for groups */
public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.GroupHolder> {

    private static final String TAG = "GroupsRecyclerAdapter";

    private Context context;
    private ArrayList<Group> groups;
    private ArrayList<Group> filteredGroups;
    private LongSparseArray<Integer> groupsMembersCount = new LongSparseArray<>();
    private OnReplaceFragListener listener;

    public GroupsRecyclerAdapter(Context context) {
        this.context = context;
        groups = new ArrayList<>();
        filteredGroups = new ArrayList<>();
        listener = (OnReplaceFragListener) context;
    }

    public void updateGroups(ArrayList<Group> groups){
        GroupMembersDBHandler handler = new GroupMembersDBHandler(context);
        groupsMembersCount = handler.getAllGroupsMembersCount();
        filteredGroups.clear();
        filteredGroups.addAll(groups);
        this.groups = groups;
        notifyDataSetChanged();
    }

    /** filter through the search in the action bar */
    // TODO: 21/12/2016 currently not implemented
    public void filter(String text){
        filteredGroups.clear();
        if(text.isEmpty()){
            filteredGroups.addAll(groups);
        } else{
            text = text.toLowerCase();
            for (Group group : groups) {
                if(group.getGroupName().contains(text)){
                    filteredGroups.add(group);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_group_list,parent,false);
        return new GroupHolder(v);
    }

    @Override
    public void onBindViewHolder(GroupHolder holder, int position) {
        holder.bindGroup(filteredGroups.get(position));
    }

    @Override
    public int getItemCount() {
        return filteredGroups.size();
    }

    class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textGroupName, textGroupMembers, textAdmin;
        private Group group;
        private boolean isAdmin = true; // test!!!!

        GroupHolder(View itemView) {
            super(itemView);
            textAdmin = (TextView) itemView.findViewById(R.id.textAdmin);
            textGroupName = (TextView) itemView.findViewById(R.id.textGroupName);
            textGroupMembers = (TextView) itemView.findViewById(R.id.textGroupMembers);

            itemView.setOnClickListener(this);
        }

        void bindGroup(Group group){
            this.group = group;
            textGroupName.setText(group.getGroupName());
            Integer membersCount = groupsMembersCount.get(group.getGroupID());
            if(membersCount == null){
                membersCount = 0;
            }
            textGroupMembers.setText(String.valueOf(membersCount));
            // TODO: 07/12/2016 add logic to check if the user is the admin
            if(isAdmin){
                textAdmin.setVisibility(View.VISIBLE);
            } else{
                textAdmin.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                default:
                    String fragToOpen;
                    if(isAdmin){
                        fragToOpen = GroupsActivity.ADMIN_GROUP_TAG;
                    } else{
                        fragToOpen = GroupsActivity.OPEN_GROUP_TAG;
                    }
                    ArrayList<Parcelable> arrayList = new ArrayList<>();
                    arrayList.add(group);
                    /** run the method on the listener to change the fragment */
                    listener.replaceFrags(fragToOpen,arrayList);
                    break;
            }
        }
    }
}
