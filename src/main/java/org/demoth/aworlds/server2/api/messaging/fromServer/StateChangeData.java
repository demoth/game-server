package org.demoth.aworlds.server2.api.messaging.fromServer;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.demoth.aworlds.server2.api.messaging.Message;

public class StateChangeData extends Message {
    public String id;
    public String field;
    @JsonProperty("new_value")
    public String newValue;

    public StateChangeData() {
    }

    public StateChangeData(String id, String field, String newValue) {
        this.id = id;
        this.field = field;
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "StateChangeData{" +
                "id='" + id + '\'' +
                ", field='" + field + '\'' +
                ", newValue='" + newValue + '\'' +
                '}';
    }
}
