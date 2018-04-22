package org.demoth.aworlds.server2.api.messaging.fromClient

import org.demoth.aworlds.server2.api.messaging.Message

class LoginMessage(var login: String, var password: String) : Message() {

    override fun toString(): String {
        return "LoginMessage{" +
                "login='" + login + '\''.toString() +
                ", password='" + password + '\''.toString() +
                '}'.toString()
    }
}
