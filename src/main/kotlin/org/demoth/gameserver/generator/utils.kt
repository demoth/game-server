package org.demoth.gameserver.generator

import org.demoth.gameserver.api.ActorType.REGION
import org.demoth.gameserver.model.Location

const val alphabet = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM01234567890"
fun printlnLocation(location: Location) {
    location.actors.filter { it.type == REGION }.forEach {
        val i = Math.abs(it.id.hashCode()) % alphabet.length
        println("${it.id} = ${alphabet[i]}")

    }
    location.board.forEach { row ->
        row?.forEach { c ->
            if (c == null) {
                print(' ')
            } else {
                val region = c.actors.find { it.type == REGION }
                val i = Math.abs(region!!.id.hashCode()) % alphabet.length
                print(alphabet[i])
            }
        }
        println()
    }
}
