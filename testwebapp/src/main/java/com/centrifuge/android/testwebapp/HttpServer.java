package com.centrifuge.android.testwebapp;

import org.json.simple.JSONObject;

import fi.iki.elonen.NanoHTTPD;

/**
 * This file is part of centrifuge-android
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