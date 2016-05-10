package com.centrifugal.centrifuge.android.util;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 01.05.2016.
 * */
public class Signing {

    private static final String secret = "very-long-secret-key";

    public static String generateConnectionToken(final String userId, final String timestamp, final String info) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {}
        try {
            mac.init(secretKeySpec);
        } catch (InvalidKeyException e) {}
        mac.update(userId.getBytes());
        mac.update(timestamp.getBytes());
        byte[] hmac = mac.doFinal(info.getBytes());
        byte[] encode = new Hex().encode(hmac);
        try {
            return new String(encode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String generateApiToken(final String data) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {}
        try {
            mac.init(secretKeySpec);
        } catch (InvalidKeyException e) {}
        byte[] hmac = mac.doFinal(data.getBytes());
        byte[] encode = new Hex().encode(hmac);
        try {
            return new String(encode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

}
