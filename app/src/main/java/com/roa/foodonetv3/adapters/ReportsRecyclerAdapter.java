package com.roa.foodonetv3.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.roa.foodonetv3.R;
import com.roa.foodonetv3.commonMethods.CommonConstants;
import com.roa.foodonetv3.commonMethods.CommonMethods;
import com.roa.foodonetv3.model.PublicationReport;
import java.util.ArrayList;
import java.util.Locale;

/** recycler for publication reports */
public class ReportsRecyclerAdapter extends RecyclerView.Adapter<ReportsRecyclerAdapter.ReportsHolder> {
    private static final String TAG = "ReportsRecyclerAdapter";

    private static final int REPORT_VIEW = 1;
    private static final int REPORT_SPACER = 2;
    private Context context;
    private ArrayList<PublicationReport> reports;

    public ReportsRecyclerAdapter(Context context) {
        this.context = context;
        reports = new ArrayList<>();
    }

    public void updateReports(ArrayList<PublicationReport> reports){
        this.reports = reports;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        /** adding a spacer in the bottom so that the fab won't hide the last one */
        if(position==reports.size()){
            return REPORT_SPACER;
        } return REPORT_VIEW;
    }

    @Override
    public ReportsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if(viewType == REPORT_VIEW){
            return new ReportsHolder(inflater.inflate(R.layout.item_report_list,parent,false),viewType);
        } else{
            return new ReportsHolder(inflater.inflate(R.layout.item_list_spacer,parent,false),viewType);
        }
    }

    @Override
    public void onBindViewHolder(ReportsHolder holder, int position) {
        if(getItemViewType(position)==REPORT_VIEW){
            holder.bindReport(reports.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return reports.size()+1;
    }

    class ReportsHolder extends RecyclerView.ViewHolder{
        private TextView textReport;

        ReportsHolder(View itemView, int viewType) {
            super(itemView);
            if(viewType == REPORT_VIEW){
                textReport = (TextView) itemView.findViewById(R.id.textReport);
            }
        }

        void bindReport(PublicationReport report){
            /** set the message */
            textReport.setText(String.format(Locale.US,"%1$s - (%2$s)",
                    CommonMethods.getReportStringFromType(context,report.getReportType()),
                    CommonMethods.getTimeDifference(context,Double.parseDouble(report.getDateOfReport()),
                            CommonMethods.getCurrentTimeSeconds(), CommonConstants.TIME_TYPE_AGO)));
        }
    }
}
