package org.demoth.gameserver

import com.fasterxml.jackson.databind.ObjectMapper
import org.demoth.gameserver.api.messaging.ErrorMessage
import org.demoth.gameserver.api.messaging.LoginMessage
import org.demoth.gameserver.api.messaging.decode
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler


@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [WebSocketConfig::class])
class IntegrationTest {

    @Autowired
    lateinit var handler: WebSocketHandler

    private val mapper = ObjectMapper()

    @Test
    fun `login with wrong credentials`() {
        val session = TestWebsocketSession()
        Assert.assertNotNull(handler)
        handler.handleMessage(session, TextMessage(mapper.writeValueAsString(LoginMessage("1", "1"))))
        Assert.assertFalse(session.isOpen)
        Assert.assertEquals(decode(session.sentMessages[0]!!, mapper), ErrorMessage("Wrong credentials"))

    }
}