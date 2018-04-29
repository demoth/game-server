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

object TestClient {

    var state = State.OFFLINE
    var characters: List<String>? = null
    var myId: String? = null

    @JvmStatic
    fun main(args: Array<String>) {
        val mapper = ObjectMapper()
        log("Welcome to testclient! (q)uit, (c)onnect, (l)ogin, (j)oin, (d)isconnect")
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
                    LoginMessage("demoth", "cadaver")
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

    fun handleUpdateMessage(update: Message) {
        when (update) {
            is AppearData -> {

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
                    }
                    else -> log("msg = $msg")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
