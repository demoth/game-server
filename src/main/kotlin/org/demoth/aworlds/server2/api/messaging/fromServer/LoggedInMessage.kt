package org.demoth.aworlds.server2.api.messaging.fromServer

import org.demoth.aworlds.server2.api.messaging.Message

class LoggedInMessage(var characters: Collection<String>) : Message() {

    override fun toString(): String {
        return "LoggedInMessage{" +
                "characters=" + characters +
                '}'.toString()
    }
}
