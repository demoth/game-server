package org.demoth.gameserver.api.messaging

interface Positioned {
    val x: Int
    val y: Int
}

data class LoggedInMessage(var characters: List<String> = emptyList()) : Message()

data class JoinedMessage(var id: String = "") : Message()

abstract class Update : Message()
/**
 * list of appear/disappear/state change data
 */
data class UpdateMessage(var updates: List<Update> = emptyList()) : Message()

data class Movement(
        var id: String = "",
        var x: Int = 0,
        var y: Int = 0
) : Update()

data class StateChangeData(
        var id: String = "",
        var field: String = "",
        var new_value: String = "") : Update()

data class AppearData(
        var object_type: String = "",
        var id: String = "",
        override var x: Int = 0,
        override var y: Int = 0) : Update(), Positioned

data class DisappearData(var id: String = "") : Update()

