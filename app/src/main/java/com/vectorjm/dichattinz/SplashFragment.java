package com.vectorjm.dichattinz;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;

public class SplashFragment extends Fragment {

    private FirebaseAuth auth;


    public SplashFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadScreen();
    }

    private void loadScreen() {

        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.baseContainer, checkAuth())
                        .addToBackStack(null)
                        .commit();
            }
        }, 2000);
    }

    private Fragment checkAuth() {

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            return new HomeFragment();
        }

        return new LoginFragment();
    }
}
