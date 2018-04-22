package org.demoth.aworlds.server2.api.messaging

import com.fasterxml.jackson.annotation.JsonProperty

data class CommandMessage(var action: Message?) : Message()

data class JoinMessage(
        @JsonProperty("character_id") var characterId: String) : Message()

data class LoginMessage(var login: String, var password: String) : Message()

data class MoveAction(var direction: String?) : Message()