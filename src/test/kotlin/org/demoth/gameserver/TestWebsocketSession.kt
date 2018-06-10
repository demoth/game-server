package org.demoth.gameserver

import org.demoth.gameserver.api.messaging.Message
import org.demoth.gameserver.api.messaging.decode
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketExtension
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.InetSocketAddress
import java.net.URI
import java.security.Principal
import java.util.*

class TestWebsocketSession : WebSocketSession {

    private val id = UUID.randomUUID().toString()
    private var closed = false

    private val sentMessages = mutableListOf<WebSocketMessage<*>>()

    companion object {
        val LOG = LoggerFactory.getLogger(TextWebSocketHandler::class.java)
    }

    fun getMessages(): List<Message> {
        return sentMessages.map { decode(it) }
    }

    override fun sendMessage(message: WebSocketMessage<*>) {
        LOG.debug("sendMessage: $message")
        sentMessages.add(message)
    }

    override fun getId(): String {
        return id
    }

    override fun isOpen(): Boolean {
        return !closed
    }


    override fun close() {
        closed = true
    }

    // NOT IMPLEMENTED //

    override fun close(p0: CloseStatus?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBinaryMessageSizeLimit(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAcceptedProtocol(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTextMessageSizeLimit(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLocalAddress(): InetSocketAddress {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun getExtensions(): MutableList<WebSocketExtension> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUri(): URI {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setBinaryMessageSizeLimit(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAttributes(): MutableMap<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getHandshakeHeaders(): HttpHeaders {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPrincipal(): Principal {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setTextMessageSizeLimit(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRemoteAddress(): InetSocketAddress {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}