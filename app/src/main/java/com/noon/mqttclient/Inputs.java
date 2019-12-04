package com.noon.mqttclient;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by Islam Salah on 2019-08-15.
 * <p>
 * https://github.com/IslamSalah
 * islamsalah007@gmail.com
 */

/* Google IoT */
public class Inputs {

    String CLIENT_PRIVATE_KEY ;
    String HOST_URL ;
    String CLIENT_ID ;
    String PROJECT_ID ;
    String PUBLISH_TOPIC;
    String RECEIVE_TOPIC;
    String TEST_PAYLOAD;

    public Inputs(Context context) {
        readConnectionConfig(context);
    }

    private void readConnectionConfig(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("connections.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(json, Map.class);
            CLIENT_ID = map.get("CLIENT_ID");
            HOST_URL = map.get("HOST_URL");
            CLIENT_PRIVATE_KEY = map.get("CLIENT_PRIVATE_KEY");
            PROJECT_ID = map.get("PROJECT_ID");
            PUBLISH_TOPIC = map.get("PUBLISH_TOPIC");
            RECEIVE_TOPIC = map.get("RECEIVE_TOPIC");
            RECEIVE_TOPIC = map.get("RECEIVE_TOPIC");
            TEST_PAYLOAD = map.get("TEST_PAYLOAD");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}