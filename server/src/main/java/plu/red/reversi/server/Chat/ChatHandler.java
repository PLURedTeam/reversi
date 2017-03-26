package plu.red.reversi.server.Chat;

import plu.red.reversi.core.util.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Andrew on 3/25/2017.
 */
public class ChatHandler implements Runnable {

    private HashMap<Long, ChatMessage> messages;

    public ChatHandler() {
        messages = new HashMap<Long, ChatMessage>();
    }//chatHandler

    /**
     * Iterates through the Hashmap removing messages older than five seconds
     */
    public void run() {

        while(true) {
            for(Long time: messages.keySet()) {
                if(time > System.currentTimeMillis() + 2000)
                    messages.remove(time);
            }//for
            try { Thread.sleep(1000);}
            catch (InterruptedException e) { e.printStackTrace();}
        }//true
    }//run

    /**
     * Retrieves messages from the hash map
     * @param username the username of the player
     * @return An arraylist of chat messages
     */
    public ArrayList<ChatMessage> getMessages(String username) {
        ArrayList<ChatMessage> m = new ArrayList<ChatMessage>();

        for(Long key: messages.keySet()) {
            m.add(messages.get(key));
        }//for

        return m;
    }//getMessages

    /**
     * Puts a messages into the HashMap
     * @param message
     */
    public void postMessage(ChatMessage message) {
        messages.put(System.currentTimeMillis(),message);
    }//postMessage




}//chatHandler
