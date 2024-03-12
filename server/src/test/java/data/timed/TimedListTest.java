package data.timed;

import cat.michal.catbase.server.data.TimedList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TimedListTest {

    TimedList<String> list;
    MockTimeSource timeSource;
    final long TIMEOUT = 100;

    @BeforeEach
    void setup() {
        timeSource = new MockTimeSource();
        timeSource.setTime(0);
        list = new TimedList<String>().setTimeSource(timeSource).setTimeout(TIMEOUT);
        list.add("Test1");
        list.add("Test2");
        list.add("Test3");
    }

    @Test
    public void testContains() {
        assertTrue(list.contains("Test1"));
        assertFalse(list.contains("Test4"));
    }

    @Test
    public void testIterator() {
        Iterator<String> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("Test1", iterator.next());
        iterator.remove();
        assertFalse(list.contains("Test1"));
        assertEquals("Test2", iterator.next());
    }

    @Test
    public void testAdd() {
        list.add("Test4");
        assertTrue(list.contains("Test4"));
    }

    @Test
    public void testAddAtIndex() {
        list.add(1, "Test4");
        assertEquals("Test4", list.get(1));
    }

    @Test
    public void testRemoveObject() {
        list.remove("Test1");
        assertFalse(list.contains("Test1"));
    }

    @Test
    public void testRemoveIf() {
        list.removeIf(s -> s.startsWith("Test"));
        assertTrue(list.isEmpty());
    }

    @Test
    public void testContainsAll() {
        List<String> sublist = new ArrayList<>();
        sublist.add("Test1");
        sublist.add("Test2");
        assertTrue(list.containsAll(sublist));
    }

    @Test
    public void testAddAll() {
        List<String> additionalList = new ArrayList<>();
        additionalList.add("Test4");
        additionalList.add("Test5");
        list.addAll(additionalList);
        assertTrue(list.containsAll(additionalList));
    }

    @Test
    public void testAddAllAtIndex() {
        List<String> additionalList = new ArrayList<>();
        additionalList.add("Test4");
        additionalList.add("Test5");
        list.addAll(1, additionalList);
        assertEquals("Test4", list.get(1));
        assertEquals("Test5", list.get(2));
    }

    @Test
    public void testRemoveAll() {
        List<String> sublist = new ArrayList<>();
        sublist.add("Test1");
        sublist.add("Test2");
        list.removeAll(sublist);
        assertFalse(list.contains("Test1"));
        assertFalse(list.contains("Test2"));
    }

    @Test
    public void testRetainAll() {
        List<String> sublist = new ArrayList<>();
        sublist.add("Test1");
        sublist.add("Test2");
        list.retainAll(sublist);
        assertTrue(list.contains("Test1"));
        assertTrue(list.contains("Test2"));
        assertFalse(list.contains("Test3"));
    }

    @Test
    public void testClear() {
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGet() {
        assertEquals("Test1", list.get(0));
    }

    @Test
    public void testSet() {
        list.set(0, "Test4");
        assertEquals("Test4", list.get(0));
    }

    @Test
    public void testRemoveAtIndex() {
        list.remove(0);
        assertFalse(list.contains("Test1"));
    }

    @Test
    public void testIndexOf() {
        assertEquals(0, list.indexOf("Test1"));
    }

    @Test
    public void testLastIndexOf() {
        list.add("Test1");
        assertEquals(3, list.lastIndexOf("Test1"));
    }

    /*
     * In-house feature testing
     */

    @Test
    public void testFindAndRemove() {
        assertNull(list.findAndRemove(e -> e.equals("Test4")));
        assertEquals(3, list.size());

        assertEquals("Test2", list.findAndRemove(e -> e.equals("Test2")));
        assertEquals(2, list.size());
    }

    @Test
    public void testTimeoutFindAndRemove() {
        assertNull(list.findAndRemove(e -> e.equals("Test4")));
        assertEquals(3, list.size());

        timeSource.setTime(TIMEOUT + 1);

        assertNull(list.findAndRemove(e -> e.equals("Test2")));
        // size returns the size of the backing list
        // the find and remove call does not remove stale records
        assertEquals(3, list.size());
    }

    @Test
    public void testRemoveStale() {
        timeSource.setTime(TIMEOUT - 1);
        list.add("Test4");

        timeSource.setTime(TIMEOUT + 1);
        list.removeStale();

        assertEquals(1, list.size());
        assertEquals("Test4", list.get(0));
    }

    @Test
    public void testGetTimeout() {
        timeSource.setTime(TIMEOUT - 1);
        list.add("Test4");

        timeSource.setTime(TIMEOUT + 1);
        assertEquals(4, list.size());
        assertNull(list.get(0));
        assertNull(list.get(0));
        assertNull(list.get(0));
        assertEquals("Test4", list.get(0));
    }
}
