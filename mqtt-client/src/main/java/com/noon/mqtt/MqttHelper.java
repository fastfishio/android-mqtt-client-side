package com.noon.mqtt;

/**
 * Created by Islam Salah on 2019-08-18.
 * <p>
 * https://github.com/IslamSalah
 * islamsalah007@gmail.com
 */
public interface MqttHelper {

    void connect();

    void disconnect();

    void subscribe(String topic);

    void publish(String topic, String message);

    void addOnConnectionSuccessListener(OnConnectionSuccessListener listener);

    void removeOnConnectionSuccessListener(OnConnectionSuccessListener listener);

    void addOnMessageArrivedListener(OnMessageArrivedListener listener);

    void removeOnMessageArrivedListener(OnMessageArrivedListener listener);

    void addOnConnectionLostListener(OnConnectionLostListener listener) ;

    void removeOnConnectionLostListener(OnConnectionLostListener listener);

    interface OnConnectionSuccessListener {
        void onConnectionCompleted();
    }

    interface OnMessageArrivedListener {
        void onMessageArrived(String topic, String message);
    }

    interface OnConnectionLostListener {
        void onConnectionLost();
    }
}
