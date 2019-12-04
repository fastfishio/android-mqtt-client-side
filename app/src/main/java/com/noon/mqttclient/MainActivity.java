package com.noon.mqttclient;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.noon.mqtt.MqttHelper;
import com.noon.mqtt.MqttHelperImp;
import com.noon.mqtt.PasswordGenerator;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runMqttHelperDemo();
    }

    private void runMqttHelperDemo() {
        Inputs inputs = new Inputs(this);
        PasswordGenerator passwordGenerator = new PasswordGenerator(inputs.CLIENT_PRIVATE_KEY,
                inputs.PROJECT_ID);
        MqttHelper mqttHelper = MqttHelperImp.builder()
                .context(this)
                .clientId(inputs.CLIENT_ID)
                .hostUrl(inputs.HOST_URL)
                .connectionUserName("unused")
                .passwordGenerator(passwordGenerator)
                .build();

        mqttHelper.connect();
        mqttHelper.addOnConnectionSuccessListener(() -> {
            mqttHelper.subscribe(inputs.RECEIVE_TOPIC);
            new Timer().scheduleAtFixedRate(new TimerTask(){
                @Override
                public void run(){
                    Log.d(TAG, "Publishing " + inputs.TEST_PAYLOAD);
                    mqttHelper.publish(inputs.PUBLISH_TOPIC, inputs.TEST_PAYLOAD);
                }
            },0,15000);
            Log.d(TAG, "connection success!");
        });
        mqttHelper.addOnMessageArrivedListener((topic, message) -> {
            Log.d(TAG, topic + " " + message);
        });

    }
}
