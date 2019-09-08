package com.vectorjm.dichattinz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.baseContainer, new SplashFragment())
                .addToBackStack(null)
                .commit();
    }
}
