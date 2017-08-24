package org.demoth.aworlds.server;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class TestEndpoint {
    Session userSession = null;
    private MessageHandler messageHandler;

    public TestEndpoint(URI uri) throws IOException, DeploymentException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, uri);
    }

    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }


    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket");
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }


    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }


    public interface MessageHandler {
        void handleMessage(String message);
    }
}
