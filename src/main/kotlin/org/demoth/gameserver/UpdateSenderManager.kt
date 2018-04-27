package org.demoth.gameserver

import com.fasterxml.jackson.databind.ObjectMapper
import org.demoth.gameserver.api.messaging.UpdateMessage
import org.demoth.gameserver.model.Player
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage

import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
open class UpdateSenderManager {

    private val mapper: ObjectMapper = ObjectMapper()
    private val players = ConcurrentHashMap<Player, Thread>()

    fun startSendingUpdates(player: Player) {
        val sender = Thread {
            LOG.debug("Started sending updates for player {} ", player.name)
            while (true) {
                try {
                    val change = player.update
                    val update = UpdateMessage(listOf(change))
                    player.session!!.sendMessage(TextMessage(mapper.writeValueAsString(update)))
                } catch (e: IOException) {
                    LOG.error("Error while sending updates", e)
                    return@Thread
                } catch (e: InterruptedException) {
                    LOG.debug("Finished sending updates for {}", player.name)
                    // todo persist player
                    return@Thread
                }

            }
        }
        players[player] = sender
        sender.name = "Update sender for " + player.name!!
        sender.start()
    }

    fun stopSendingUpdates(player: Player) {
        val sender = players[player]
        sender?.interrupt()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(UpdateSenderManager::class.java)
    }
}
