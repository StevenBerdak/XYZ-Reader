package com.sbschoolcode.xyzreader.remote;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

class Config {
    public static final URL BASE_URL;
    private static final String TAG = Config.class.toString();

    static {
        URL url = null;
        try {
            url = new URL("https://go.udacity.com/xyz-reader-json" );
        } catch (MalformedURLException e) {
            // TODO: throw a real error
            // RESPONSE: How are you supposed to throw an exception (error?) from a static initialization block?
            Log.e(TAG, "The URL could not be parsed");
        }

        BASE_URL = url;
    }
}
