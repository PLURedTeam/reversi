package plu.red.reversi.core.util;


import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Predicate;

public class ChatLog extends TreeSet<ChatMessage> implements ListModel<ChatMessage> {

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
        if(index < 0) throw new IndexOutOfBoundsException("Index must be positive");
        Iterator<ChatMessage> it = iterator();
        for(int i = 0; i < index; i++) {
            if(it.hasNext()) it.next();
            else throw new IndexOutOfBoundsException("No ChatMessage element at index '" + index + "'");
        }
        if(it.hasNext()) return it.next();
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
        while(it.hasNext()) {
            ChatMessage n = it.next();
            if(element == null && n == null) return i;
            else if(element != null && element.equals(n)) return i;
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
        switch(type) {
            case ListDataEvent.CONTENTS_CHANGED:
                for(ListDataListener listener : listenerSetListData) listener.contentsChanged(event);
                break;
            case ListDataEvent.INTERVAL_ADDED:
                for(ListDataListener listener : listenerSetListData) listener.intervalAdded(event);
                break;
            case ListDataEvent.INTERVAL_REMOVED:
                for(ListDataListener listener : listenerSetListData) listener.intervalRemoved(event);
                break;
            default:
                break;
        }
    }

    @Override public boolean add(ChatMessage element) {
        if(super.add(element)) {
            int i = getElementIndex(element);
            notifyListeners(ListDataEvent.INTERVAL_ADDED, i, i);
            return true;
        } else return false;
    }

    @Override public boolean addAll(Collection<? extends ChatMessage> elements) {
        if(super.addAll(elements)) {
            // No way to know what sequence is added, as they are then sorted
            notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, size()-1);
            return true;
        } else return false;
    }

    @Override public void clear() {
        int i = size()-1;
        super.clear();
        notifyListeners(ListDataEvent.INTERVAL_REMOVED, 0, i);
    }

    @Override public ChatMessage pollFirst() {
        ChatMessage e = super.pollFirst();
        notifyListeners(ListDataEvent.INTERVAL_REMOVED, 0, 0);
        return e;
    }

    @Override public ChatMessage pollLast() {
        ChatMessage e = super.pollFirst();
        notifyListeners(ListDataEvent.INTERVAL_REMOVED, size(), size());
        return e;
    }

    @Override public boolean remove(Object element) {
        int i = size()-1;
        if(super.remove(element)) {
            // Lazy notify
            notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, i);
            return true;
        } else return false;
    }

    @Override public boolean removeAll(Collection<?> elements) {
        int i = size()-1;
        if(super.removeAll(elements)) {
            // Lazy notify
            notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, size()-1);
            return true;
        } else return false;
    }

    @Override public boolean removeIf(Predicate<? super ChatMessage> filter) {
        int i = size()-1;
        if(super.removeIf(filter)) {
            // Lazy notify
            notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, size()-1);
            return true;
        } else return false;
    }

    @Override public boolean retainAll(Collection<?> elements) {
        int i = size()-1;
        if(super.retainAll(elements)) {
            // Lazy notify
            notifyListeners(ListDataEvent.CONTENTS_CHANGED, 0, size()-1);
            return true;
        } else return false;
    }


}
