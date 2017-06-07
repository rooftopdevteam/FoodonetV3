package com.foodonet.foodonet.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.foodonet.foodonet.R;

/**
 * custom dialog which handles input for the title of a new group being created, returning the title through OnNewGroupClickListener interface
 */
public class NewGroupDialog extends Dialog implements View.OnClickListener {
    private EditText editGroupName;
    private OnNewGroupClickListener listener;

    public NewGroupDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_new_group);
        listener = (OnNewGroupClickListener) context;
        editGroupName = (EditText) findViewById(R.id.editGroupName);
        setTitle(context.getResources().getString(R.string.dialog_new_group));
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        findViewById(R.id.buttonCreate).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonCancel:

                break;

            case R.id.buttonCreate:
                String groupName = editGroupName.getText().toString();
                if(groupName.equals("")){
                    Toast.makeText(getContext(), R.string.toast_please_enter_a_group_name, Toast.LENGTH_SHORT).show();
                    return;
                } else{
                    listener.onNewGroupClick(editGroupName.getText().toString());
                }
                break;
        }
        this.dismiss();
    }

    /**
     * interface for handling click event after creating a new group name
     */
    public interface OnNewGroupClickListener{
        /**
         * implement to get new title of new group created by the user and send to foodonet server
         * @param groupName String title of the new group
         */
        void onNewGroupClick(String groupName);
    }
}
