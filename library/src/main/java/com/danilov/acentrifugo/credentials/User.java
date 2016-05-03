package com.danilov.acentrifugo.credentials;

import javax.annotation.Nonnull;

/**
 * Created by Semyon on 03.05.2016.
 */
public class User {

    @Nonnull
    private String user;

    @Nonnull
    private String client;

    public User(@Nonnull final String user, @Nonnull final String client) {
        this.user = user;
        this.client = client;
    }

    @Nonnull
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