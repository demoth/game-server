package org.demoth.aworlds.server2.api.messaging.fromServer;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.demoth.aworlds.server2.api.messaging.Message;

import java.util.Objects;

public class AppearData extends Message implements Positioned {
    @JsonProperty("object_type")
    public String objectType;
    public String id;
    public long x;
    public long y;

    public AppearData() {
    }

    public AppearData(String objectType, String id, long x, long y) {
        this.objectType = objectType;
        this.id = id;
        this.x = x;
        this.y = y;
    }

    @Override
    public long getX() {
        return x;
    }

    @Override
    public long getY() {
        return y;
    }

    @Override
    public String toString() {
        return "AppearData{" +
                "objectType='" + objectType + '\'' +
                ", id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppearData that = (AppearData) o;
        return x == that.x &&
                y == that.y &&
                Objects.equals(objectType, that.objectType) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(objectType, id, x, y);
    }
}
