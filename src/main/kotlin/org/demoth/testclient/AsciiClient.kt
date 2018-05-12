package org.demoth.testclient

import com.fasterxml.jackson.databind.ObjectMapper
import org.demoth.gameserver.api.messaging.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI

enum class State {
    OFFLINE,
    CONNECTED,
    LOGGED_IN,
    IN_GAME
}

const val ESC = "\u001B"

const val NORMAL = ESC + "[0"
const val BOLD = ESC + "[1"
const val BLACK = ESC + "[0;40m"  // black background
const val WHITE = ESC + "[0;37m"  // normal white foreground

object TestClient {

    var state = State.OFFLINE
    var characters: List<String>? = null
    var myId: String? = null
    private val objects: MutableMap<String, AppearData> = mutableMapOf()
    private var board: Array<Array<Char>> = arrayOf()

    private var minx = 0
    private var maxx = 16
    private var miny = 0
    private var maxy = 16

    @JvmStatic
    fun main(args: Array<String>) {
        val mapper = ObjectMapper()
        log("Welcome to testclient! (q)uit, (c)onnect, (l)ogin, (j)oin, (d)isconnect, (wnse)walk")
        BufferedReader(InputStreamReader(System.`in`)).use {
            var te: TestEndpoint? = null
            var line = it.readLine()
            do {
                when {
                    line == "q" -> {
                        te?.disconnect()
                        println("bye!")
                        return
                    }
                    line == "d" -> {
                        state = State.OFFLINE
                        te?.disconnect()
                        log("disconnected")
                    }
                    line.startsWith("c") -> {
                        te = connect("$line ".split(" ")[1])
                        state = State.CONNECTED
                        log("connected")
                    }

                    line.startsWith("l") -> {
                        if (te == null)
                            log("error! connect first!")
                        else
                            login(te, mapper, line.split(" "))
                    }
                    line.startsWith("j") -> {
                        val params = line.split(" ")
                        val msg = if (params.size > 1)
                            JoinMessage(characters?.get(params[1].toInt())!!)
                        else
                            JoinMessage(characters?.first()!!)
                        te?.sendMessage(mapper.writeValueAsString(msg))
                    }
                    line in (setOf("w", "n", "e", "s")) -> {
                        te?.sendMessage(mapper.writeValueAsString(MoveAction(line)))
                    }
                }
                line = it.readLine()
            } while (line != null)
        }
    }

    private fun log(str: String) {
        println(str)
        print("[$state]>")
    }

    private fun login(te: TestEndpoint, mapper: ObjectMapper, params: List<String>) {
        val msg =
                if (params.size < 3)
                    LoginMessage("test", "test")
                else
                    LoginMessage(params[1], params[2])
        te.sendMessage(mapper.writeValueAsString(msg))
    }

    private fun connect(input: String): TestEndpoint {
        val url = if (input.isBlank())
            "ws://localhost:8080/action.json"
        else input
        return TestEndpoint(URI(url)).apply {
            addMessageHandler(TestHandler())
        }
    }

    private fun updateBoard() {
        objects.values.forEach {
            if (it.x < minx) {
                minx = it.x
            }
            if (it.x > maxx) {
                maxx = it.x
            }

            if (it.y < miny) {
                miny = it.x
            }
            if (it.y > maxy) {
                maxy = it.y
            }
        }
        board = Array(maxy - miny + 1, {
            Array(maxx - minx + 1, { ' ' })
        })
        objects.values.sortedBy {
            when (it.object_type) {
                "TILE" -> -1
                "CREATURE" -> 1
                else -> 0
            }
        }.forEach {
            val ch = when (it.object_type) {
                "TILE" -> '.'
                "CREATURE" -> '@'
                else -> ' '
            }
            board[it.y][it.x] = ch
        }
    }

    private fun drawBoard() {
        board.forEach {
            it.forEach { print(it) }
            println()
        }
    }

    fun handleUpdateMessage(update: Message) {
        when (update) {
            is AppearData -> {
                objects[update.id] = update
            }
            is DisappearData -> {
                objects.remove(update.id)
            }
            is StateChangeData -> {
                when (update.field) {
                    "x" -> objects[update.id]?.x = update.new_value.toInt()
                    "y" -> objects[update.id]?.y = update.new_value.toInt()
                }
            }
        }
    }

    internal class TestHandler : TestEndpoint.MessageHandler {
        private var mapper = ObjectMapper()

        override fun handleMessage(message: String) {
            try {
                val msg = mapper.readValue(message, Message::class.java)
                when (msg) {
                    is LoggedInMessage -> {
                        state = State.LOGGED_IN
                        log("Logged in! Chars: " + msg.characters)
                        characters = msg.characters

                    }
                    is JoinedMessage -> {
                        state = State.IN_GAME
                        log("Joined: $msg")
                        myId = msg.id
                    }
                    is UpdateMessage -> {
                        msg.updates.forEach {
                            handleUpdateMessage(it)
                        }
                        print("${ESC}c")
                        updateBoard()
                        drawBoard()
                        log("command:")
                    }
                    else -> log("msg = $msg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

fun main(args: Array<String>) {
    TestClient.main(args)
}