package org.demoth.aworlds.server2.api.messaging.fromServer

import com.fasterxml.jackson.annotation.JsonProperty
import org.demoth.aworlds.server2.api.messaging.Message

import java.util.Objects

class AppearData(
        @JsonProperty("object_type") var objectType: String,
        var id: String,
        override var x: Long,
        override var y: Long) : Message(), Positioned {

    override fun toString(): String {
        return "AppearData{" +
                "objectType='" + objectType + '\''.toString() +
                ", id='" + id + '\''.toString() +
                ", x=" + x +
                ", y=" + y +
                '}'.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AppearData?
        return x == that!!.x &&
                y == that.y &&
                objectType == that.objectType &&
                id == that.id
    }

    override fun hashCode(): Int {

        return Objects.hash(objectType, id, x, y)
    }
}
