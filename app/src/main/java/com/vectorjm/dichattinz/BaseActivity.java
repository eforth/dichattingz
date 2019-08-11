package com.vectorjm.dichattinz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.baseContainer, new SplashFragment())
                .addToBackStack(null)
                .commit();
    }
}
