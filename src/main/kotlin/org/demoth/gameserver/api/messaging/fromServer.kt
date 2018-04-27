package org.demoth.gameserver.api.messaging

import com.fasterxml.jackson.annotation.JsonProperty

interface Positioned {
    val x: Int
    val y: Int
}

data class AppearData(
        @JsonProperty("object_type") var objectType: String,
        var id: String,
        override var x: Int,
        override var y: Int) : Message(), Positioned

data class DisappearData(var id: String) : Message()

data class JoinedMessage(var x: Int, var y: Int) : Message()

data class LoggedInMessage(var characters: Collection<String>) : Message()

data class StateChangeData(
        var id: String,
        var field: String,
        @JsonProperty("new_value") var newValue: String) : Message()

data class UpdateMessage(var updates: List<Message>) : Message()