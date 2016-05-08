package com.centrifugal.centrifuge.android.util;

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * This file is part of ACentrifugo.
 *
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see <http://www.gnu.org/licenses/>.
 *
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
