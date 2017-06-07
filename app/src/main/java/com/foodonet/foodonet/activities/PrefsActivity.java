package com.foodonet.foodonet.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonMethods;
import com.foodonet.foodonet.fragments.PrefsFragment;

/** preferences screen */
public class PrefsActivity extends AppCompatActivity implements PrefsFragment.OnSignOutClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prefs);
    }

    @Override
    public void onSignOutClick() {
        CommonMethods.signOffUser(this);
        finish();
    }
}
