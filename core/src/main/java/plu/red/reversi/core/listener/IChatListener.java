package plu.red.reversi.core.listener;

import plu.red.reversi.core.util.ChatMessage;

/**
 * Glory to the Red Team.
 *
 * Interface for when a chat message is received (usually from the server).
 */
public interface IChatListener extends IListener {

    /**
     * Called when a chat message has been received, usually from the server.
     *
     * @param message ChatMessage object that is received
     */
    void onChat(ChatMessage message);
}
