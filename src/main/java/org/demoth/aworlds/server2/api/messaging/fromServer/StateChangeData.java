package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.Message;

public class StateChangeData extends Message {
    public String id;
    public String field;
    public String newValue;

    public StateChangeData() {
    }

    public StateChangeData(String id, String field, String newValue) {
        this.id = id;
        this.field = field;
        this.newValue = newValue;
    }
}
