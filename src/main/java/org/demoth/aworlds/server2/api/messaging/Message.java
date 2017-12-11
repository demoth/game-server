package org.demoth.aworlds.server2.api.messaging;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public abstract class Message {
}
