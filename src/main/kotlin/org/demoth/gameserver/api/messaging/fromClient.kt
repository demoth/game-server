package org.demoth.gameserver.api.messaging

data class JoinMessage(var character_id: String = "") : Message()

data class LoginMessage(var login: String = "", var password: String = "") : Message()

data class CommandMessage(var action: Message? = null) : Message()

data class MoveAction(var direction: String = "") : Message()