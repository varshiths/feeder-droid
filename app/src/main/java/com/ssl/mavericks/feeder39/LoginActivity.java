package com.ssl.mavericks.feeder39;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(false) {// if already logged in, authenticate again though
            // get cred from storage
            authenticate("Stored", "Stored");

            Toast.makeText(getApplicationContext(), R.string.prev_login_message, Toast.LENGTH_LONG);
        }else{

            final EditText credentials = (EditText) findViewById(R.id.lUser);
            final EditText password = (EditText) findViewById(R.id.lPassword);

            Button loginButton = (Button) findViewById(R.id.lButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String credStr = credentials.getText().toString();
                    String passStr = password.getText().toString();

                    authenticate(credStr, passStr);
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


    }

    public void authenticate(String credStr, String passStr){
        System.out.println("Authenticating '" + credStr + "' with password '" + passStr + "'");

        if(true){
            Intent intent = new Intent(getApplicationContext(), UserActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(getApplicationContext(), R.string.login_fail_string, Toast.LENGTH_SHORT).show();
        }
    }
}
