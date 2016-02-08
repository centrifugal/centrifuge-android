package com.danilov.acentrifugo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by semyon on 08.02.16.
 */
public class Info implements Parcelable {

    public static final int SUBSCRIBED_TO_CHANNEL = 0;
    public static final int UNSUBSCRIBED_FROM_CHANNEL = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECTED = 3;
    public static final int ERROR = 4;
    public static final int CONNECTING = 5;

    private int state;

    @NonNull
    private String value;

    public Info(final int state, @NonNull final String value) {
        this.state = state;
        this.value = value;
    }

    protected Info(Parcel in) {
        state = in.readInt();
        value = in.readString();
    }

    public static final Creator<Info> CREATOR = new Creator<Info>() {
        @Override
        public Info createFromParcel(Parcel in) {
            return new Info(in);
        }

        @Override
        public Info[] newArray(int size) {
            return new Info[size];
        }
    };

    @NonNull
    public String getValue() {
        return value;
    }

    public int getState() {
        return state;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(state);
        dest.writeString(value);
    }

}