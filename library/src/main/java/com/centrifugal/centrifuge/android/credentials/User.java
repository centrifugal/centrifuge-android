package com.centrifugal.centrifuge.android.credentials;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This file is part of centrifuge-android
 * Created by Semyon on 03.05.2016.
 * */
public class User {

    @Nonnull
    private String user;

    @Nullable
    private String client;

    public User(@Nonnull final String user, @Nullable final String client) {
        this.user = user;
        this.client = client;
    }

    @Nullable
    public String getClient() {
        return client;
    }

    @Nonnull
    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                "user='" + user + '\'' +
                ", client='" + client + '\'' +
                '}';
    }

}