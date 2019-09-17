package com.noon.mqtt;

import android.util.Base64;

import androidx.annotation.Nullable;

import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.jsonwebtoken.Jwts;

/**
 * Created by Islam Salah on 2019-08-15.
 * <p>
 * https://github.com/IslamSalah
 * islamsalah007@gmail.com
 */
public class PasswordGenerator {

    private static final String SIGNING_ALGORITHM = "RSA";
    private static final int PASSWORD_EXPIRY_LENGTH = 20; // minutes
    private static final int PERIOD_BETWEEN_PASSWORD_GENERATION = 1; // minutes

    private final String mKey;
    private final String mAudience;
    private OnPasswordGeneratedListener mOnPasswordGeneratedListener;
    private ScheduledExecutorService mExecutorService;

    public PasswordGenerator(String key, String audiance) {
        mKey = key;
        mAudience = audiance;
    }

    /**
     * The device will be disconnected after the token expires, and will
     * have to reconnect with a new token.
     */
    void generatePeriodically() {
        if (mExecutorService != null) return;
        mExecutorService = Executors.newSingleThreadScheduledExecutor();
        mExecutorService.scheduleAtFixedRate(this::notifyListenerWithNewPassword,
                /* Initial delay */0, PERIOD_BETWEEN_PASSWORD_GENERATION, TimeUnit.MINUTES);
    }

    void stopGenerating() {
        if (mExecutorService == null) return;
        mExecutorService.shutdown();
        mExecutorService = null;
    }

    private void notifyListenerWithNewPassword() {
        if (mOnPasswordGeneratedListener == null) return;
        String newPassword = tryGeneratingPassword();
        mOnPasswordGeneratedListener.onPasswordGenerated(newPassword);
    }

    @Nullable
    public String tryGeneratingPassword() {
        try {
            return generatePassword();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String generatePassword() throws GeneralSecurityException {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(getDateAfter(now, PASSWORD_EXPIRY_LENGTH))
                .setAudience(mAudience)
                .signWith(getPrivateKey(mKey))
                .compact();
    }

    private Date getDateAfter(Date reference, int minutesCount) {
        long futureDateMilliseconds = reference.getTime() + TimeUnit.MINUTES.toMillis(minutesCount);
        return new Date(futureDateMilliseconds);
    }

    private Key getPrivateKey(String privateKey) throws GeneralSecurityException {
        privateKey = removeKeyUnusedCharacters(privateKey);

        byte[] encoding = Base64.decode(privateKey, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoding);

        return KeyFactory.getInstance(SIGNING_ALGORITHM).generatePrivate(keySpec);
    }

    private String removeKeyUnusedCharacters(String key) {
        key = key.replace("-----BEGIN PRIVATE KEY-----", "");
        key = key.replace("-----END PRIVATE KEY-----", "");
        return key.replaceAll("\\s+", ""); // remove all white spaces
    }

    void setOnPasswordGeneratedListener(OnPasswordGeneratedListener listener) {
        mOnPasswordGeneratedListener = listener;
    }

    interface OnPasswordGeneratedListener {

        void onPasswordGenerated(String password);
    }
}
