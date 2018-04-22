package org.demoth.aworlds.server2.api.messaging.fromClient

import org.demoth.aworlds.server2.api.messaging.Message

class MoveAction : Message() {
    var direction: String? = null

    override fun toString(): String {
        return "MoveAction{" +
                "direction='" + direction + '\''.toString() +
                '}'.toString()
    }
}
