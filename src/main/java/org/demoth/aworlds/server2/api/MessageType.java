package org.demoth.aworlds.server2.api;

public enum MessageType {
    // common
    TEXT, // message, recipient(optional)

    // from client
    LOGIN, // user, pass
    JOIN, // character_id

    COMMAND, // character_id, type, params

    // from server
    LOGGED_IN, // characters
    ERROR, // error_message
    JOINED, // terrain_and_objects_around_the_character
    UPDATE, // game update
}
