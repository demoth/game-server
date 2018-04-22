package org.demoth.aworlds.server2.api.messaging.fromClient

import com.fasterxml.jackson.annotation.JsonProperty
import org.demoth.aworlds.server2.api.messaging.Message

class JoinMessage(
        @JsonProperty("character_id") var characterId: String) : Message() {

    override fun toString(): String {
        return "JoinMessage{" +
                "characterId='" + characterId + '\''.toString() +
                '}'.toString()
    }
}
