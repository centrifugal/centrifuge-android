package com.centrifuge.android.testwebapp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import fi.iki.elonen.NanoHTTPD;

public class TestWebApp {

    public static void main(String[] args) {
        Options options = new Options();
        Option useDocker = new Option("d", "docker", false, "Use dockerized Centrifugo");
        Option httpAppPort = new Option("p", "port", true, "Port to listen for webapp");
        Option centrifugoAddress = new Option("a", "address", true, "Centrifugo's IP address (if not dockerized) WITHOUT protocol (http[s])");
        options.addOption(useDocker);
        options.addOption(httpAppPort);
        options.addOption(centrifugoAddress);

        CommandLineParser cmdLinePosixParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = cmdLinePosixParser.parse(options, args);
        } catch (ParseException e) {
            printHelp(options);
            return;
        }
        if (commandLine == null) {
            printHelp(options);
            return;
        }
        boolean useDockerVal = false;
        if (commandLine.hasOption("d")) {
            useDockerVal = true;
        }
        int portValue = 8080;
        if (commandLine.hasOption("p")) {
            try {
                portValue = Integer.parseInt(commandLine.getOptionValue("p"));
            } catch (NumberFormatException e) {
                printHelp(options);
                return;
            }
        }
        String centrifugoAddressValue = null;
        if (!useDockerVal && commandLine.hasOption("a")) {
            centrifugoAddressValue = commandLine.getOptionValue("a");
        }
        if (!useDockerVal && centrifugoAddressValue == null) {
            printHelp(options);
            return;
        }
        TestWebApp testWebApp = new TestWebApp(useDockerVal, portValue, centrifugoAddressValue);
        testWebApp.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Stopping");
                testWebApp.destroy();
                System.out.println("Stopped");
            }
        });
    }

    private GenericContainer centrifugo;
    private HttpServer server;
    private int webAppPort;
    private String centrifugoAPIAddress;
    private String centrifugoWSAddress;
    private String centrifugoWebAddress;
    private boolean usingDocker = false;


    public TestWebApp(final boolean useDocker, final int webAppPort, final String centrifugoAddress) {
        String ipAddress;
        if (useDocker) {
            centrifugo = new GenericContainer("samvimes/centrifugo:latest")
                    .withExposedPorts(8000);
            centrifugo.start();
            ipAddress = centrifugo.getContainerIpAddress() + ":" + centrifugo.getMappedPort(8000);
            usingDocker = true;
        } else {
            ipAddress = centrifugoAddress;
        }
        this.webAppPort = webAppPort;

        centrifugoWSAddress = "ws://" + ipAddress + "/connection/websocket";
        centrifugoAPIAddress = "http://" + ipAddress + "/api/";
        centrifugoWebAddress = "http://" + ipAddress;
    }

    private void start() {
        server = new HttpServer(webAppPort);
        server.init(centrifugoAPIAddress, centrifugoWSAddress);
        try {
            server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        } catch (IOException e) {
            System.err.println("Failed to run webapp:" + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
        System.out.println("Running webapp on localhost:" + webAppPort);
        if (usingDocker) {
            System.out.println("You can send requests via Centrifugo HTTP API " + centrifugoAPIAddress
                    + " (secret: very-long-secret-key) or via web-interface " + centrifugoWebAddress + "/"
                    + " (password: strong_password_to_log_in)");
        } else {
            System.out.println("You can send requests via Centrifugo HTTP API " + centrifugoAPIAddress
                    + " (secret is in your config.json) or via web-interface " + centrifugoWebAddress
                    + " (Centrifugo must be run with --web parameter, web_password is in your config.json");
        }
        System.out.println("Documentation can be found here: https://fzambia.gitbooks.io/centrifugal/content/");
    }

    private void destroy() {
        if (centrifugo != null) {
            centrifugo.stop();
        }
        if (server != null) {
            server.stop();
        }
    }

    public static void printHelp(final Options options ) {
        printHelp(options, 80, "Options", "-- HELP --", 3, 5, true, System.out);
    }

    public static void printHelp(
            final Options options,
            final int printedRowWidth,
            final String header,
            final String footer,
            final int spacesBeforeOption,
            final int spacesBeforeOptionDescription,
            final boolean displayUsage,
            final OutputStream out) {
        final String commandLineSyntax = "java TestWebApp.jar";
        final PrintWriter writer = new PrintWriter(out);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(
                writer,
                printedRowWidth,
                commandLineSyntax,
                header,
                options,
                spacesBeforeOption,
                spacesBeforeOptionDescription,
                footer,
                displayUsage);
        writer.flush();
    }

}
