package org.demoth.aworlds.server2.api.messaging.fromClient

import org.demoth.aworlds.server2.api.messaging.Message

class CommandMessage : Message() {
    var action: Message? = null

    override fun toString(): String {
        return "CommandMessage{" +
                "action=" + action +
                '}'.toString()
    }
}
