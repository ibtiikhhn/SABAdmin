package com.chamelon.sabadmin.asynctasks;

import android.os.AsyncTask;
import android.util.Log;

import com.chamelon.sabadmin.info.Info;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class FirebaseSendMessage extends AsyncTask<String, Integer, Double> implements Info {

    private final static String AUTH_KEY = KEY_LSK;
    private long contentId;

    protected Double doInBackground(String... params) {
        try {
            sendRequest(params);
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }
        return null;
    }

    public FirebaseSendMessage(long contentId) {
        this.contentId = contentId;
    }

    private void sendRequest(String... params) {
        try {
            String urlString = "https://fcm.googleapis.com/fcm/send";
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "key=" + AUTH_KEY);
            String postJsonData = "{\"to\": \"/topics/allDevices\", \"data\": {\"message\": \"" + contentId + "\"}}";
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postJsonData);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            Log.v(TAG, "POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.v(TAG, "succeeded");
            }
        } catch (IOException e) {
            Log.d("exception thrown: ", e.toString());
        }
    }
}