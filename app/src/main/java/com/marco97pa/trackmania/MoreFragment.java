package com.marco97pa.trackmania;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.marco97pa.trackmania.utils.FLog;

public class MoreFragment extends Fragment {

    private static final String LOG_TAG = "MoreFragment";
    private FLog log = new FLog(LOG_TAG);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_more, container, false);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();


    }


}