package com.badjoras.safetynettest.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

import timber.log.Timber;

/**
 * Created by baama on 14/02/2017.
 */

public class JWTUtils {

    public static String decodedGetHeader(String JWTEncoded) throws Exception {
        try {
            String[] split = JWTEncoded.split("\\.");
            Timber.e("Header: %s",getJson(split[0]));
            Timber.e("Body: %s", getJson(split[1]));
            return getJson(split[0]);
        } catch (UnsupportedEncodingException e) {
            //Error
            return "";
        }
    }

    public static String decodedGetBody(String JWTEncoded) throws Exception {
        try {
            String[] split = JWTEncoded.split("\\.");
            Timber.e("Header: %s",getJson(split[0]));
            Timber.e("Body: %s", getJson(split[1]));
            return getJson(split[1]);
        } catch (UnsupportedEncodingException e) {
            //Error
            return "";
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }



}
