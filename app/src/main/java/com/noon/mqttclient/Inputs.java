package com.noon.mqttclient;

/**
 * Created by Islam Salah on 2019-08-15.
 * <p>
 * https://github.com/IslamSalah
 * islamsalah007@gmail.com
 */

/* Google IoT */
public class Inputs {

    // Used for JWT password
    static final String PROJECT_ID = "enter project id";
    static final String CLIENT_PRIVATE_KEY = "enter private key";

    static final String HOST_URL = "ssl://mqtt.eclipse.org:8883";
    static final String DEVICE_ID = "enter device id";
    static final String BROKER_LOCATION = "enter location";
    static final String REGISTERY = "enter registry";
    static final String CLIENT_ID = String.format("projects/%s/locations/%s/registries/%s/devices/%s", PROJECT_ID, BROKER_LOCATION, REGISTERY, DEVICE_ID);
}