package com.ssl.mavericks.feeder39;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class UserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String FEEDBACK_URL = LoginActivity.HOST_URL + "android/feedbacklist/";
    public static final String ASSIGNMENTS_URL = LoginActivity.HOST_URL + "android/assignmentlist/";
    public static final String COURSES_URL = LoginActivity.HOST_URL + "android/courselist/";

    ListView courseListView;
    ArrayList<String> courseListItems = new ArrayList<String>();
    ArrayAdapter<String> courseListAdapter;

    ListView eventListView;
    ArrayAdapter<String> eventListAdapter;
    ArrayList<String> eventListItems = new ArrayList<String>();

    ArrayList<Course> coursesInDatabase;
    HashMap<String, Integer> assignmentCodesInDatabase;
    HashMap<Date, ArrayList<Assignment>> assignmentsDateLists;

    CaldroidListener caldroidListener;
    CaldroidCustom caldroidFragment;

    UserSessionManager session;
    ViewFlipper vf;

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

        syncData();

        vf.setDisplayedChild(1);
    }

    public void setUIElements(){

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vf = (ViewFlipper) findViewById(R.id.content_flipper);

        courseListView = (ListView) findViewById(R.id.courses_list);
        eventListView = (ListView) findViewById(R.id.agenda_view);

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

        findViewById(R.id.agenda_head_view).setVisibility(View.GONE);

    }

    public void setListeners(){
        caldroidListener = new CaldroidListener() {

            SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd");
            @Override
            public void onSelectDate(Date date, View view) {
                TextView agendaHead = (TextView) findViewById(R.id.agenda_head_view);
                agendaHead.setVisibility(View.VISIBLE);
                eventListView.setVisibility(View.VISIBLE);
                eventListItems.clear();

                try {
                    ArrayList<Assignment> list = assignmentsDateLists.get(date);
                    if(list != null) {
                        for (Assignment a : list) {
                            eventListItems.add(a.getTitle());
                        }
                    }
                    eventListAdapter.notifyDataSetChanged();

                    agendaHead.setText("Agenda for " + formatter.format(date));
                }catch (NullPointerException e){
                    e.printStackTrace();
                }


            }

        };
        caldroidFragment.setCaldroidListener(caldroidListener);

        courseListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, courseListItems);
        courseListView.setAdapter(courseListAdapter);
        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                Toast.makeText(getApplicationContext(), coursesInDatabase.get(position).getDatabaseCode() + "", Toast.LENGTH_SHORT);
            }
        });

        eventListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, eventListItems);
        eventListView.setAdapter(eventListAdapter);
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        eventListView.setVisibility(View.GONE);
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

    public void syncData() {
        DataSyncTask dataSyncTask = new DataSyncTask();
        dataSyncTask.execute();
    }

    public void populateApp(){
        try{
            for (Course course : coursesInDatabase ) {
                courseListItems.add(course.getCode() + " " + course.getName());
            }
            courseListAdapter.notifyDataSetChanged();
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
    }

    public class DataSyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Snackbar.make(findViewById(R.id.calendar_user), "Database syncing", Snackbar.LENGTH_INDEFINITE).show();
        }


        @Override
        protected Void doInBackground(Void... voids) {

            try {

                NetReq netReq = new NetReq();
                String response;
//                response = "a[{\"model\":\"assignments.assignmentstore\",\"pk\":4,\"fields\":{\"course_code\":1,\"deadline\":\"2016-11-23T19:00:00Z\",\"name\":\"First assignment added\"}},{\"model\":\"assignments.assignmentstore\",\"pk\":5,\"fields\":{\"course_code\":2,\"deadline\":\"2016-11-13T12:00:00Z\",\"name\":\"Submit assignment in 213 lab\"}},{\"model\":\"assignments.assignmentstore\",\"pk\":6,\"fields\":{\"course_code\":3,\"deadline\":\"2016-11-10T02:10:21Z\",\"name\":\"Assignment manageable\"}},{\"model\":\"assignments.assignmentstore\",\"pk\":7,\"fields\":{\"course_code\":4,\"deadline\":\"2016-11-06T00:00:00Z\",\"name\":\"Sleep.Code.Repeat.\"}},{\"model\":\"assignments.assignmentstore\",\"pk\":8,\"fields\":{\"course_code\":1,\"deadline\":\"2016-11-29T12:00:00Z\",\"name\":\"Project Submission\"}}]a";
                response = netReq.dataRequest(ASSIGNMENTS_URL,session);

                response = response.replace("\\","");
                response = response.substring(1,response.length()-1);
                JSONArray assignmentJson = new JSONArray(response);
                System.out.println(assignmentJson);

                assignmentCodesInDatabase = new HashMap<String, Integer>();
                assignmentsDateLists = new HashMap<Date, ArrayList<Assignment>>();
                for (int i = 0; i < assignmentJson.length(); i++){
                    JSONObject temp = (JSONObject) assignmentJson.get(i);
                    JSONObject fields = (JSONObject) temp.get("fields");
                    assignmentCodesInDatabase.put(fields.get("name").toString(), temp.getInt("pk"));

                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    String dateString = fields.getString("deadline").toString();
                    dateString = dateString.substring(0,dateString.indexOf("Z")-8) + "00:00:00Z";
                    Date day = formatter.parse(dateString);
                    if(assignmentsDateLists.get(day) == null){
                        assignmentsDateLists.put(day, new ArrayList<Assignment>());
                    }
                    assignmentsDateLists.get(day).add(new Assignment(temp.getInt("pk"), fields.getInt("coursecode"), fields.get("title").toString(),fields.getString("description"), day));
                }

                response = netReq.dataRequest(COURSES_URL,session);
                response = response.replace("\\","");
                response = response.substring(1,response.length()-1);
                JSONArray coursesJson = new JSONArray(response);
                System.out.println(coursesJson);

                coursesInDatabase = new ArrayList<Course>();
                for (int i = 0; i < coursesJson.length(); i++){
                    JSONObject temp = (JSONObject) coursesJson.get(i);
                    JSONObject fields = (JSONObject) temp.get("fields");
                    coursesInDatabase.add(new Course(temp.getInt("pk"), fields.getString("code"), fields.getString("name"))); // (fields.get("code").toString(),temp.getInt("pk"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            populateApp();
            Snackbar.make(findViewById(R.id.calendar_user), "Sync complete", Snackbar.LENGTH_SHORT).show();
        }
    }

    public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

        UserLogoutTask() {}

        @Override
        protected Boolean doInBackground(Void... params) {

            URL url = null;
            try {
                url = new URL(LoginActivity.LOGIN_URL);
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

                System.out.println(client.getHeaderFields());

                reader.close();
                return true;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                client.disconnect();

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
//                Toast.makeText(getApplicationContext(), "Logout failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
