package com.centrifuge.android.testwebapp;

import org.json.simple.JSONObject;

import fi.iki.elonen.NanoHTTPD;

/**
 * This file is part of ACentrifugo.
 * <p/>
 * ACentrifugo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * ACentrifugo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ACentrifugo.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by Semyon on 08.05.2016.
 */
public class HttpServer extends NanoHTTPD {

    private String centrifugoAPIAddress;
    private String centrifugoWSAddress;

    public HttpServer(final int port) {
        super(port);
    }

    @Override
    public Response serve(final IHTTPSession session) {
        System.out.println("\nNew request:");
        System.out.println(session.getMethod() + " : " + session.getUri() + " " + session.getParms().toString() + "\n");
        String uri = session.getUri();
        if (uri == null) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_HTML, "");
        }
        switch (uri) {
            case "/token":
                return onTokenRequest(session);
            default:
                return newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_HTML, "");
        }
    }

    private Response onTokenRequest(final IHTTPSession session) {
        String timestamp = System.currentTimeMillis() + "";
        String userId = "";
        String info = "";
        String num = session.getParms().get("userId");
        userId += num;
        String token = Signing.generateConnectionToken(userId, timestamp, info);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("token", token);
        jsonObject.put("centrifugoWS", centrifugoWSAddress);
        return newFixedLengthResponse(Response.Status.OK, "application/json", jsonObject.toJSONString());
    }

    public void init(final String centrifugoAPIAddress, final String centrifugoWSAddress) {
        this.centrifugoAPIAddress = centrifugoAPIAddress;
        this.centrifugoWSAddress = centrifugoWSAddress;
    }

}