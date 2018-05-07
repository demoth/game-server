package org.demoth.gameserver.model

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.messaging.AppearData
import org.demoth.gameserver.api.messaging.Message
import org.demoth.gameserver.api.messaging.Update
import org.springframework.web.socket.WebSocketSession
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.LinkedBlockingQueue

class Player : Actor(ActorType.CREATURE, name = "Player") {

    var appeared = false

    var commands = ConcurrentLinkedQueue<Message>()

    // this cache holds appear data sent from server to client.
    // scope - session
    var cache: MutableMap<String, AppearData> = HashMap()

    // this set holds ids of object that were visible previous frame.
    // used to calculate appear/disappear data
    // scope - frame
    var sightLastFrame: MutableSet<String> = TreeSet()

    val results = LinkedBlockingQueue<Update>()

    var location: Location? = null

    var session: WebSocketSession? = null

    val update: Update
        @Throws(InterruptedException::class)
        get() = results.take()

    fun enqueueRequest(command: Message) {
        commands.add(command)
    }

    fun enqueueResponse(response: Update) {
        results.add(response)
    }

    fun idle(): Boolean {
        return commands.isEmpty()
    }
}
