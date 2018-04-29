package org.demoth.gameserver.api.messaging

interface Positioned {
    val x: Int
    val y: Int
}

data class AppearData(
        var object_type: String = "",
        var id: String = "",
        override var x: Int = 0,
        override var y: Int = 0) : Message(), Positioned

data class DisappearData(var id: String = "") : Message()

data class JoinedMessage(var id: String = "") : Message()

data class LoggedInMessage(var characters: List<String> = emptyList()) : Message()

data class StateChangeData(
        var id: String = "",
        var field: String = "",
        var new_value: String = "") : Message()

data class UpdateMessage(var updates: List<Message> = emptyList()) : Message()