package org.demoth.gameserver

import org.demoth.gameserver.api.messaging.*
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.web.socket.WebSocketHandler

@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = [WebSocketConfig::class])
class IntegrationTest {

    @Autowired
    lateinit var handler: WebSocketHandler

    @Test
    fun `login with wrong credentials`() {
        val session = TestWebsocketSession()
        Assert.assertNotNull(handler)
        testMessages(session, LoginMessage("1", "1"))
        Assert.assertFalse(session.isOpen)
        Assert.assertEquals(session.getMessages().first(), ErrorMessage("Wrong credentials"))
    }

    @Test
    fun `test login join move`() {
        val session = TestWebsocketSession()
        Assert.assertNotNull(handler)
        testMessages(session,
                LoginMessage("test", "test"),
                JoinMessage("test character 1"),
                MoveAction("e")
        )
        Thread.sleep(3000)
        println("messages: ${session.getMessages().size}")
        Assert.assertTrue(session.getMessages().any { it is LoggedInMessage })
        Assert.assertTrue(session.getMessages().any { it is JoinedMessage })
        Assert.assertTrue(session.getMessages().any { it is UpdateMessage && it.updates.any { it is Movement } })
    }

    private fun testMessages(session: TestWebsocketSession, vararg msg: Message) {
        msg.forEach {
            handler.handleMessage(session, it.encode())
        }
    }
}