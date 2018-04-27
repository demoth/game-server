package org.demoth.gameserver.api.messaging

import com.fasterxml.jackson.annotation.JsonProperty

interface Positioned {
    val x: Long
    val y: Long
}

data class AppearData(
        @JsonProperty("object_type") var objectType: String,
        var id: String,
        override var x: Long,
        override var y: Long) : Message(), Positioned

data class DisappearData(var id: String) : Message()

data class JoinedMessage(var x: Long?, var y: Long?) : Message()

data class LoggedInMessage(var characters: Collection<String>) : Message()

data class StateChangeData(
        var id: String,
        var field: String,
        @JsonProperty("new_value") var newValue: String) : Message()

data class UpdateMessage(var updates: List<Message>) : Message()