package org.demoth.gameserver

import org.demoth.gameserver.api.messaging.AppearData
import org.demoth.gameserver.api.messaging.StateChangeData
import org.demoth.gameserver.api.messaging.Update
import org.demoth.gameserver.model.Location
import org.demoth.gameserver.model.Player
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
open class LocationWorkerManager {

    private val instances = ConcurrentHashMap<Location, Thread>()

    fun runLocation(location: Location) {
        val worker = Thread {
            LOG.debug("Start updating {}", location.name)
            while (true) {
                if (location.players.isEmpty()) {
                    LOG.debug("No players in {}, exiting...", location.name)
                    return@Thread
                }
                val updates = location.updateLocation()
                val allPlayersReady = location.players.none({ it.idle() })
                // todo: make configurable
                val sleep = if (allPlayersReady) 100 else 2000
                try {
                    Thread.sleep(sleep.toLong())
                } catch (e: InterruptedException) {
                    LOG.debug("Finishing working thread: {}", Thread.currentThread().name)
                    // todo persist location
                    return@Thread
                }

            }
        }
        instances[location] = worker
        worker.name = "Location worker: ${location.name}"
        worker.start()
    }

    fun stopLocation(location: Location) {
        val worker = instances[location]
        worker?.interrupt()
    }

    /*
     * For each player filter updates and put them to the send queue.
     */
    @Deprecated("")
    private fun filterUpdates(updates: Collection<Update>, players: Collection<Player>) {
        players.forEach { player ->
            updates.stream().filter { message ->
                if (message is StateChangeData) {
                    val data = message
                    return@filter player.id == data.id
                }
                if (message is AppearData) {
                    if (message.id == player.id) {
                        if (player.appeared) {
                            return@filter false
                        } else {
                            player.appeared = true
                            return@filter true
                        }
                    }
                    if (Math.abs(message.x - player.x) > player.sightRadius)
                        return@filter false
                    if (Math.abs(message.y - player.y) > player.sightRadius)
                        return@filter false

                    // todo: think about caching
                    val key = message.x.toString() + ":" + message.y
                    if (player.cache.containsKey(key) && player.cache[key] == message)
                        return@filter false
                    else {
                        player.cache.put(key, message)
                    }
                    return@filter true
                }
                false
            }.forEach({ player.enqueueResponse(it) })
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LocationWorkerManager::class.java)
    }
}
