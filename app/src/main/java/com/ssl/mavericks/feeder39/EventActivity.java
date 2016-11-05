package com.ssl.mavericks.feeder39;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class EventActivity extends AppCompatActivity {

    final static String EVENT_TITLE = "com.ssl.mavericks.feeder39.EVENT_TITLE";
    final static String EVENT_DESCRIPTION = "com.ssl.mavericks.feeder39.EVENT_DESCRIPTION";
    final static String EVENT_DEADLINE = "com.ssl.mavericks.feeder39.EVENT_DEADLINE";
    final static String EVENT_SOURCE = "com.ssl.mavericks.feeder39.EVENT_SOURCE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Bundle bundle = getIntent().getExtras();
        String titleString =  bundle.getString(EVENT_TITLE);
        String sourceString =  bundle.getString(EVENT_SOURCE);
        String deadlineString =  bundle.getString(EVENT_DEADLINE);
        String descriptionString =  bundle.getString(EVENT_DESCRIPTION);

        TextView title = (TextView) findViewById(R.id.title_view);
        TextView deadline = (TextView) findViewById(R.id.deadline_view);
        TextView source = (TextView) findViewById(R.id.source_view);
        TextView description = (TextView) findViewById(R.id.description_view);

        title.setText(titleString);
        deadline.setText(deadlineString);
        source.setText(sourceString);
        description.setText(descriptionString);

    }
}
