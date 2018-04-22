package org.demoth.aworlds.server2.model

import org.demoth.aworlds.server2.api.messaging.Message
import org.demoth.aworlds.server2.api.messaging.fromClient.CommandMessage
import org.demoth.aworlds.server2.api.messaging.fromServer.AppearData
import org.springframework.web.socket.WebSocketSession

import java.util.HashMap
import java.util.TreeSet
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue

class Player : Actor() {

    var appeared = false

    var commands = ConcurrentLinkedQueue<CommandMessage>()

    // this cache holds appear data sent from server to client.
    // scope - session
    var cache: MutableMap<String, AppearData> = HashMap()

    // this set holds ids of object that were visible previous frame.
    // used to calculate appear/disappear data
    // scope - frame
    var sightLastFrame: MutableSet<String> = TreeSet()

    private val results = LinkedBlockingQueue<Message>()

    var location: Location? = null

    var session: WebSocketSession? = null

    val update: Message
        @Throws(InterruptedException::class)
        get() = results.take()

    fun enqueueRequest(command: CommandMessage) {
        commands.add(command)
    }

    fun enqueueResponse(response: Message) {
        results.add(response)
    }

    fun idle(): Boolean {
        return commands.isEmpty()
    }
}
