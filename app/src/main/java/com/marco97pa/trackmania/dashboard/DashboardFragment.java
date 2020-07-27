package com.marco97pa.trackmania.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.marco97pa.trackmania.R;
import com.marco97pa.trackmania.utils.FLog;

public class DashboardFragment extends Fragment {

    private static final String LOG_TAG = "DashboardFragment";
    private FLog log = new FLog(LOG_TAG);

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);

        MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = root.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //TODO: GET /api/token/campaign/official?offset=0&length=1 SOLO LA PRIMA
        //TODO: GET /api/token/leaderboard/group/groupId/ https://github.com/The-Firexx/trackmania2020apidocumentation/blob/master/LiveServices.md
        //TODO: GET /api/token/leaderboard/group/groupId/map
        return root;
    }
}
