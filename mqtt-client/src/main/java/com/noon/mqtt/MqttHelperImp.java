package com.noon.mqtt;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Islam Salah on 2019-08-15.
 * <p>
 * https://github.com/IslamSalah
 * islamsalah007@gmail.com
 */
public class MqttHelperImp implements MqttHelper {

    private static final int AT_LEAST_ONCE_QOS = 1; // at least once

    private final MqttAndroidClient mMqttClient;
    private final PasswordGenerator mPasswordGenerator;
    private final int mKeepAliveIntervalSeconds;
    private final MqttConnectOptions mMqttConnectOptions;
    private final String mConnectionUserName;

    private final List<OnConnectionSuccessListener> mOnConnectionSuccessListeners = new LinkedList<>();
    private final List<OnMessageArrivedListener> mOnMessageArrivedListeners = new LinkedList<>();

    private MqttHelperImp(MqttHelperBuilder builder) {
        mMqttClient = new MqttAndroidClient(builder.getContext(), builder.getHostUrl(), builder.getClientId());
        mPasswordGenerator = builder.getPasswordGenerator();
        mKeepAliveIntervalSeconds = builder.getKeepAliveIntervalSeconds();
        mConnectionUserName = builder.getConnectionUserName();
        mMqttConnectOptions = initializeMqttConnectionOption();
    }

    public static MqttHelperBuilder builder() {
        return new MqttHelperBuilder();
    }

    @Override
    public void connect() {
        periodicallyRefreshConnectionPassword();
        setConnectionListener();
        tryConnecting();
    }

    private void periodicallyRefreshConnectionPassword() {
        mPasswordGenerator.setOnPasswordGeneratedListener(password -> {
            char[] passwordArr = password != null ? password.toCharArray() : null;
            mMqttConnectOptions.setPassword(passwordArr);
        });
        mPasswordGenerator.generatePeriodically();
    }

    @Override
    public void disconnect() {
        if(!mMqttClient.isConnected()) return;
        mPasswordGenerator.stopGenerating();
        tryDisconnecting();
    }

    private void tryDisconnecting() {
        try {
            mMqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void setConnectionListener() {
        mMqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                notifyOnConnectionSuccessListeners();
            }

            @Override
            public void connectionLost(Throwable cause) {
                /* No-op */
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                notifyOnMessageArrivedListeners(topic, payload);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                /* No-op */
            }
        });
    }

    private void trySubscribing(String topic) {
        try {
            mMqttClient.subscribe(topic, AT_LEAST_ONCE_QOS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifyOnConnectionSuccessListeners() {
        for (OnConnectionSuccessListener listener : mOnConnectionSuccessListeners) {
            if (listener != null) listener.onConnectionCompleted();
        }
    }

    private void notifyOnMessageArrivedListeners(String topic, String message) {
        for (OnMessageArrivedListener listener : mOnMessageArrivedListeners) {
            if (listener != null) listener.onMessageArrived(topic, message);
        }
    }

    private void tryConnecting() {
        try {
            mMqttClient.connect(mMqttConnectOptions);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private MqttConnectOptions initializeMqttConnectionOption() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setAutomaticReconnect(true);
        options.setUserName(mConnectionUserName);
        options.setPassword(getConnectionPassword());
        options.setKeepAliveInterval(mKeepAliveIntervalSeconds);
        return options;
    }

    private char[] getConnectionPassword() {
        String password = mPasswordGenerator.tryGeneratingPassword();
        return password != null ? password.toCharArray() : null;
    }

    @Override
    public void subscribe(String topic) {
        trySubscribing(topic);
    }

    @Override
    public void publish(String topic, String message) {
        try {
            tryPublishing(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void tryPublishing(String topic, String message) throws MqttException {
        byte[] payload = message.getBytes();
        MqttMessage mqttMessage = new MqttMessage(payload);
        mqttMessage.setQos(AT_LEAST_ONCE_QOS);
        mqttMessage.setRetained(true);
        mMqttClient.publish(topic, mqttMessage);
    }

    @Override
    public void addOnConnectionSuccessListener(OnConnectionSuccessListener listener) {
        mOnConnectionSuccessListeners.add(listener);
    }

    @Override
    public void removeOnConnectionSuccessListener(OnConnectionSuccessListener listener) {
        mOnConnectionSuccessListeners.remove(listener);
    }

    @Override
    public void addOnMessageArrivedListener(OnMessageArrivedListener listener) {
        mOnMessageArrivedListeners.add(listener);
    }

    @Override
    public void removeOnMessageArrivedListener(OnMessageArrivedListener listener) {
        mOnMessageArrivedListeners.remove(listener);
    }

    public static class MqttHelperBuilder {

        private Context mContext;
        private PasswordGenerator mPasswordGenerator;
        private String mHostUrl;
        private String mClientId;
        private int mKeepAliveIntervalSeconds = 20 * 60;   // 20 minutes default value
        private String mConnectionUserName;

        public MqttHelperBuilder context(Context context) throws IllegalArgumentException {
            mContext = context;
            return this;
        }

        public MqttHelperBuilder passwordGenerator(PasswordGenerator passwordGenerator) {
            mPasswordGenerator = passwordGenerator;
            return this;
        }

        public MqttHelperBuilder hostUrl(String hostUrl) {
            mHostUrl = hostUrl;
            return this;
        }

        public MqttHelperBuilder clientId(String clientId) {
            mClientId = clientId;
            return this;
        }

        public MqttHelperBuilder keepAliveIntervalSeconds(int seconds) {
            mKeepAliveIntervalSeconds = seconds;
            return this;
        }

        public MqttHelperBuilder connectionUserName(String name) {
            mConnectionUserName = name;
            return this;
        }

        public MqttHelperImp build() {
            if (!isValid()) throw new IllegalStateException();
            return new MqttHelperImp(this);
        }

        private boolean isValid() {
            return mContext != null && mPasswordGenerator != null && mHostUrl != null
                    && mClientId != null && mConnectionUserName != null;
        }

        private Context getContext() {
            return mContext;
        }

        private PasswordGenerator getPasswordGenerator() {
            return mPasswordGenerator;
        }

        private String getHostUrl() {
            return mHostUrl;
        }

        private String getClientId() {
            return mClientId;
        }

        private int getKeepAliveIntervalSeconds() {
            return mKeepAliveIntervalSeconds;
        }

        private String getConnectionUserName() {
            return mConnectionUserName;
        }
    }
}
