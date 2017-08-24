package org.demoth.aworlds.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.Request;
import org.demoth.aworlds.server2.api.RequestType;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TestClient {
    public static void main(String[] args) throws URISyntaxException, IOException, DeploymentException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        TestEndpoint te = new TestEndpoint(new URI("ws://localhost:8080/action"));
        te.addMessageHandler(message -> System.out.println("Client received message: " + message));
        Request login = new Request(RequestType.LOGIN, new String[]{"demoth", "cadaver"});
        te.sendMessage(mapper.writeValueAsString(login));
    }
}

