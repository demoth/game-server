package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.Message;
import org.demoth.aworlds.server2.api.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class TestClient {

    private static final Logger LOG = LoggerFactory.getLogger(TestClient.class);

    public static void main(String[] args) throws URISyntaxException, IOException, DeploymentException, InterruptedException {
        ObjectMapper mapper = new ObjectMapper();
        TestEndpoint te = new TestEndpoint(new URI("ws://localhost:8080/action.json"));
        TestHandler tester = new TestHandler(te);
        te.addMessageHandler(tester);
        Message login = new Message(MessageType.LOGIN, "demoth", "cadaver");
        te.sendMessage(mapper.writeValueAsString(login));
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
                Message msg = mapper.readValue(message, Message.class);
                switch (msg.type) {
                    case TEXT:
                        LOG.debug(msg.params[0]);
                        break;
                    case LOGGED_IN:
                        LOG.debug("Logged in! Chars: " + Arrays.toString(msg.params));
                        endpoint.sendMessage(mapper.writeValueAsString(new Message(MessageType.JOIN, msg.params[0])));
                        break;
                    case ERROR:
                        LOG.error("Error: " + msg.params[0]);
                        done = true;
                        break;
                    case JOINED:
                        LOG.debug("Joined: " + message);
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

