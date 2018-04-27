package org.demoth.gameserver

import com.fasterxml.jackson.databind.ObjectMapper
import org.demoth.gameserver.api.messaging.Message
import org.demoth.gameserver.api.messaging.JoinMessage
import org.demoth.gameserver.api.messaging.LoginMessage
import org.demoth.gameserver.api.messaging.JoinedMessage
import org.demoth.gameserver.api.messaging.LoggedInMessage
import org.slf4j.LoggerFactory

import javax.websocket.DeploymentException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

object TestClient {

    private val LOG = LoggerFactory.getLogger(TestClient::class.java)

    @Throws(URISyntaxException::class, IOException::class, DeploymentException::class, InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val mapper = ObjectMapper()
        val te = TestEndpoint(URI("ws://localhost:8080/action.json"))
        val tester = TestHandler(te)
        te.addMessageHandler(tester)
        te.sendMessage(mapper.writeValueAsString(LoginMessage("demoth", "cadaver")))
        while (!tester.done()) {
            Thread.sleep(100)
        }
    }

    internal class TestHandler(private val endpoint: TestEndpoint) : TestEndpoint.MessageHandler {
        var done = false
        var mapper = ObjectMapper()

        override fun handleMessage(message: String) {
            try {
                LOG.debug("Received {}", message)
                val msg = mapper.readValue(message, Message::class.java)
                if (msg is LoggedInMessage) {
                    LOG.debug("Logged in! Chars: " + msg.characters)
                    endpoint.sendMessage(mapper.writeValueAsString(JoinMessage(msg.characters.stream().findAny().get())))
                } else if (msg is JoinedMessage) {
                    LOG.debug("Joined: $msg")
                } else {
                    println("msg = $msg")
                }
            } catch (e: Exception) {
                LOG.error("Error: ", e)
                done = true
            }

        }


        fun done(): Boolean {
            return done
        }
    }
}

