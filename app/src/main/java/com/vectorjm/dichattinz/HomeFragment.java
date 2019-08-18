package com.vectorjm.dichattinz;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    public HomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity baseActivity = ((BaseActivity) getActivity());

        if (baseActivity == null) return;

        if (baseActivity.getSupportActionBar() == null) return;

        baseActivity.setTitle("Home");
        baseActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        baseActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
