package org.demoth.gameserver

import org.demoth.gameserver.api.messaging.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.socket.WebSocketHandler

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [WebSocketConfig::class])
class IntegrationTest {

    @Autowired
    lateinit var handler: WebSocketHandler

    @Test
    fun `login with wrong credentials`() {
        val session = TestWebsocketSession()
        assertNotNull(handler)
        testMessages(session, LoginMessage("1", "1"))
        assertFalse(session.isOpen)
        assertEquals(session.getMessages().first(), ErrorMessage("Wrong credentials"))
    }

    @Test
    fun `test login join move`() {
        val session = TestWebsocketSession()
        assertNotNull(handler)
        testMessages(session,
                LoginMessage("test", "test"),
                JoinMessage("test character 1"),
                MoveAction("e")
        )
        Thread.sleep(3000)
        println("messages: ${session.getMessages().size}")
        assertTrue(session.getMessages().any { it is LoggedInMessage })
        assertTrue(session.getMessages().any { it is JoinedMessage })
        assertTrue(session.getMessages().any { it is UpdateMessage && it.updates.any { it is Movement } })
    }

    private fun testMessages(session: TestWebsocketSession, vararg msg: Message) {
        msg.forEach {
            handler.handleMessage(session, it.encode())
        }
    }
}