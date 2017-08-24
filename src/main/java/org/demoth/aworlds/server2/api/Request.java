package org.demoth.aworlds.server2.api;

public class Request {
    public RequestType type;
    public String[] params;

    public Request() {
    }

    public Request(RequestType type, String[] params) {
        this.type = type;
        this.params = params;
    }
}
