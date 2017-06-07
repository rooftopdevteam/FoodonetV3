package com.foodonet.foodonet.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

import com.foodonet.foodonet.R;
import com.foodonet.foodonet.serverMethods.ServerMethods;

/**
 * custom dialog for handling the user contacting the foodonet team through "contact us" (feedback in the server)
 */
public class ContactUsDialog extends Dialog implements View.OnClickListener {

    private EditText contactEditText;
    private Context context;

    public ContactUsDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_contact_us);
        this.context = context;
        contactEditText = (EditText) findViewById(R.id.contactEditText);
        setTitle(context.getResources().getString(R.string.drawer_contact_us));
        findViewById(R.id.buttonCancel).setOnClickListener(this);
        findViewById(R.id.buttonSend).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonCancel:
                break;

            case R.id.buttonSend:
                String message = contactEditText.getText().toString();
                ServerMethods.postFeedback(context,message);
                break;
        }
        this.dismiss();
    }
}
