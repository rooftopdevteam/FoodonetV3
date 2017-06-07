package com.foodonet.foodonet.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonConstants;

/**
 * custom dialog for allowing the user to pick a report to be made for a publication the use is registered for
 * contains both report rating and report type
 * report rating - from 1 star to 5 stars
 * report type - has more to offer, took all, found nothing there
 * result should be received through implementing OnReportCreateListener
 */
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

    /**
     * interface for handling created report through the ReportDialog
     */
    public interface OnReportCreateListener{
        /**
         * implement to handle the created report for a publication
         * @param rating int of user rating, 1 to 5
         * @param typeOfReport short of the type of report, can be:
         *                     CommonConstants.REPORT_TYPE_HAS_MORE
         *                     CommonConstants.REPORT_TYPE_TOOK_ALL
         *                     CommonConstants.REPORT_TYPE_NOTHING_THERE
         */
        void onReportCreate(int rating, short typeOfReport);
    }
}
