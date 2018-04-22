package org.demoth.aworlds.server2.api.messaging.fromServer

import org.demoth.aworlds.server2.api.messaging.Message

class UpdateMessage(var updates: List<Message>) : Message() {

    override fun toString(): String {
        return "UpdateMessage{" +
                "updates=" + updates +
                '}'.toString()
    }
}
