package org.demoth.aworlds.server2.api.messaging.fromServer

import org.demoth.aworlds.server2.api.messaging.Message

class JoinedMessage : Message {
    var x: Long? = null
    var y: Long? = null

    constructor() {}

    constructor(x: Long?, y: Long?) {
        this.x = x
        this.y = y
    }

    override fun toString(): String {
        return "JoinedMessage{" +
                "x=" + x +
                ", y=" + y +
                '}'.toString()
    }
}
