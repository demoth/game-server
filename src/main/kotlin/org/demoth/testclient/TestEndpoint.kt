package org.demoth.testclient

import java.io.IOException
import java.net.URI
import javax.websocket.*

@ClientEndpoint
class TestEndpoint @Throws(IOException::class, DeploymentException::class)
constructor(uri: URI) {
    private var userSession: Session? = null
    private var messageHandler: MessageHandler? = null

    init {
        val container = ContainerProvider.getWebSocketContainer()
        container.connectToServer(this, uri)
    }

    @OnOpen
    fun onOpen(userSession: Session) {
        this.userSession = userSession
    }


    @OnClose
    fun onClose(userSession: Session, reason: CloseReason) {
        this.userSession = null
    }

    @OnMessage
    fun onMessage(message: String) {
        this.messageHandler?.handleMessage(message)
    }

    fun addMessageHandler(msgHandler: MessageHandler) {
        this.messageHandler = msgHandler
    }


    fun sendMessage(message: String) {
        this.userSession?.asyncRemote?.sendText(message)
    }

    fun disconnect() {
        userSession?.close()

    }


    interface MessageHandler {
        fun handleMessage(message: String)
    }
}
