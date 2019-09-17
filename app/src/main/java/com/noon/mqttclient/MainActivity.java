package com.noon.mqttclient;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.noon.mqtt.MqttHelper;
import com.noon.mqtt.MqttHelperImp;
import com.noon.mqtt.PasswordGenerator;

import static com.noon.mqttclient.Inputs.CLIENT_ID;
import static com.noon.mqttclient.Inputs.CLIENT_PRIVATE_KEY;
import static com.noon.mqttclient.Inputs.HOST_URL;
import static com.noon.mqttclient.Inputs.PROJECT_ID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runMqttHelperDemo();
    }

    private void runMqttHelperDemo() {
        PasswordGenerator passwordGenerator = new PasswordGenerator(CLIENT_PRIVATE_KEY, PROJECT_ID);
        MqttHelper mqttHelper = MqttHelperImp.builder()
                .context(this)
                .clientId(CLIENT_ID)
                .hostUrl(HOST_URL)
                .connectionUserName("unused")
                .passwordGenerator(passwordGenerator)
                .build();

        mqttHelper.connect();
        mqttHelper.addOnConnectionSuccessListener(() -> {
            mqttHelper.subscribe("/devices/my-node-device/config");
            Log.d(TAG, "connection success!");
        });
        mqttHelper.addOnMessageArrivedListener((topic, message) -> {
            Log.d(TAG, topic + " " + message);
        });
    }
}
