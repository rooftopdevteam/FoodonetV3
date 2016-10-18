package com.roa.foodonetv3.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.roa.foodonetv3.R;

public class WelcomeUserActivity extends AppCompatActivity {
    ///test from roi

    private ImageView userImageView;
    private Button finishRegisterationButton;
    private TextView userNameTxt;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private String userName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_user);

        userImageView = (ImageView) findViewById(R.id.userImageView);
        finishRegisterationButton = (Button) findViewById(R.id.finishRegisterationButton);
        userNameTxt = (TextView) findViewById(R.id.userNameTxt);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        userName = mFirebaseUser.getDisplayName();

        //load the photo from fireBase
        Glide.with(this).load(mFirebaseUser.getPhotoUrl()).into(userImageView);
        userNameTxt.setText(userName);

        finishRegisterationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeUserActivity.this, MainDrawerActivity.class);
                startActivity(intent);
            }
        });




    }
}
