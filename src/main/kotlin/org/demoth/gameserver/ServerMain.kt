package org.demoth.gameserver

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class ServerMain

fun main(args: Array<String>) {
    SpringApplication.run(ServerMain::class.java, *args)
}

