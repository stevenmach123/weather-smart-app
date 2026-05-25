package edu.uiuc.cs427app.data.database.entities;

import java.util.List;

/**
 * Represents a chat item containing a name, options, message, and selection state.
 */
public class ChatItem {
    public List<String> options;
    public String name;
    public String message;
    public boolean isSelected;

    /**
     * Constructs a ChatItem with the specified name, options, and message.
     *
     * @param name     the name associated with the chat item
     * @param options  the list of selectable options
     * @param message  the message content
     */
    public ChatItem(String name, List<String> options, String message) {
        this.options = options;
        this.name = name;
        this.message = message;
        this.isSelected = false;
    }

}
