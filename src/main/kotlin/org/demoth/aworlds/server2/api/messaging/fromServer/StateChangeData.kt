package org.demoth.aworlds.server2.api.messaging.fromServer

import com.fasterxml.jackson.annotation.JsonProperty
import org.demoth.aworlds.server2.api.messaging.Message

class StateChangeData(
        var id: String,
        var field: String,
        @JsonProperty("new_value") var newValue: String) : Message() {

    override fun toString(): String {
        return "StateChangeData{" +
                "id='" + id + '\''.toString() +
                ", field='" + field + '\''.toString() +
                ", newValue='" + newValue + '\''.toString() +
                '}'.toString()
    }
}
