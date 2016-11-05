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
    public static final String QUESTIONS_URL = LoginActivity.HOST_URL + "android/questionlist/";
    public static final String ASSIGNMENTS_URL = LoginActivity.HOST_URL + "android/assignmentlist/";
    public static final String COURSES_URL = LoginActivity.HOST_URL + "android/courselist/";

    ListView courseListView;
    ArrayList<String> courseListItems = new ArrayList<String>();
    ArrayAdapter<String> courseListAdapter;

    ListView eventListView;
    ArrayAdapter<String> eventListAdapter;
    ArrayList<String> eventListItems = new ArrayList<String>();

    public static HashMap<String, Course> coursesInDatabase;
    public static HashMap<Integer, String> coursesCodesInDatabase;
    public static HashMap<String, Assignment> assignmentsInDatabase;
    public static HashMap<Date, ArrayList<Assignment>> assignmentsDateLists;
    public static HashMap<String, Feedback> feedbacksInDatabase;
    public static HashMap<Date, ArrayList<Feedback>> feedbacksDateLists;

    NetReq netReq = new NetReq();

    CaldroidListener caldroidListener;
    CaldroidCustom caldroidFragment;

    public static UserSessionManager session;
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
                agendaHead.setText("Agenda for " + formatter.format(date));

                try {
                    ArrayList<Assignment> list = assignmentsDateLists.get(date);
                    if(list != null) {
                        for (Assignment a : list) {
                            eventListItems.add(a.getTitle());
                        }
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }

                try {
                    ArrayList<Feedback> list = feedbacksDateLists.get(date);
                    if(list != null) {
                        for (Feedback f : list) {
                            eventListItems.add(f.getFeedbackName());
                        }
                    }
                } catch (NullPointerException e){
                    e.printStackTrace();
                }

                eventListAdapter.notifyDataSetChanged();

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
//                Toast.makeText(getApplicationContext(), coursesInDatabase.get(position).getDatabaseCode() + "", Toast.LENGTH_SHORT);
            }
        });

        eventListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, eventListItems);
        eventListView.setAdapter(eventListAdapter);
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    Assignment a = assignmentsInDatabase.get(eventListItems.get(i));
                    SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd");

                    Intent intent = new Intent(getApplicationContext(),EventActivity.class);
                    intent.putExtra(EventActivity.EVENT_TITLE,a.getTitle());
                    intent.putExtra(EventActivity.EVENT_DESCRIPTION,a.getDescription());
                    intent.putExtra(EventActivity.EVENT_DEADLINE,formatter.format(a.getDeadline()));
                    intent.putExtra(EventActivity.EVENT_SOURCE,coursesInDatabase.get(coursesCodesInDatabase.get(a.getCourseDatabaseCode())).getCode());
                    startActivity(intent);
                }catch (NullPointerException e){
                    e.printStackTrace();
                } finally {
                    try {
                        Feedback a = feedbacksInDatabase.get(eventListItems.get(i));
                        SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd");

                        if(a.alreadyFilled){
                            Toast.makeText(getApplicationContext(),"Already filled", Toast.LENGTH_SHORT);
                        }
                        else{
                            Intent intent = new Intent(getApplicationContext(),FeedbackActivity.class);
                            intent.putExtra(FeedbackActivity.FEEDBACK_NAME,a.getFeedbackName());
                            startActivity(intent);
                        }
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }

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
            for (Course course : coursesInDatabase.values() ) {
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

                String response;

                response = netReq.dataRequest(COURSES_URL,session);
                response = response.replace("\\","");
                response = response.substring(1,response.length()-1);
                System.out.println(response);
                JSONArray coursesJson = new JSONArray(response);
                System.out.println(coursesJson);

                coursesInDatabase = new HashMap<String, Course>();
                coursesCodesInDatabase = new HashMap<Integer, String>();
                for (int i = 0; i < coursesJson.length(); i++){
                    JSONObject temp = (JSONObject) coursesJson.get(i);
                    JSONObject fields = (JSONObject) temp.get("fields");
                    coursesCodesInDatabase.put(temp.getInt("pk"),fields.getString("name"));
                    coursesInDatabase.put(fields.getString("name"),new Course(temp.getInt("pk"), fields.getString("code"), fields.getString("name"))); // (fields.get("code").toString(),temp.getInt("pk"));
                }


                response = netReq.dataRequest(FEEDBACK_URL,session);
                response = response.replace("\\","");
                response = response.substring(1,response.length()-1);
                JSONArray feedbackJson = new JSONArray(response);
                System.out.println(feedbackJson);

                feedbacksInDatabase = new HashMap<String, Feedback>();
                feedbacksDateLists = new HashMap<Date, ArrayList<Feedback>>();
                for (int i = 0; i < feedbackJson.length(); i++){
                    JSONObject temp = (JSONObject) feedbackJson.get(i);
                    JSONObject fields = (JSONObject) temp.get("fields");
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    feedbacksInDatabase.put(fields.get("feedback_name").toString(),new Feedback(temp.getInt("pk"), fields.getInt("coursecode"),
                            fields.get("feedback_name").toString(), formatter.parse(fields.getString("deadline"))));

                    String dateString = fields.getString("deadline").toString();
                    dateString = dateString.substring(0,dateString.indexOf("Z")-8) + "00:00:00Z";
                    Date day = formatter.parse(dateString);
                    if(feedbacksDateLists.get(day) == null){
                        feedbacksDateLists.put(day, new ArrayList<Feedback>());
                    }
                    feedbacksDateLists.get(day).add(new Feedback(temp.getInt("pk"), fields.getInt("coursecode"), fields.get("feedback_name").toString(), day));
                }

                response = netReq.dataRequest(ASSIGNMENTS_URL,session);
                response = response.replace("\\","");
                response = response.substring(1,response.length()-1);
                JSONArray assignmentJson = new JSONArray(response);
                System.out.println(assignmentJson);

                assignmentsInDatabase = new HashMap<String, Assignment>();
                assignmentsDateLists = new HashMap<Date, ArrayList<Assignment>>();
                for (int i = 0; i < assignmentJson.length(); i++){
                    JSONObject temp = (JSONObject) assignmentJson.get(i);
                    JSONObject fields = (JSONObject) temp.get("fields");
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    assignmentsInDatabase.put(fields.get("title").toString(),new Assignment(temp.getInt("pk"), fields.getInt("coursecode"),
                            fields.get("title").toString(),fields.getString("description"), formatter.parse(fields.getString("deadline"))));

                    String dateString = fields.getString("deadline").toString();
                    dateString = dateString.substring(0,dateString.indexOf("Z")-8) + "00:00:00Z";
                    Date day = formatter.parse(dateString);
                    if(assignmentsDateLists.get(day) == null){
                        assignmentsDateLists.put(day, new ArrayList<Assignment>());
                    }
                    assignmentsDateLists.get(day).add(new Assignment(temp.getInt("pk"), fields.getInt("coursecode"), fields.get("title").toString(),fields.getString("description"), day));
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

            StringBuilder stringBuilder = null;
            JSONObject jsonObject = null;
            HttpURLConnection client = null;
            try {
                client = (HttpURLConnection) url.openConnection();

                client.setRequestProperty("Cookie", session.getUserDetails().get(UserSessionManager.SESSION_COOKIE));
                client.setRequestMethod("GET");
                client.setConnectTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                stringBuilder = new StringBuilder();
                String line = null;
                while((line = reader.readLine()) != null){ stringBuilder.append(line + "\n"); }
                System.out.println(client.getHeaderFields());

                reader.close();
                return true;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                client.disconnect();

                return stringBuilder.toString().contains("logout") && stringBuilder.toString().contains("successful");
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                session.logoutUser();
                caldroidFragment.refreshView();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else {
//                Toast.makeText(getApplicationContext(), "Logout failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
