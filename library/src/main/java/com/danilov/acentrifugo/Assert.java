package com.danilov.acentrifugo;

/**
 * Created by semyon on 08.02.16.
 */
public class Assert {

    public static void isTrue(final boolean val) {
        if (BuildConfig.DEBUG && !val) {
            throw new AssertionError();
        }
    }

}
