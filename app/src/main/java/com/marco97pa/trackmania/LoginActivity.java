package com.marco97pa.trackmania;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setWebContentsDebuggingEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                Log.d(LOG_TAG, view.getUrl());
                if(view.getUrl().equals("https://players.trackmania.com/player")){
                    String cookie = CookieManager.getInstance().getCookie(url);
                    if(cookie == null){
                        Log.e(LOG_TAG, "Cookie is null");
                    }
                    else{
                        Log.d(LOG_TAG, "Acquired this cookie: " + cookie);
                    }

                    //Terminate, go to MainActivity
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("cookie", cookie);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
            }
        });

        webView.loadUrl("https://players.trackmania.com/player");

    }

    //BACK CLOSES APP
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Terminate, go to MainActivity
        Intent returnIntent = new Intent();
        returnIntent.putExtra("close", true);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
