package com.foodonet.foodonet.activities;

import android.animation.ObjectAnimator;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import com.foodonet.foodonet.R;
import com.foodonet.foodonet.commonMethods.CommonConstants;

public class SplashForCamera extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_for_camera);

        // set animation of message to rotate the phone to take the picture, and rotate the message
        TextView textSplashMessageForCamera = (TextView) findViewById(R.id.textSplashMessageForCamera);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(textSplashMessageForCamera,"rotation",90);
        objectAnimator.setDuration((int)(CommonConstants.SPLASH_CAMERA_TIME /1.5));
        objectAnimator.start();

        // exit after timer ends
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, CommonConstants.SPLASH_CAMERA_TIME);
    }
}
