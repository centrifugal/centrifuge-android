package com.danilov.acentrifugo;

import android.os.Parcel;
import android.os.Parcelable;
import javax.annotation.Nonnull;

/**
 * Created by semyon on 08.02.16.
 */
public class ConnectionInfo implements Parcelable {

    public static final int SUBSCRIBED_TO_CHANNEL = 0;
    public static final int UNSUBSCRIBED_FROM_CHANNEL = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECTED = 3;
    public static final int ERROR = 4;
    public static final int CONNECTING = 5;

    private int state;

    @Nonnull
    private String value;

    public ConnectionInfo(final int state, @Nonnull final String value) {
        this.state = state;
        this.value = value;
    }

    protected ConnectionInfo(Parcel in) {
        state = in.readInt();
        value = in.readString();
    }

    public static final Creator<ConnectionInfo> CREATOR = new Creator<ConnectionInfo>() {
        @Override
        public ConnectionInfo createFromParcel(Parcel in) {
            return new ConnectionInfo(in);
        }

        @Override
        public ConnectionInfo[] newArray(int size) {
            return new ConnectionInfo[size];
        }
    };

    @Nonnull
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