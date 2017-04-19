package plu.red.reversi.core.util;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * This is a disjoint set data structure which represents multiple non-overlapping sets which will be
 * searched and merged regularly. This implementation uses both ranking and path compression.
 *
 * It keeps track of tree nodes internally to prevent the calling code from having to keep references.
 *
 * Implementation based on Disjoint-Set Forest pseudocode provided by Introduction to Algorithms Third ed.,
 * T. Cormen, C. Leiserson, R. Rivest, C. Stein. 2009 MIT Press, Cambridge, MA, USA.
 *
 * @author Matthew Conover
 */
public class UnionFind<T> extends AbstractSet<T> {
    /// Number of different non-overlapping sets.
    private int disjointSets = 0;

    /// Used for reference lookup of the nodes in the forest.
    HashMap< T, UnionFindNode<T> > members;


    /**
     * Constructs an empty UnionFind with the default initial capacity and load factor.
     */
    public UnionFind() {
        members = new HashMap<>();
    }


    /**
     * Constructs an empty union find with the initial capacity.
     * @param initialCapacity The initial capacity.
     */
    public UnionFind(int initialCapacity) {
        members = new HashMap<>(initialCapacity);
    }


    /**
     * Constructs an empty union find with the initial capacity and load factor.
     * @param initialCapacity The initial capacity.
     * @param loadFactor The load factor.
     */
    public UnionFind(int initialCapacity, float loadFactor) {
        members = new HashMap<>(initialCapacity, loadFactor);
    }


    /**
     * Performs the union of two disjoint-sets. If both x and y are members of the same disjoint set,
     * no action will be performed. Otherwise, both x and y will end up with the same representative.
     * Will do nothing if either x or y are not in the forest.
     * @param x First value in the forest.
     * @param y Second value in the forest.
     */
    public void union(T x, T y) throws InvalidParameterException {
        UnionFindNode<T> a = members.get(x);
        UnionFindNode<T> b = members.get(x);
        if(a == null || b == null) return;
        link(getRep(a), getRep(b));
    }


    /**
     * Finds the set representative of the given value.
     * @param x A value in the forest.
     * @return The set representative of x, or null if x is not in the forest.
     */
    public T getRep(T x) {
        UnionFindNode<T> t = members.get(x);
        if(t == null) return null;
        return getRep(t).value;
    }


    /**
     * Gets the number of unique sets that exist in this superset.
     * @return Number of disjoint sets.
     */
    public int disjointSets() {
        return disjointSets;
    }


    /**
     * Gets the total number of items in this set.
     * @return Total number of items in this set.
     */
    @Override
    public int size() {
        return members.size();
    }


    /**
     * Check if o is contained within one of the sets.
     * @param o Object to check for the existence of.
     * @return True if it was founds, else false.
     */
    @Override
    public boolean contains(Object o) {
        return members.get(o) != null;
    }


    /**
     * Retrieves an iterator to the front of the set. The iterator will go through all items and ignore
     * disjointed set boundaries.
     * @return An iterator which will iterate all sets.
     */
    @Override
    public Iterator<T> iterator() {
        return new UnionFindIterator<>();
    }


    /**
     * Adds a new set containing x to the superset. If the value already exists, it will perform no action.
     * @param x Value to add.
     * @return True if the value was added, false if the value already existed in the forest.
     */
    @Override
    public boolean add(T x) {
        if(x == null) return false;
        if(members.containsKey(x)) return false;
        members.put(x, new UnionFindNode<T>(x));
        disjointSets++;
        return true;
    }


    /**
     * Adds a new set containing x to the superset and unions it with y. If x already exists this will perform
     * no action, and if y does not exist in the superset, x will be in its own set.
     * @param x The value to add.
     * @param y A member of the set to union with x.
     * @return True if the add was successful, false if the element was already in the superset.
     */
    public boolean addAndUnion(T x, T y) {
        if(!add(x)) return false;
        union(x, y);
        return true;
    }


    /**
     * Remove o from the set it resides in.
     * Note this has an O(n) efficiency.
     * @param o Member of a set that is to be removed.
     * @return True if it was successfully removed. False if it is not in any set.
     */
    @Override
    public boolean remove(Object o) {
        UnionFindNode<T> removed = members.remove(o);
        if(removed == null) return false;

        UnionFindNode<T> root = getRep(removed);

        LinkedList<UnionFindNode<T> > set = new LinkedList<>();
        for(UnionFindNode<T> i : members.values())
            if(getRep(i) == root) set.add(i);

        //it was a singleton
        if(root == removed && set.isEmpty()) {
            disjointSets--;
            return true;
        }

        //it has more...
        if(root == removed) //update root if needed
            root = set.getFirst();

        //update all of their parents
        for(UnionFindNode<T> i : set)
            i.parent = root;

        return true;
    }


    /**
     * Remove the entire disjoint set that v is part of.
     * Note this has an O(n) efficiency.
     * @param v A member of the set to be removed.
     * @return True if it removes the set, false if v is not in any set.
     */
    public boolean removeSet(T v) {
        if(v == null) return false;
        UnionFindNode<T> x = members.get(v);
        if(x == null) return false;

        //Get rep so we know if something else is in the same set
        x = getRep(x);

        //create list of things to remove and then remove them
        LinkedList<T> toRemove = new LinkedList<>();
        for(UnionFindNode<T> i : members.values())
            if(getRep(i) == x) toRemove.add(i.value);
        for(T i : toRemove)
            members.remove(i);

        disjointSets--;
        return true;
    }


    /**
     * Remove all values from the superset. This effectively wipes everything.
     */
    @Override
    public void clear() {
        members.clear();
        disjointSets = 0;
    }


    /**
     * Finds the set representative for x.
     * @param x Value to getRep the set representative of.
     * @return Set representative of x.
     */
    private UnionFindNode<T> getRep(UnionFindNode<T> x) {
        if(x != x.parent)
            x.parent = getRep(x.parent);
        return x;
    }


    /**
     * Links two disjoint-sets together. Expects that both x and y are the representatives of their set.
     * If x and y are the same, it will perform no action.
     * @param x A root node in the forest.
     * @param y A root node in the forest.
     */
    private void link(UnionFindNode<T> x, UnionFindNode<T> y) {
        if(x == y) return; //check if references are same
        if(x.rank > y.rank)
            y.parent = x;
        else {
            x.parent = y;
            if(x.rank == y.rank)
                y.rank++;
        }
        disjointSets--;
    }



    /**
     * Wrapper which represents an individual in the forest.
     * @param <T> The type of value being wrapped.
     */
    private static class UnionFindNode<T> {
        public UnionFindNode<T> parent;
        public int rank;
        public T value;


        public UnionFindNode(T value) {
            parent = this;
            rank = 0;
            this.value = value;
        }
    }


    /**
     * A crude iterator which gets the job done. This cannot be used for the removal of elements.
     * @param <T> The type contained within the superset.
     */
    private class UnionFindIterator<T> implements Iterator<T> {
        Iterator<T> itr;

        private UnionFindIterator() {
            itr = (Iterator<T>)members.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public T next() {
            return itr.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
