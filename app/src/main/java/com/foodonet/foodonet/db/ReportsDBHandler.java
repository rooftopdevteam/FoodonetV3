package com.foodonet.foodonet.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.foodonet.foodonet.model.PublicationReport;

import java.util.ArrayList;

/**
 * @deprecated currently not in use, as it is preferable to load each publication reports in publication detail from the foodonet server
 */
public class ReportsDBHandler {
    private Context context;

    public ReportsDBHandler(Context context) {
        this.context = context;
    }

    /** get all reports for a specific publication */
    public ArrayList<PublicationReport> getReportsForPublication(long publicationID){
        ArrayList<PublicationReport> reports = new ArrayList<>();

        String where = String.format("%1$s = ?" ,FoodonetDBProvider.ReportsDB.PUBLICATION_ID_COLUMN);
        String[] whereArgs = {String.valueOf(publicationID)};
        Cursor c = context.getContentResolver().query(FoodonetDBProvider.ReportsDB.CONTENT_URI,null,where,whereArgs,null);
        long reportId, reportUserID;
        int publicationVersion, rating;
        short reportType;
        String dateOfReport, reportUserName; //createdDate, updateDate

        while(c!= null && c.moveToNext()){
            reportId = c.getLong(c.getColumnIndex(FoodonetDBProvider.ReportsDB.REPORT_ID_COLUMN));
            publicationVersion = c.getInt(c.getColumnIndex(FoodonetDBProvider.ReportsDB.PUBLICATION_VERSION_COLUMN));
            reportType = c.getShort(c.getColumnIndex(FoodonetDBProvider.ReportsDB.REPORT_COLUMN));
            dateOfReport = c.getString(c.getColumnIndex(FoodonetDBProvider.ReportsDB.TIME_OF_REPORT_COLUMN));
            reportUserName = c.getString(c.getColumnIndex(FoodonetDBProvider.ReportsDB.USER_NAME_COLUMN));
            reportUserID = c.getLong(c.getColumnIndex(FoodonetDBProvider.ReportsDB.USER_ID_COLUMN));
            rating = c.getInt(c.getColumnIndex(FoodonetDBProvider.ReportsDB.REPORT_RATING_COLUMN));

            reports.add(new PublicationReport(reportId,publicationID,publicationVersion,reportType,null,dateOfReport,reportUserName,null,reportUserID,rating));
        }
        if(c!=null){
            c.close();
        }
        return reports;
    }

    /** replace all reports from db */
    public void replaceAllReports(ArrayList<PublicationReport> reports){
        // delete old reports
        deleteAllReports();

        ContentResolver resolver = context.getContentResolver();
        // declarations
        PublicationReport report;
        ContentValues values;
        for (int i = 0; i < reports.size(); i++){
            values = new ContentValues();
            report = reports.get(i);
            values.put(FoodonetDBProvider.ReportsDB.REPORT_ID_COLUMN,report.getReportUserID());
            values.put(FoodonetDBProvider.ReportsDB.PUBLICATION_VERSION_COLUMN,report.getPublicationVersion());
            values.put(FoodonetDBProvider.ReportsDB.REPORT_COLUMN,report.getReportType());
            values.put(FoodonetDBProvider.ReportsDB.TIME_OF_REPORT_COLUMN,report.getDateOfReport());
            values.put(FoodonetDBProvider.ReportsDB.USER_NAME_COLUMN,report.getReportUserName());
            values.put(FoodonetDBProvider.ReportsDB.USER_ID_COLUMN,report.getReportUserID());
            values.put(FoodonetDBProvider.ReportsDB.REPORT_RATING_COLUMN,report.getRating());

            resolver.insert(FoodonetDBProvider.ReportsDB.CONTENT_URI,values);
        }
    }

    /** delete all reports from db */
    public void deleteAllReports(){
        context.getContentResolver().delete(FoodonetDBProvider.ReportsDB.CONTENT_URI,null,null);
    }

}
