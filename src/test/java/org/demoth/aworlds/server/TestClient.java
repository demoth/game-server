package org.demoth.aworlds.server;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TestClient {
    public static void main(String[] args) throws URISyntaxException, IOException, DeploymentException, InterruptedException {
        TestEndpoint te = new TestEndpoint(new URI("ws://localhost:8080/action"));
        te.addMessageHandler(message -> System.out.println("Client received message: " + message));
        Thread client = new Thread(() -> {
            while (true) {
                te.sendMessage("Hi this is client");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        client.start();
        client.join();
    }
}

