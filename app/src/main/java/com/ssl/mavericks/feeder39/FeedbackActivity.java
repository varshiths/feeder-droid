package com.ssl.mavericks.feeder39;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import static com.ssl.mavericks.feeder39.NetReq.CONNECTON_TIMEOUT;
import static com.ssl.mavericks.feeder39.UserActivity.QUESTIONS_URL;
import static com.ssl.mavericks.feeder39.UserActivity.coursesCodesInDatabase;
import static com.ssl.mavericks.feeder39.UserActivity.coursesInDatabase;
import static com.ssl.mavericks.feeder39.UserActivity.feedbacksInDatabase;
import static com.ssl.mavericks.feeder39.UserActivity.session;

public class FeedbackActivity extends AppCompatActivity {

    public static final String FEEDBACK_SUBMIT_URL = LoginActivity.HOST_URL + "android/feedbackresponse/";
    public static final String FEEDBACK_NAME = "com.ssl.mavericks.FEEDBACK_PK";
    String finalPOSTString = null;

    NetReq netReq = new NetReq();

    Feedback feedback;
    ArrayList<Question> questions;
    ArrayList<View> answerSegments;
    LinearLayout master;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        master = (LinearLayout) findViewById(R.id.feedback_container);

        Bundle bundle = getIntent().getExtras();
        String pk = bundle.getString(FEEDBACK_NAME);
        feedback = feedbacksInDatabase.get(pk);

        SimpleDateFormat formatter = new SimpleDateFormat("E, MMM dd");

        QuestionsSyncTask q = new QuestionsSyncTask();
        q.execute();

        TextView titleView = (TextView) findViewById(R.id.feed_title);
        titleView.setText(feedback.getFeedbackName());

//        TextView sourceView = (TextView) findViewById(R.id.feed_source);
//        sourceView.setText(feedback.getCourseDatabaseCode());

        TextView deadlineView = (TextView) findViewById(R.id.feed_deadline);
        deadlineView.setText(formatter.format(feedback.getDeadline()));

    }

    void loadQuestions(){

        answerSegments = new ArrayList<View>();
        for (Question q : questions ){
            System.out.println(q.questionType);
        }

        for (Question q : questions){
            TextView textView = new TextView(getApplicationContext());
            if(q.questionType.equals("TextField")){
                TextView tv = new TextView(this);
                tv.setTextSize(15f);
                tv.setText(q.text);

                EditText answerView = new EditText(this);

                master.addView(tv);
                answerSegments.add(answerView);
                master.addView(answerView);
            }

            if(q.questionType.equals("RatingField")){
                TextView tv = new TextView(this);
                tv.setTextSize(15f);
                tv.setText(q.text);

                RatingBar answerView = new RatingBar(this);
                answerView.setNumStars(5);
                answerView.setStepSize(1f);

                master.addView(tv);
                answerSegments.add(answerView);
                master.addView(answerView);
            }
//
            if(q.questionType.equals("yesNoQuestion")){
                TextView tv = new TextView(this);
                tv.setTextSize(15f);
                tv.setText(q.text);

                System.out.println("Adding here");
                View answerView = new Switch(this);

                master.addView(tv);
                answerSegments.add(answerView);
                master.addView(answerView);
            }
        }

        Button submitButton = new Button(this);
        submitButton.setText("Submit");
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FeedbackSubmitTask feedbackSubmitTask = new FeedbackSubmitTask();
                feedbackSubmitTask.execute();
            }
        });
        master.addView(submitButton);
    }

    public class FeedbackSubmitTask extends AsyncTask<Void, Void, Boolean> {

        Boolean finalStatus = false;
        FeedbackSubmitTask() {}

        void constructFinalPOSTString(){

            JSONArray finale = new JSONArray();
            HashMap<String, String> finalPOST = new HashMap<String, String>();

            for (int i = 0; i < questions.size(); i++) {

                HashMap<String, String> answer = new HashMap<String, String>();
                Question q = questions.get(i);
                View answerView = answerSegments.get(i);
                answer.put("pk",q.pk + "");
                answer.put("questiontype", q.questionType);

                if(q.questionType.equals("TextField")){
                    String p = ((EditText) answerView).getText().toString();
                    answer.put("answer",p);
                }
                if(q.questionType.equals("RatingField")) {
                    int p = ((RatingBar) answerView).getNumStars();
                    answer.put("answer",p + "");
                }
                if(q.questionType.equals("yesNoQuestion")) {
                    boolean p = ((Switch) answerView).isChecked();
                    answer.put("answer", p + "");
                }

                JSONObject jsTemp = new JSONObject(answer);

                finale.put(jsTemp);
            }

            finalPOSTString = finale.toString();
            System.out.println("Constructing the final POST String");
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            constructFinalPOSTString();

            String pTemo = FEEDBACK_SUBMIT_URL + feedback.getDatabaseCode() + "/";

            URL url = null;
            try {
                url = new URL(pTemo);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            JSONObject jsonObject = null;
            HttpURLConnection client = null;
            try {
                client = (HttpURLConnection) url.openConnection();

                client.setRequestMethod("POST");
//                client.setRequestProperty("Referer",FEEDBACK_SUBMIT_URL);
//                client.setRequestProperty("X-CSRFToken",session.getUserDetails().get(UserSessionManager.TOKEN));
//                client.setRequestProperty("Cookie", session.getUserDetails().get(UserSessionManager.SESSION_COOKIE));
                client.setConnectTimeout(CONNECTON_TIMEOUT);
                client.setReadTimeout(CONNECTON_TIMEOUT);

                System.out.println(client.getRequestProperties());

                OutputStreamWriter outputPost = new OutputStreamWriter(client.getOutputStream());
                System.out.println(finalPOSTString);
                outputPost.write(finalPOSTString);
                outputPost.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                String line = null;

                while((line = reader.readLine()) != null) { stringBuilder.append(line + "\n"); }
//                System.out.println("Feedback POST request here");
//                System.out.println(client.getHeaderFields());
//                System.out.println(stringBuilder.toString());

                finalStatus = stringBuilder.toString().contains("successful");
                reader.close();
                outputPost.close();
                return finalStatus;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } finally {
                client.disconnect();

                return finalStatus;
//                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if (success) {
                Toast.makeText(getApplicationContext(), "Feedback posted successfully", Toast.LENGTH_SHORT).show();
                // set feedback as filled
                feedback.setFilled(true);
                // do not forget
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Feedback post failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class QuestionsSyncTask extends AsyncTask<Object, Object, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Object... voids) {


            JSONArray questionsJson = null;
            try {
                String response;
                response = netReq.dataRequest(QUESTIONS_URL + feedback.getDatabaseCode() + "/", session);
                response = response.replace("\\", "");
                response = response.substring(1, response.length() - 1);

                questionsJson = new JSONArray(response);
                System.out.println(questionsJson);

                questions = new ArrayList<Question>();
                for (int i = 0; i < questionsJson.length(); i++){
                    JSONObject temp = (JSONObject) questionsJson.get(i);
                    JSONObject fields = (JSONObject) temp.get("fields");
                    questions.add(new Question(temp.getInt("pk"), fields.getString("text"), fields.getString("questiontype"),fields.getInt("feedback")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadQuestions();
        }
    }

}
