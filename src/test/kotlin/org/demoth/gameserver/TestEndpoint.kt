package org.demoth.aworlds.server2

import javax.websocket.*
import java.io.IOException
import java.net.URI

@ClientEndpoint
class TestEndpoint @Throws(IOException::class, DeploymentException::class)
constructor(uri: URI) {
    internal var userSession: Session? = null
    private var messageHandler: MessageHandler? = null

    init {
        val container = ContainerProvider.getWebSocketContainer()
        container.connectToServer(this, uri)
    }

    @OnOpen
    fun onOpen(userSession: Session) {
        println("opening websocket")
        this.userSession = userSession
    }


    @OnClose
    fun onClose(userSession: Session, reason: CloseReason) {
        println("closing websocket")
        this.userSession = null
    }

    @OnMessage
    fun onMessage(message: String) {
        if (this.messageHandler != null) {
            this.messageHandler!!.handleMessage(message)
        }
    }

    fun addMessageHandler(msgHandler: MessageHandler) {
        this.messageHandler = msgHandler
    }


    fun sendMessage(message: String) {
        this.userSession!!.asyncRemote.sendText(message)
    }


    interface MessageHandler {
        fun handleMessage(message: String)
    }
}
