package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.messaging.MapLike;
import org.demoth.aworlds.server2.api.messaging.MessageParser;
import org.demoth.aworlds.server2.api.messaging.fromClient.JoinMessage;
import org.demoth.aworlds.server2.api.messaging.fromClient.LoginMessage;
import org.demoth.aworlds.server2.api.messaging.fromServer.JoinedMessage;
import org.demoth.aworlds.server2.api.messaging.fromServer.LoggedInMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class TestClient {

    private static final Logger LOG = LoggerFactory.getLogger(TestClient.class);

    public static void main(String[] args) throws URISyntaxException, IOException, DeploymentException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        TestEndpoint te = new TestEndpoint(new URI("ws://localhost:8080/action.json"));
        TestHandler tester = new TestHandler(te);
        te.addMessageHandler(tester);
        te.sendMessage(mapper.writeValueAsString(new LoginMessage("demoth", "cadaver").toMap()));
        while (!tester.done()) {
            Thread.sleep(100);

        }
    }

    static class TestHandler implements TestEndpoint.MessageHandler {
        private final TestEndpoint endpoint;
        boolean done = false;
        ObjectMapper mapper = new ObjectMapper();

        TestHandler(TestEndpoint te) {
            this.endpoint = te;
        }

        @Override
        public void handleMessage(String message) {
            try {
                Map map = mapper.readValue(message, Map.class);
                LOG.debug("Received {}", map);
                MapLike msg = MessageParser.fromMap(map);
                if (msg instanceof LoggedInMessage) {
                    LoggedInMessage loggedIn = (LoggedInMessage) msg;
                    LOG.debug("Logged in! Chars: " + loggedIn.characters);
                    endpoint.sendMessage(mapper.writeValueAsString(new JoinMessage(loggedIn.characters.stream().findAny().get()).toMap()));
                } else if (msg instanceof JoinedMessage) {
                    JoinedMessage joined = (JoinedMessage) msg;
                    LOG.debug("Joined: " + joined);
                }
            } catch (Exception e) {
                LOG.error("Error: ", e);
                done = true;
            }
        }


        boolean done() {
            return done;
        }
    }
}

