package plu.red.reversi.core;

import org.junit.Test;
import plu.red.reversi.core.util.UnionFind;

import java.util.LinkedList;
import java.util.Objects;

import static org.junit.Assert.*;

public class UnionFindTest {
    @Test
    public void testUnionFind() {
        UnionFind<Integer> uf = new UnionFind<>();
        assertEquals(0, uf.disjointSets());
    }

    @Test
    public void testAdd() {
        //add a couple things
        UnionFind<Integer> uf = new UnionFind<>();
        assertFalse(uf.contains(8));
        assertTrue(uf.add(8));
        assertTrue(uf.contains(8));
        assertEquals(1, uf.disjointSets());
        assertEquals(1, uf.size());

        assertFalse(uf.contains(1238));
        assertTrue(uf.add(1238));
        assertTrue(uf.contains(1238));
        assertEquals(2, uf.disjointSets());
        assertEquals(2, uf.size());

        //test adding a duplicate
        assertFalse(uf.add(8));
    }

    @Test
    public void testUnion() {
        UnionFind<Integer> uf = new UnionFind<>();
        uf.add(9); uf.add(2); uf.add(3);
        assertEquals(3, uf.disjointSets());
        assertEquals(3, uf.size());

        assertTrue(uf.union(3, 9));

        assertEquals(2, uf.disjointSets());
        assertEquals(3, uf.size());
        assertEquals(uf.getRep(3), uf.getRep(9));
        assertTrue(uf.inSameSet(3, 9));
        assertFalse(uf.inSameSet(2, 9));

        assertTrue(uf.addAndUnion(1, 2));
        assertEquals(2, uf.disjointSets());
        assertEquals(4, uf.size());

        assertEquals(uf.getRep(1), uf.getRep(2));
        assertNotEquals(uf.getRep(2), uf.getRep(9));
        assertNotEquals(uf.getRep(3), uf.getRep(1));

        assertTrue(uf.union(1, 9));
        assertEquals(4, uf.size());
        assertEquals(1, uf.disjointSets());
        assertEquals(uf.getRep(3), uf.getRep(1));
    }

    @Test
    public void testRemove() {
        UnionFind<Integer> uf = new UnionFind<>();
        uf.add(1); uf.add(2); uf.add(3); uf.add(4);
        uf.union(1,2);
        uf.union(2, 3);

        //remove independent
        assertEquals(4, uf.size());
        assertEquals(2, uf.disjointSets());
        assertTrue(uf.contains(4));
        assertTrue(uf.remove(4));
        assertEquals(3, uf.size());
        assertEquals(1, uf.disjointSets());
        assertFalse(uf.contains(4));

        //remove child
        assertTrue(uf.contains(3));
        assertTrue(uf.remove(3));
        assertFalse(uf.contains(3));
        assertTrue(uf.contains(1) && uf.contains(2));
        assertEquals(2, uf.size());

        //remove parent
        assertTrue(uf.remove(1));
        assertTrue(uf.contains(2));
        assertEquals(new Integer(2), uf.getRep(2));
        assertEquals(1, uf.size());
        assertEquals(1, uf.disjointSets());
    }
}
