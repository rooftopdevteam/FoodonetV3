package com.foodonet.foodonet.activities;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.foodonet.foodonet.R;

public class AboutUsActivity extends AppCompatActivity {

    private static final String TAG = "AboutUsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        TextView textAppVersion = (TextView) findViewById(R.id.textAppVersion);
        String appVersion = "";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),0);
            appVersion = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,e.getMessage());
        }
        textAppVersion.setText(String.format("v %1$s",appVersion));

        // TODO: 21/12/2016 add facebook button for "Like"
    }
}
