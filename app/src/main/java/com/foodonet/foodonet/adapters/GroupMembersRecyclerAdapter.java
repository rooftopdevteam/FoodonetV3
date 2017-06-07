package com.foodonet.foodonet.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.db.GroupMembersDBHandler;
import com.foodonet.foodonet.model.GroupMember;
import com.foodonet.foodonet.serverMethods.ServerMethods;

import java.util.ArrayList;

/** recycler for groupsMembers */
public class GroupMembersRecyclerAdapter extends RecyclerView.Adapter<GroupMembersRecyclerAdapter.MemberHolder>{
    private static final int GROUP_MEMBER_VIEW = 1;
    private static final int GROUP_MEMBER_SPACER = 2;

    private Context context;
    private ArrayList<GroupMember> members = new ArrayList<>();
    private boolean isUserGroupAdmin;

    public GroupMembersRecyclerAdapter(Context context) {
        this.context = context;
    }

    public void updateMembers(long groupID){
        GroupMembersDBHandler handler = new GroupMembersDBHandler(context);
        this.members = handler.getGroupMembers(groupID);
        isUserGroupAdmin = handler.isUserGroupAdmin(context,groupID);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(position == members.size()){
            return GROUP_MEMBER_SPACER;
        }
        return GROUP_MEMBER_VIEW;
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType == GROUP_MEMBER_VIEW){
            return new MemberHolder(inflater.inflate(R.layout.item_group_member,parent,false),viewType);
        }
        return new MemberHolder(inflater.inflate(R.layout.item_list_spacer,parent,false),viewType);
    }

    @Override
    public void onBindViewHolder(MemberHolder holder, int position) {
        if(getItemViewType(position) == GROUP_MEMBER_VIEW){
            holder.bindMember(members.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return members.size()+1;
    }

    class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private GroupMember member;
        private ImageView imageMember, imageRemoveMember;
        private TextView textMemberName, textAdmin;

        MemberHolder(View itemView, int viewType) {
            super(itemView);
            if(viewType == GROUP_MEMBER_VIEW){
                imageMember = (ImageView) itemView.findViewById(R.id.imageMember);
                textMemberName = (TextView) itemView.findViewById(R.id.textMemberName);
                textAdmin = (TextView) itemView.findViewById(R.id.textAdmin);
                imageRemoveMember = (ImageView) itemView.findViewById(R.id.imageRemoveMember);
            }
        }

        void bindMember(GroupMember member){
            this.member = member;
            textMemberName.setText(member.getName());
            imageMember.setImageResource(member.getMemberTypeImage());
            if(member.isAdmin()){
                textAdmin.setVisibility(View.VISIBLE);
                imageRemoveMember.setVisibility(View.INVISIBLE);
                imageRemoveMember.setClickable(false);
                imageRemoveMember.setOnClickListener(null);
            } else{
                textAdmin.setVisibility(View.GONE);
                if(isUserGroupAdmin) {
                    imageRemoveMember.setVisibility(View.VISIBLE);
                    imageRemoveMember.setClickable(true);
                    imageRemoveMember.setOnClickListener(this);
                } else{
                    imageRemoveMember.setVisibility(View.INVISIBLE);
                    imageRemoveMember.setClickable(false);
                    imageRemoveMember.setOnClickListener(null);
                }
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.imageRemoveMember:
                    AlertDialog.Builder removeMemberAlertDialogBuilder = new AlertDialog.Builder(context)
                            .setTitle(R.string.dialog_are_you_sure)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ServerMethods.deleteGroupMember(context,member.getUniqueID(),false,member.getGroupID());
                                }
                            })
                            .setNegativeButton(R.string.no, null);
                    removeMemberAlertDialogBuilder.show();
                    break;
            }
        }
    }
}
