package org.demoth.aworlds.server2

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class ServerMain

fun main(args: Array<String>) {
    SpringApplication.run(ServerMain::class.java, *args)
}

