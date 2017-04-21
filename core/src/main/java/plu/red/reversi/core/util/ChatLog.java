package plu.red.reversi.core.util;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Glory to the Red Team.
 *
 * Object representing a chat log. The ChatLog class contains lists of previous ChatMessages. The ChatMessages are
 * organized by their String <code>channel</code>, and stored in separate ChannelLogs which can be retrieved individually.
 *
 * @see ChannelLog
 * @see ChatMessage
 */
public class ChatLog implements Iterable<ChatLog.ChannelLog> {

    private final HashMap<String, ChannelLog> channels = new HashMap<>();

    /**
     * ChannelLog Getter. Retrieves the ChannelLog associated with the given String <code>channel</code>.
     *
     * @param channel String <code>channel</code>
     * @return ChannelLog associated with the given <code>channel</code>, or <code>null</code> if no ChannelLog exists
     *
     * @see ChannelLog
     */
    public ChannelLog get(String channel) {
        return channels.get(channel);
    }

    /**
     * ChannelLog Creator. Creates a new ChannelLog associated with the given String <code>channel</code> and stores it.
     * If a previous ChannelLog already exists for the given <code>channel</code>, it is overwritten.
     *
     * @param channel String <code>channel</code>
     */
    public void create(String channel) {
        channels.put(channel, new ChannelLog(channel));
    }

    /**
     * Adds a ChatMessage to this ChatLog. Adds the given ChatMessage <code>message</code> to a ChannelLog that matches
     * the ChatMessage's <code>channel</code>, creating a new ChannelLog if necessary.
     *
     * @param message ChatMessage object to add
     *
     * @see ChatLog#get
     * @see ChatMessage
     */
    public void add(ChatMessage message) {
        if(!channels.containsKey(message.channel))
            channels.put(message.channel, new ChannelLog(message.channel));
        channels.get(message.channel).add(message);
    }

    /**
     * Offers a ChatMessage to this ChatLog. If this ChatLog has a ChannelLog associated with the given ChatMessage's
     * <code>channel</code>, adds the given ChatMessage to it.
     *
     * @param message ChatMessage object to offer
     */
    public void offer(ChatMessage message) {
        if(channels.containsKey(message.channel))
            channels.get(message.channel).add(message);
    }

    /**
     * Clears a ChannelLog. Erases the ChannelLog that is associated with the given String <code>channel</code>. If there
     * is no ChannelLog associated with the given <code>channel</code>, does nothing. To clear all channels, use no
     * parameters.
     *
     * @param channel String <code>channel</code>
     *
     * @see ChannelLog
     * @see ChatLog#clear()
     */
    public void clear(String channel) {
        channels.remove(channel);
    }

    /**
     * Clears the ChatLog. Erases all ChannelLogs stored in this ChatLog. To clear an individual channel, use a String
     * <code>channel</code> as a parameter.
     *
     * @see ChannelLog
     * @see ChatLog#clear(String)
     */
    public void clear() {
        channels.clear();
    }

    @Override public Iterator<ChannelLog> iterator() { return channels.values().iterator(); }
    @Override public void forEach(Consumer<? super ChannelLog> action) { channels.values().forEach(action); }
    @Override public Spliterator<ChannelLog> spliterator() { return channels.values().spliterator(); }

    /**
     * Object representing a chat log for a specific channel. The ChannelLog class is an inner class of ChatLog, and
     * is the actual storage class of ChatMessages. Many ChannelLogs can exist in one ChatLog; one for each unique
     * String <code>channel</code> of the stored ChatMessages. In addition, the ChannelLog class implements the Swing
     * ListModel interface so that it may be used directly as the model for Swing GUI elements that use a ListModel.
     *
     * @see ChatLog
     * @see ChatMessage
     * @see ListModel
     */
    public static class ChannelLog extends TreeSet<ChatMessage> implements ListModel<ChatMessage> {

        /**
         * String <code>channel</code> that this ChannelLog stores ChatMessages for.
         *
         * @see ChatMessage
         */
        public final String channel;

        /**
         * Constructor. Creates a new ChannelLog for the given String <code>channel</code>.
         *
         * @param channel String that represents this ChannelLog's channel
         */
        ChannelLog(String channel) {
            this.channel = channel;
        }

        /**
         * Returns the length of the list.
         *
         * @return the length of the list
         */
        @Override
        public int getSize() {
            return size();
        }

        /**
         * Returns the value at the specified index.
         *
         * @param index the requested index
         * @return the value at <code>index</code>
         */
        @Override
        public ChatMessage getElementAt(int index) {
            if (index < 0) throw new IndexOutOfBoundsException("Index must be positive");
            Iterator<ChatMessage> it = iterator();
            for (int i = 0; i < index; i++) {
                if (it.hasNext()) it.next();
                else throw new IndexOutOfBoundsException("No ChatMessage element at index '" + index + "'");
            }
            if (it.hasNext()) return it.next();
            else throw new IndexOutOfBoundsException("No ChatMessage element at index '" + index + "'");
        }

        /**
         * Returns the index that the specified value is at.
         *
         * @param element the requested value
         * @return the index that <code>element</code> is at
         */
        public int getElementIndex(ChatMessage element) {
            Iterator<ChatMessage> it = iterator();
            int i = 0;
            while (it.hasNext()) {
                ChatMessage n = it.next();
                if (element == null && n == null) return i;
                else if (element != null && element.equals(n)) return i;
                i++;
            }
            return -1;
        }

        // Internal Listener Set
        HashSet<ListDataListener> listenerSetListData = new HashSet<>();

        /**
         * Adds a listener to the list that's notified each time a change
         * to the data model occurs.
         *
         * @param l the <code>ListDataListener</code> to be added
         */
        @Override
        public void addListDataListener(ListDataListener l) {
            listenerSetListData.add(l);
        }

        /**
         * Removes a listener from the list that's notified each time a
         * change to the data model occurs.
         *
         * @param l the <code>ListDataListener</code> to be removed
         */
        @Override
        public void removeListDataListener(ListDataListener l) {
            listenerSetListData.remove(l);
        }

        protected final void notifyListeners(int type, int i0, int i1) {
            ListDataEvent event = new ListDataEvent(this, type, i0, i1);
            switch (type) {
                case ListDataEvent.CONTENTS_CHANGED:
                    for (ListDataListener listener : listenerSetListData) listener.contentsChanged(event);
                    break;
                case ListDataEvent.INTERVAL_ADDED:
                    for (ListDataListener listener : listenerSetListData) listener.intervalAdded(event);
                    break;
                case ListDataEvent.INTERVAL_REMOVED:
                    for (ListDataListener listener : listenerSetListData) listener.intervalRemoved(event);
                    break;
                default:
                    break;
            }
        }

        @Override
        public boolean add(ChatMessage element) {
            if (super.add(element)) {
                int i = getElementIndex(element);
                notifyListeners(ListDataEvent.INTERVAL_ADDED, i, i);
                return true;
            } else return false;
        }

        @Override
        public boolean addAll(Collection<? extends ChatMessage> elements) {
            if (super.addAll(elements)) {
                // No way to know what sequence is added, as they are then sorted
                notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, size() - 1);
                return true;
            } else return false;
        }

        @Override
        public void clear() {
            int i = size() - 1;
            super.clear();
            notifyListeners(ListDataEvent.INTERVAL_REMOVED, 0, i);
        }

        @Override
        public ChatMessage pollFirst() {
            ChatMessage e = super.pollFirst();
            notifyListeners(ListDataEvent.INTERVAL_REMOVED, 0, 0);
            return e;
        }

        @Override
        public ChatMessage pollLast() {
            ChatMessage e = super.pollFirst();
            notifyListeners(ListDataEvent.INTERVAL_REMOVED, size(), size());
            return e;
        }

        @Override
        public boolean remove(Object element) {
            int i = size() - 1;
            if (super.remove(element)) {
                // Lazy notify
                notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, i);
                return true;
            } else return false;
        }

        @Override
        public boolean removeAll(Collection<?> elements) {
            int i = size() - 1;
            if (super.removeAll(elements)) {
                // Lazy notify
                notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, size() - 1);
                return true;
            } else return false;
        }

        @Override
        public boolean removeIf(Predicate<? super ChatMessage> filter) {
            int i = size() - 1;
            if (super.removeIf(filter)) {
                // Lazy notify
                notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, size() - 1);
                return true;
            } else return false;
        }

        @Override
        public boolean retainAll(Collection<?> elements) {
            int i = size() - 1;
            if (super.retainAll(elements)) {
                // Lazy notify
                notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, size() - 1);
                return true;
            } else return false;
        }
    }
}