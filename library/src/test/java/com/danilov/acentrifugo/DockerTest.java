package com.danilov.acentrifugo;

import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.apache.maven.wagon.observers.Debug;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by semyon on 29.04.16.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DockerTest {

    public GenericContainer dockerizedCentrifugo;

    private MockWebServer mockWebServer;

    @Before
    public void beforeMethod() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        dockerizedCentrifugo = new GenericContainer("fzambia/centrifugo:latest")
                .withExposedPorts(8000)
                .withClasspathResourceMapping("centrifugo", "/centrifugo", BindMode.READ_ONLY);
    }

    @After
    public void afterMethod() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void test() throws Exception {
        System.out.println("asdasd");
        String containerIpAddress = dockerizedCentrifugo.getContainerIpAddress();
        String port = "8000";


        mockWebServer.setDispatcher(new TestWebapp());
        String url = mockWebServer.url("/tokens").toString();

        OkHttpClient okHttpClient = new OkHttpClient();

        Request build = new Request.Builder().url(url).build();
        Response execute = okHttpClient.newCall(build).execute();
        String body = execute.body().string();
        JSONObject jsonObject = new JSONObject(body);
        String userId = jsonObject.optString("userId");
        String timestamp = jsonObject.optString("timestamp");
        String token = jsonObject.optString("token");
        Centrifugo centrifugo = new Centrifugo(containerIpAddress + port,
                userId, null, token, timestamp);
        centrifugo.start();
    }

}
