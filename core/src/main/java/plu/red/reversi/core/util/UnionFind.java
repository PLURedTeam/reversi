package plu.red.reversi.core.util;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * This is a disjoint set data structure which represents multiple non-overlapping sets which will be
 * searched and merged regularly. This implementation uses both ranking and path compression.
 *
 * This implementation uses internal handles to keep track of tree nodes and allow the outside program to
 * have no knowledge of the internal structure used to create and represent the disjoint forest. This prevents
 * the caller from needing to track handles as things are added.
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
     * Constructs a copy of the other UnionFind set of disjoint sets. It will not perform a deep copy of objects it
     * stores, only its internal structure. This means a copy of can be modified without influencing the original.
     * This does not grantee the representatives will be the same as the original set.
     * @param other UnionFind object to copy.
     */
    public UnionFind(UnionFind<T> other) {
        //construct empty hashmap of correct size
        this((int)(other.size()*1.3));

        //Copy over all of the nodes
        for(UnionFindNode<T> i : other.members.values())
            add(i.value);

        //Union the nodes into the same state as before; use other's rep first so that it becomes the rep again
        for(UnionFindNode<T> i : other.members.values())
            union(i.value, other.getRep(i.value));
    }


    /**
     * Performs the union of two disjoint-sets. If both x and y are members of the same disjoint set,
     * no action will be performed. Otherwise, both x and y will end up with the same representative.
     * Will do nothing if either x or y are not in the forest.
     * @param x First value in the forest.
     * @param y Second value in the forest.
     * @return True if the union was successful, false if no action occurred.
     */
    public boolean union(T x, T y) throws InvalidParameterException {
        UnionFindNode<T> a = members.get(x);
        UnionFindNode<T> b = members.get(y);
        if(a == null || b == null) return false;
        return link(getRep(a), getRep(b));
    }


    public boolean inSameSet(T x, T y) {
        return getRep(x) == getRep(y);
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
     * Retrieves the entire disjoint set containing v. Note that this runs O(n). Will return null
     * if the input is not contained in the set.
     * @param v The value find the set of.
     * @return The entire disjoint set containing v, or null if v is not within any set.
     */
    public Set<T> getSet(T v) {
        if(v == null) return null;
        UnionFindNode<T> x = members.get(v);
        if(x == null) return null;

        //Get rep so we know if something else is in the same set
        x = getRep(x);

        //discover the disjoint set
        HashSet<T> collection = new HashSet<T>();
        for(UnionFindNode<T> i : members.values())
            if(getRep(i) == x) collection.add(i.value);

        return collection;
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
     * Add all items in the collection as their own individual sets.
     * @param collection Collection of values to add.
     * @return True if any modifications were made, otherwise false.
     */
    public boolean addAll(Collection<? extends T> collection) {
        boolean changed = false;
        for(T i : collection)
            changed = add(i) || changed;

        return true;
    }


    /**
     * Adds a new set containing toAdd to the superset and unions it with toUnion. If toAdd already exists this
     * will perform no action, and if toUnion does not exist in the superset, toAdd will be in its own set.
     * @param toAdd The value to add.
     * @param toUnion A member of the set to union with toAdd.
     * @return True if the add was successful, false if the element was already in the superset.
     */
    public boolean addAndUnion(T toAdd, T toUnion) {
        if(!add(toAdd)) return false;
        union(toAdd, toUnion);
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
        //create list of things to remove and then remove them
        Collection<T> toRemove = getSet(v);
        if(toRemove == null) return false;

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
            //update the parent, and then set return value to representative
            x = x.parent = getRep(x.parent);

        return x;
    }


    /**
     * Links two disjoint-sets together. Expects that both x and y are the representatives of their set.
     * If x and y are the same, it will perform no action.
     * @param x A root node in the forest.
     * @param y A root node in the forest.
     * @return True if successful, false if no action occurred.
     */
    private boolean link(UnionFindNode<T> x, UnionFindNode<T> y) {
        if(x == null || y == null || x == y) return false;
        if(x.rank > y.rank)
            y.parent = x;
        else {
            x.parent = y;
            if(x.rank == y.rank)
                y.rank++;
        }
        disjointSets--;
        return true;
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
