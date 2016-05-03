package com.danilov.acentrifugo.credentials;

import javax.annotation.Nonnull;

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
 * Created by Semyon on 03.05.2016.
 * */
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