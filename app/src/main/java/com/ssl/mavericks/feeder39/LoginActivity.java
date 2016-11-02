package com.ssl.mavericks.feeder39;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    public static final String LOGIN_URL = "http://localhost:8039/loginstuff/androidlogin/";
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

        TextView registerButton = (TextView) findViewById(R.id.lRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Register Activity opens");
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
        String cookie;
        String login_message;

        UserLoginTask(String username, String password, Context context) {
            mUserName = username;
            mPassword = password;
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Boolean s = null;

            URL url = null;
            try {
                url = new URL(LOGIN_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = null;
            HttpURLConnection client = null;
            try {
                client = (HttpURLConnection) url.openConnection();

                client.setRequestMethod("GET");
                client.setConnectTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    stringBuilder.append(line + "\n");
                }

                cookie = client.getHeaderFields().get("Set-Cookie").get(0);
                for (Map.Entry<String, List<String>> entry : client.getHeaderFields().entrySet()) {
                    System.out.println(entry.getKey()
                            + ":" + entry.getValue());
                }
                System.out.println("Set-Cookie:" + cookie);

                reader.close();
                jsonObject = new JSONObject(stringBuilder.toString());

                System.out.println("Raw Message: " + stringBuilder.toString());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                client.disconnect();
            }

//            System.out.println(token);

            HashMap<String,String> jsonResp = null;
            try {
                String token = cookie.substring(cookie.indexOf("=")+1,cookie.indexOf(";"));
                client = (HttpURLConnection) url.openConnection();

                client.setRequestMethod("POST");
                client.setRequestProperty("Referer",LOGIN_URL);
                client.setRequestProperty("X-CSRFToken",token);
                client.setRequestProperty("Cookie", "csrftoken="+token);
                client.setConnectTimeout(5000);
                client.setDoOutput(true);

                OutputStreamWriter outputPost = null;
                outputPost = new OutputStreamWriter(client.getOutputStream());

                HashMap<String,String> data = new HashMap<String, String>();
                data.put("username",mUserName);
                data.put("password",mPassword);
                String dataStr = (new JSONObject(data)).toString();

                outputPost.write(dataStr);
                outputPost.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

//                InputStream ois = client.getInputStream();
//
//                ObjectInputStream o = new ObjectInputStream(ois);
//                final Object obj = o.readObject();
//
//                ois.close();
//
//                jsonObject = (JSONObject) obj;


                StringBuilder stringBuilder = new StringBuilder();
//                jsonObject = new JSONObject(stringBuilder.toString());
                System.out.println(stringBuilder.toString());
                String line = null;
//                String
//                 Read Server Response
                while((line = reader.readLine()) != null)
                {
//                     Append server response in string
                    stringBuilder.append(line + "\n");
                }
//

                cookie = client.getHeaderFields().get("Set-Cookie").get(0);
//
                for (Map.Entry<String, List<String>> entry : client.getHeaderFields().entrySet()) {
                    System.out.println(entry.getKey()
                            + ":" + entry.getValue());
                }
//                System.out.println("Set-Cookie:" + cookie);

//                reader.close();
                outputPost.close();

                login_message = stringBuilder.toString();
//                System.out.println(stringBuilder.toString());
//                System.out.println(jsonObject);

//                System.out.println("Here");
//                client.disconnect();
                s = stringBuilder.toString().contains("Welcome");
                return true;
//                jsonResp = new HashMap<String, String>((Map<? extends String, ? extends String>) jsonObject);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                client.disconnect();
                if(s==null)
                    return false;

                return s;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                Toast.makeText(getApplicationContext(), login_message, Toast.LENGTH_LONG).show();
                session.createUserLoginSession(mUserName,mPassword,cookie,mContext);
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), R.string.login_fail_string, Toast.LENGTH_SHORT).show();
            }
        }
    }
}


