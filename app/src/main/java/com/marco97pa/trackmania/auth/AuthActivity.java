package com.marco97pa.trackmania.auth;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.marco97pa.trackmania.MainActivity;
import com.marco97pa.trackmania.R;
import com.marco97pa.trackmania.utils.FLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AuthActivity extends AppCompatActivity {

    private EditText emailTextView, passwordTextView;
    private Button logInButton;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        emailTextView = (EditText) findViewById(R.id.username);
        passwordTextView = (EditText) findViewById(R.id.password);
        logInButton = (Button) findViewById(R.id.login);
        loading = (ProgressBar) findViewById(R.id.loading);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTextView.getText().toString();
                String password = passwordTextView.getText().toString();

                //TODO: check nulls or empty
                new AuthenticationTask().execute(email, password);
            }
        });
    }

    public class AuthenticationTask extends AsyncTask<String, Void, Auth> {

        private static final String LOG_TAG = "AuthenticationTask";
        private FLog log = new FLog(LOG_TAG);

        private static final int RESPONSE_OK = 200;
        private String ticket = null;

        protected Auth doInBackground(String... params) {
            log.d( "Starting task...");
            String username = params[0];
            String password = params[1];
            Auth auth = null;

            /*
             * Stage ZERO: GET A TICKET
             * HTTP POST to https://public-ubiservices.ubi.com/v3/profiles/sessionsHeaders needed:
             * Authorization = Basic <Base64 traditional Basic authentication of email and password>
             *                 see https://en.wikipedia.org/wiki/Basic_access_authentication for more
             * Ubi-AppId = internal Ubisoft game app id, got from parameters.xml
             * Ubi-RequestedPlatformType = platform that requires data, got from parameters.xml
             * Content-Type = application/json
             */
            log.d("Starting STAGE 0");

            OkHttpClient client = new OkHttpClient();
            try {
                Request request = new Request.Builder()
                        .url(getString(R.string.URL_stage0))
                        .addHeader("Authorization", Credentials.basic(username, password))
                        .addHeader("Ubi-AppId", getString(R.string.Ubi_AppId))
                        .addHeader("Ubi-RequestedPlatformType", getString(R.string.Ubi_RequestedPlatformType))
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create("", null))
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                if(response.code() == RESPONSE_OK){
                    String jsonData = response.body().string();
                    JSONObject Jobject = new JSONObject(jsonData);
                    ticket = Jobject.get("ticket").toString();
                    log.d("Ticket: " + ticket);
                }
                else{
                    log.d("Response: " + response.code());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            if(ticket != null){

                /*
                 * Stage ONE: GET A TOKEN
                 * HTTP POST to https://prod.trackmania.core.nadeo.online/v2/authentication/token/ubiservices needed:
                 * Authorization = <ticket from Stage ZERO>
                 *
                 * Returns an accessToken value and a refreshToken value
                 */
                log.d("Starting STAGE 1");

                try {
                    Request request = new Request.Builder()
                            .url(getString(R.string.URL_stage1))
                            .addHeader("Authorization", "ubi_v1 t=" + ticket)
                            .post(RequestBody.create("", null))
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    if(response.code() == RESPONSE_OK){
                        String jsonData = response.body().string();
                        JSONObject Jobject = new JSONObject(jsonData);
                        String accessToken = Jobject.get("accessToken").toString();
                        log.d("accessToken: " + accessToken);
                        String refreshToken = Jobject.get("refreshToken").toString();
                        log.d("refreshToken: " + refreshToken);

                        auth = new Auth(accessToken, refreshToken);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }

            return auth;
        }

        protected void onPostExecute(Auth auth) {
            super.onPostExecute(auth);
            //this method will be running on UI thread
            loading.setVisibility(View.GONE);
            logInButton.setEnabled(true);

            if(auth != null) {
                Toast.makeText(getApplicationContext(), getString(R.string.log_in_success), Toast.LENGTH_LONG).show();
                //TODO: Launch MAIN ACTIVITY PASSING AUTH
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("accessToken", auth.getAccessToken());
                intent.putExtra("refreshToken", auth.getRefreshToken());
                startActivity(intent);
            }
            else{
                Snackbar.make(logInButton, getString(R.string.no_network), BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            hideKeybaord(logInButton);
            logInButton.setEnabled(false);
            loading.setVisibility(View.VISIBLE);
            loading.setIndeterminate(true);

        }

        private void hideKeybaord(View v) {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
