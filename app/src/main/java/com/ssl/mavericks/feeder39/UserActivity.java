package com.ssl.mavericks.feeder39;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ViewFlipper vf;
    ListView courseList;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    CaldroidCustom caldroidFragment;
    CaldroidListener caldroidListener;
    UserSessionManager session;
//    public final static CookieManager cookies = new CookieManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        session = new UserSessionManager(getApplicationContext());
        if(session.checkLogin()) {
            finish();
        }else {
            Toast.makeText(getApplicationContext(),"Logged in as " + session.getUserDetails().get(session.KEY_NAME),Toast.LENGTH_SHORT).show();
        }

        setUIElements();

        setListeners();

        populateApp();
        vf.setDisplayedChild(1);
    }

    public void setUIElements(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vf = (ViewFlipper) findViewById(R.id.content_flipper);
        courseList = (ListView) findViewById(R.id.courses_list);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        caldroidFragment = new CaldroidCustom();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL,true);
        caldroidFragment.setArguments(args);

        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, caldroidFragment);
        t.commit();

        CalendarView c = (CalendarView) findViewById(R.id.calendar);

        findViewById(R.id.agenda_head_view).setVisibility(View.GONE);

    }

    public void setListeners(){
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        courseList.setAdapter(adapter);

        courseList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                openCourseActivity(position);
            }
        });

        caldroidListener = new CaldroidListener() {

            SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd");
            @Override
            public void onSelectDate(Date date, View view) {
                TextView agendaHead = (TextView) findViewById(R.id.agenda_head_view);
                agendaHead.setVisibility(View.VISIBLE);
                agendaHead.setText("Agenda for " + formatter.format(date));



            }

            @Override
            public void onChangeMonth(int month, int year) {
//                String text = "month: " + month + " year: " + year;
//                Toast.makeText(getApplicationContext(), text,
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongClickDate(Date date, View view) {
//                Toast.makeText(getApplicationContext(),
//                        "Long click " + formatter.format(date),
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCaldroidViewCreated() {
//                Toast.makeText(getApplicationContext(),
//                        "Caldroid view is created",
//                        Toast.LENGTH_SHORT).show();
            }
        };

        caldroidFragment.setCaldroidListener(caldroidListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_calendar) {
            vf.setDisplayedChild(1);

        } else if (id == R.id.nav_courses) {
            vf.setDisplayedChild(2);

        } else if (id == R.id.nav_sync) {
            syncData();
        } else if (id == R.id.nav_logout) {
            UserLogoutTask logout = new UserLogoutTask();
            logout.execute();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void syncData(){
//        System.out.println("Sync service called");
//        Intent intent = new Intent(getApplicationContext(), SyncService.class);
//        startService(intent);
        populateApp();
    }
//
    public void populateApp(){

        RequestQueue q = Volley.newRequestQueue(this);

        StringRequest s = new StringRequest(Request.Method.GET, "http://192.168.0.121:8000/coreapp", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                JSONObject j = null;
//                try {
//                    j = new JSONObject(response);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    listItems.add(j.getString("foo"));
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

//        q.add(s);

        adapter.notifyDataSetChanged();

    }

    public void openCourseActivity(int index) {
        Toast.makeText(getApplicationContext(), "" + index, Toast.LENGTH_SHORT).show();
    }

    public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

        UserLogoutTask() {}

        @Override
        protected Boolean doInBackground(Void... params) {

            URL url = null;
            try {
                url = new URL("http://192.168.0.121:8000/loginstuff/androidlogin/");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = null;
            HttpURLConnection client = null;
            try {
                client = (HttpURLConnection) url.openConnection();

                client.setRequestProperty("Cookie", session.getUserDetails().get(UserSessionManager.SESSION_COOKIE));
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

//                cookie = client.getHeaderFields().get("Set-Cookie").get(0);

                for (Map.Entry<String, List<String>> entry : client.getHeaderFields().entrySet()) {
                    System.out.println(entry.getKey()
                            + ":" + entry.getValue());
                }
//                System.out.println("Raw Message: " + stringBuilder.toString());
//                System.out.println("Set-Cookie:" + cookie);

                reader.close();
                jsonObject = new JSONObject(stringBuilder.toString());

                return jsonObject.get("Success") == "true";

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                client.disconnect();
//                return false;
                return true;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                session.logoutUser();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), R.string.login_fail_string, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
