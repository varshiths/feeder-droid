package com.ssl.mavericks.feeder39;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserSessionManager {

    // Shared Preferences reference
    SharedPreferences pref;

    // Editor reference for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREFER_NAME = "com.ssl.mavericks.feeder39.SHARED_PREFERENCES";

    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "com.ssl.mavericks.feeder39.LOGIN_STATUS";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "com.ssl.mavericks.feeder39.KEY_NAME";

    // session cookie stored as string
    public static final String SESSION_COOKIE = "com.ssl.mavericks.feeder39.SESSION_COOKIE";

    // database stored in file
    public static final String FILE_DATABASE = "com.ssl.mavericks.feeder39.FILE_DATABASE";

    // session cookie stored as string
    public static final String TOKEN = "com.ssl.mavericks.feeder39.TOKEN";

    // Constructor
    public UserSessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public boolean createUserLoginSession(String username, String password, String cookie, Context context, String token){

        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        // Storing name in pref
        editor.putString(KEY_NAME, username);

        // Storing cookie in pref
        editor.putString(SESSION_COOKIE, cookie);

        editor.putString(TOKEN,token);

        // commit changes
        editor.commit();

        return true;
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     * */
    public boolean checkLogin(){
        // Check login status
        if(!this.isUserLoggedIn()){

            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);

            return true;
        }

        return false;
    }



    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){

        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<String, String>();

        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(SESSION_COOKIE, pref.getString(SESSION_COOKIE, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);
    }


    // Check for login
    public boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }

//    public class AuthenticateTask extends AsyncTask< Void, Void, Boolean > {
//
//        private final String mUserName;
//        private final String mPassword;
//
//        AuthenticateTask(String username, String password){
//            mUserName = username; mPassword = password;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean loggedIn) {
//            super.onPostExecute(loggedIn);
//        }
//    }
}
