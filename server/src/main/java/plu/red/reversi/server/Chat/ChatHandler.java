package plu.red.reversi.server.Chat;

import plu.red.reversi.core.util.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andrew on 3/25/2017.
 */
public class ChatHandler {

    private HashMap<String, ArrayList<ChatMessage>> messages;

    public ChatHandler() {
        messages = new HashMap<String, ArrayList<ChatMessage>>();
    }//chatHandler

    /**
     * Adds a new user to the chatHandler
     * @param username the username of the user
     */
    public void addUser(String username) {

        System.out.println("Adding user to chat handler");

        ArrayList<ChatMessage> blank = new ArrayList<ChatMessage>();
        messages.put(username, blank);
    }//addUser

    /**
     * Removes a user from the chatHandler
     * @param username the username of the user
     */
    public void removeUser(String username) {
        messages.remove(username);
    }//removeUser


    /**
     * Retrieves messages from the hash map
     * @param username the username of the player
     * @return An arraylist of chat messages
     */
    public ArrayList<ChatMessage> getMessages(String username) {
        ArrayList<ChatMessage> m = new ArrayList<ChatMessage>();
        ArrayList<ChatMessage> blank = new ArrayList<ChatMessage>();

        m = messages.get(username);
        messages.put(username, blank);

        return m;
    }//getMessages

    /**
     * Puts a messages into the HashMap
     * @param message
     */
    public void postMessage(ChatMessage message) {
        System.out.println("Adding a new message");

        for(String key: messages.keySet()) {
            if(!key.equals(message.username))
                messages.get(key).add(message);
        }//for
    }//postMessage
}//chatHandler
