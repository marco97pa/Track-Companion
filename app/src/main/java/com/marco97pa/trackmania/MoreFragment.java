package com.marco97pa.trackmania;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.marco97pa.trackmania.utils.FLog;

public class MoreFragment extends Fragment implements RewardedVideoAdListener {

    private static final String LOG_TAG = "MoreFragment";
    private FLog log = new FLog(LOG_TAG);
    private String API;
    private TextView APIverText, betaDesc;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private RewardedVideoAd mRewardedVideoAd;
    int taps = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_more, container, false);

        MobileAds.initialize(getActivity(), getString(R.string.admob_app_id));
        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getActivity());
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        TextView appVer = root.findViewById(R.id.app_ver);
        appVer.setText(BuildConfig.VERSION_NAME);

        APIverText = root.findViewById(R.id.api_ver);

        betaDesc = root.findViewById(R.id.betaDesc);

        LinearLayout bug = root.findViewById(R.id.report_bug);
        bug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBugEmail();
            }
        });

        LinearLayout api = root.findViewById(R.id.api_layout);
        api.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAPIDialog(getActivity());
            }
        });

        LinearLayout logout = root.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        Button shareButton = root.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareAppLink();
            }
        });

        final Button adButton = root.findViewById(R.id.adButton);
        adButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRewardedVideoAd.isLoaded()) {
                    adButton.setEnabled(false);
                    mRewardedVideoAd.show();
                }
                else{
                    Toast.makeText(getActivity(), getString(R.string.retry), Toast.LENGTH_LONG).show();
                }
            }
        });

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            log.d( "Config params updated: " + updated);
                            checkSupportedApi();
                        } else {
                            log.d( "Config fetch failed");
                        }
                    }
                });

        //Sets version name easter egg
        LinearLayout APPver = (LinearLayout) root.findViewById(R.id.app_ver_layout);
        APPver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (taps == 7) {
                    Toast.makeText(getActivity(), getString(R.string.easter_egg), Toast.LENGTH_LONG).show();
                    watchYoutubeVideo(getActivity(), "X11cciTgwiM");
                    taps = 0;
                }

                taps++;
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        API = ((MainActivity)getActivity()).getAuth().getAPIversion();
        APIverText.setText(API);
    }

    private void shareAppLink(){
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String shareMessage= getString(R.string.share_message);
            shareMessage = shareMessage + " https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share with..."));
        } catch(Exception e) {
            log.e(e.toString());
        }
    }

    private void sendBugEmail(){
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","marco97pa@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "[marco97pa@gmail.com]");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        emailIntent.putExtra(Intent.EXTRA_TEXT, "I had a problem with " +
                getString(R.string.app_name) + ", version " +  BuildConfig.VERSION_NAME +
                ", API version " + API);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    private void logout(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("autologin", false);
        editor.putString("username", "");
        editor.putString("password", "");
        editor.putString("accessToken", "");
        editor.putString("refreshToken", "");
        editor.apply();
        //return to AuthActivity
        getActivity().finish();
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd(getString(R.string.ad_full_screen),
                new AdRequest.Builder().build());
    }

    private void showAPIDialog(Context context){
        new MaterialAlertDialogBuilder(context)
                .setTitle(getString(R.string.api_version))
                .setMessage(getString(R.string.api_details))
                .setNeutralButton(getString(R.string.github), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String url = "https://github.com/The-Firexx/trackmania2020apidocumentation";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void checkSupportedApi(){
        String supported_api = mFirebaseRemoteConfig.getString("supported_api");
        if(API != "" && API != null && supported_api != "none" && supported_api != "") {
            log.d( "Supported API (from Firebase): " + supported_api);
            log.d( "Actual API (from Ubisoft): " + API);
            if (API.contains(supported_api)) {
                APIverText.setTextColor(ContextCompat.getColor(getActivity(), R.color.green));
                log.d( "API version supported");
            } else {
                Log.w(LOG_TAG, "API version unsupported");
            }
        }
    }

    private static void watchYoutubeVideo(Context context, String id) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }


    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {

    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    @Override
    public void onResume() {
        mRewardedVideoAd.resume(getActivity());
        super.onResume();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(getActivity());
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(getActivity());
        super.onDestroy();
    }
}