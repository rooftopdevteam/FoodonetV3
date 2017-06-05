package com.foodonet.foodonet.adapters;

import android.content.Context;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.activities.GroupsActivity;
import com.foodonet.foodonet.commonMethods.OnReplaceFragListener;
import com.foodonet.foodonet.db.GroupMembersDBHandler;
import com.foodonet.foodonet.model.Group;
import java.util.ArrayList;

/** recycler for groups */
public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.GroupHolder> {
    private static final int GROUP_VIEW = 1;
    private static final int GROUP_SPACER = 2;

    private Context context;
    private ArrayList<Group> groups;
    private ArrayList<Group> filteredGroups;
    private LongSparseArray<Integer> groupsMembersCount = new LongSparseArray<>();
    private OnReplaceFragListener listener;
    private GroupMembersDBHandler groupMembersDBHandler;

    public GroupsRecyclerAdapter(Context context) {
        this.context = context;
        groups = new ArrayList<>();
        filteredGroups = new ArrayList<>();
        groupMembersDBHandler = new GroupMembersDBHandler(context);
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

    /** filter through the search in the action bar , currently not implemented*/
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
    public int getItemViewType(int position) {
        if(position==filteredGroups.size()){
            return GROUP_SPACER;
        }
        return GROUP_VIEW;
    }

    @Override
    public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType == GROUP_VIEW){
            return new GroupHolder(inflater.inflate(R.layout.item_group_list,parent,false),viewType);
        }
        return new GroupHolder(inflater.inflate(R.layout.item_list_spacer,parent,false),viewType);
    }

    @Override
    public void onBindViewHolder(GroupHolder holder, int position) {
        if(getItemViewType(position) == GROUP_VIEW){
            holder.bindGroup(filteredGroups.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return filteredGroups.size()+1;
    }

    class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView textGroupName, textGroupMembers, textAdmin;
        private Group group;
        private boolean isAdmin;

        GroupHolder(View itemView, int viewType) {
            super(itemView);
            if(viewType == GROUP_VIEW){
                textAdmin = (TextView) itemView.findViewById(R.id.textAdmin);
                textGroupName = (TextView) itemView.findViewById(R.id.textGroupName);
                textGroupMembers = (TextView) itemView.findViewById(R.id.textGroupMembers);

                itemView.setOnClickListener(this);
            }
        }

        void bindGroup(Group group){
            this.group = group;
            isAdmin = groupMembersDBHandler.isUserGroupAdmin(context, group.getGroupID());
            textGroupName.setText(group.getGroupName());
            Integer membersCount = groupsMembersCount.get(group.getGroupID());
            if(membersCount == null){
                membersCount = 0;
            }
            textGroupMembers.setText(String.valueOf(membersCount));
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
                        fragToOpen = GroupsActivity.NON_ADMIN_GROUP_TAG;
                    }
                    // run the method on the listener to change the fragment
                    listener.onReplaceFrags(fragToOpen,group.getGroupID());
                    break;
            }
        }
    }
}
