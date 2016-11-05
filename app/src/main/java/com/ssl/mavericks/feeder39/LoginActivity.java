package com.ssl.mavericks.feeder39;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

//    public static final String HOST_URL = "http://localhost:8039/";
    public static final String HOST_URL = "http://10.0.1.10:8000/";
    public static final String LOGIN_URL = HOST_URL + "android/androidlog/";
    UserSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = new UserSessionManager(getApplicationContext());
        final EditText credentials = (EditText) findViewById(R.id.lUser);
        final EditText password = (EditText) findViewById(R.id.lPassword);

        credentials.requestFocus();
        credentials.setError(null);

        Button loginButton = (Button) findViewById(R.id.lButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String credStr = credentials.getText().toString();
                String passStr = password.getText().toString();

                try {
                    if(credStr.contains(" ")){
                        credentials.setError("Invalid username");
                    }else{
                        authenticate(credStr, passStr);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void authenticate(String credStr, String passStr) throws JSONException {

        System.out.println("Authenticating '" + credStr + "' with password '" + passStr + "'");
        UserLoginTask mAuthTask = new UserLoginTask(credStr,passStr, this);
        mAuthTask.execute((Void) null);

    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUserName;
        private final String mPassword;
        Context mContext;
        String cookie; String token;

        UserLoginTask(String username, String password, Context context) {
            mUserName = username;
            mPassword = password;
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean finalStatus = false;
            // get reuqest to obtain token
            NetReq call = new NetReq();
            token = call.initialRequest(LOGIN_URL);

            HashMap<String,String> data = new HashMap<String, String>();
            data.put("username",mUserName);
            data.put("password",mPassword);
            String dataStr = (new JSONObject(data)).toString();

            ArrayList<String> response = call.postRequest(LOGIN_URL, token, dataStr);
            cookie = response.get(0);

            JSONObject jsonObject = null;

            if (response == null) return false;
            try {
                jsonObject = new JSONObject(response.get(1));
                finalStatus = response.get(1).contains("true");
                return finalStatus;
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }

            return finalStatus;

        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                session.createUserLoginSession(mUserName,mPassword,cookie,mContext,token);
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), R.string.login_fail_string, Toast.LENGTH_SHORT).show();
            }
        }
    }
}