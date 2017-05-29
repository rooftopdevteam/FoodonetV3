package com.roa.foodonetv3.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;

public class ReportDialog extends Dialog implements View.OnClickListener {

    private RadioGroup radioGroup;
    private RatingBar ratingReport;
    private OnReportCreateListener listener;

    public ReportDialog(Context context, OnReportCreateListener listener, String publicationTitle) {
        super(context);
        setContentView(R.layout.dialog_report);

        this.listener = listener;

        TextView textPublicationTitle = (TextView) findViewById(R.id.textPublicationTitle);
        textPublicationTitle.setText(publicationTitle);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        ratingReport = (RatingBar) findViewById(R.id.ratingReport);

        findViewById(R.id.buttonCancel).setOnClickListener(this);
        findViewById(R.id.buttonSend).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonCancel:
                break;
            case R.id.buttonSend:
                short typeOfReport = -1;
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.radioHasMore:
                        typeOfReport = CommonConstants.REPORT_TYPE_HAS_MORE;
                        break;
                    case R.id.radioTookAll:
                        typeOfReport = CommonConstants.REPORT_TYPE_TOOK_ALL;
                        break;
                    case R.id.radioNothingThere:
                        typeOfReport = CommonConstants.REPORT_TYPE_NOTHING_THERE;
                        break;
                }
                if(typeOfReport == -1){
                    Toast.makeText(getContext(), R.string.dialog_please_choose_report, Toast.LENGTH_SHORT).show();
                    return;
                }
                listener.onReportCreate((int)ratingReport.getRating(),typeOfReport);
                break;
        }
        this.dismiss();
    }

    public interface OnReportCreateListener{
        void onReportCreate(int rating, short typeOfReport);
    }
}
