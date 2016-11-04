package com.ssl.mavericks.feeder39;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ssl.mavericks.feeder39.LoginActivity.LOGIN_URL;

public class NetReq {

    public static final int CONNECTON_TIMEOUT = 5000;
    public static final boolean RESET_COOKIE = true;
    public static final boolean DO_NOT_RESET_COOKIE = false;

    URL url = null;
    HttpURLConnection client = null;
    BufferedReader reader;
    String cookie;

    public String initialRequest(String iurl){

        String token = null;
        try {
            url = new URL(iurl);
            client = (HttpURLConnection) url.openConnection();

            client.setRequestMethod("GET");
            client.setConnectTimeout(CONNECTON_TIMEOUT);

            String tSetCookie = client.getHeaderFields().get("Set-Cookie").get(0);
            token = tSetCookie.substring(tSetCookie.indexOf("=")+1,tSetCookie.indexOf(";"));

//            for (Map.Entry<String, List<String>> entry : client.getHeaderFields().entrySet()) {
//                System.out.println(entry.getKey()
//                        + ":" + entry.getValue());
//            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }finally {
            client.disconnect();
        }

        return token;
    }

    public ArrayList<String> postRequest(String iurl, String token, String dataStr, boolean cookieReset){

        StringBuilder stringBuilder = null;
        ArrayList<String> retVal = new ArrayList<String>();
        // login process after obtaining the csrf token
        try {
            url = new URL(iurl);
            client = (HttpURLConnection) url.openConnection();

            client.setRequestMethod("POST");
            client.setRequestProperty("Referer",iurl);
            client.setRequestProperty("X-CSRFToken",token);
            client.setRequestProperty("Cookie", "csrftoken="+token);
            client.setConnectTimeout(CONNECTON_TIMEOUT);
            client.setDoOutput(true);

            OutputStreamWriter outputPost = new OutputStreamWriter(client.getOutputStream());

            outputPost.write(dataStr);
            outputPost.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            stringBuilder = new StringBuilder();
            String line = null;

            while((line = reader.readLine()) != null){stringBuilder.append(line + "\n");}

            if(cookieReset)
                cookie = client.getHeaderFields().get("Set-Cookie").get(0);

            System.out.print("Saved Cookie: ");
            System.out.println(cookie);

//            for (Map.Entry<String, List<String>> entry : client.getHeaderFields().entrySet()) {
//                System.out.println(entry.getKey()
//                        + ":" + entry.getValue());
//            }

            reader.close();
            outputPost.close();

            return null;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            client.disconnect();
            if (stringBuilder != null) {
                retVal.add(0,cookie);
                retVal.add(1,stringBuilder.toString());
                return retVal;
            }else{
                retVal.add(0,cookie);
                return retVal;
            }
        }
    }

    public String dataRequest(String iurl, UserSessionManager session){

        StringBuilder stringBuilder = null;
        ArrayList<String> retVal = new ArrayList<String>();
        try {
            url = new URL(iurl);
            client = (HttpURLConnection) url.openConnection();

            client.setRequestProperty("Cookie", session.getUserDetails().get(UserSessionManager.SESSION_COOKIE));
            client.setRequestMethod("GET");
            client.setConnectTimeout(CONNECTON_TIMEOUT);

            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            stringBuilder = new StringBuilder();
            String line = null;
            // Read Server Response
            while((line = reader.readLine()) != null) { stringBuilder.append(line + "\n"); }

//            for (Map.Entry<String, List<String>> entry : client.getHeaderFields().entrySet()) {
//                System.out.println(entry.getKey()
//                        + ":" + entry.getValue());
//            }
            reader.close();
            return null;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            client.disconnect();
            if(stringBuilder == null){
                return null;
            }
            return stringBuilder.toString();
        }
    }

}
