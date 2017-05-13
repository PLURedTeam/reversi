package plu.red.reversi.core.util;

public interface ChatLog {

    /**
     * ChannelLog Creator. Creates a new ChannelLog associated with the given String <code>channel</code> and stores it.
     * If a previous ChannelLog already exists for the given <code>channel</code>, it is overwritten.
     *
     * @param channel String <code>channel</code>
     */
    void create(String channel);

    /**
     * Adds a ChatMessage to this ChatLog. Adds the given ChatMessage <code>message</code> to a ChannelLog that matches
     * the ChatMessage's <code>channel</code>, creating a new ChannelLog if necessary.
     *
     * @param message ChatMessage object to add
     *
     * @see ChatMessage
     */
    void add(ChatMessage message);

    /**
     * Offers a ChatMessage to this ChatLog. If this ChatLog has a ChannelLog associated with the given ChatMessage's
     * <code>channel</code>, adds the given ChatMessage to it.
     *
     * @param message ChatMessage object to offer
     */
    void offer(ChatMessage message);

    /**
     * Clears a ChannelLog. Erases the ChannelLog that is associated with the given String <code>channel</code>. If there
     * is no ChannelLog associated with the given <code>channel</code>, does nothing. To clear all channels, use no
     * parameters.
     *
     * @param channel String <code>channel</code>
     *
     * @see ChatLog#clear()
     */
    void clear(String channel);

    /**
     * Clears the ChatLog. Erases all ChannelLogs stored in this ChatLog. To clear an individual channel, use a String
     * <code>channel</code> as a parameter.
     *
     * @see ChatLog#clear(String)
     */
    void clear();

    class NullChatLog implements ChatLog {

        @Override
        public void create(String channel) {

        }

        @Override
        public void add(ChatMessage message) {

        }

        @Override
        public void offer(ChatMessage message) {

        }

        @Override
        public void clear(String channel) {

        }

        @Override
        public void clear() {

        }
    }
}