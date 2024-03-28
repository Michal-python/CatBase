package datatimed;

import cat.michal.catbase.common.data.TimedList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        Assertions.assertTrue(list.contains("Test1"));
        Assertions.assertFalse(list.contains("Test4"));
    }

    @Test
    public void testIterator() {
        Iterator<String> iterator = list.iterator();
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("Test1", iterator.next());
        iterator.remove();
        Assertions.assertFalse(list.contains("Test1"));
        Assertions.assertEquals("Test2", iterator.next());
    }

    @Test
    public void testAdd() {
        list.add("Test4");
        Assertions.assertTrue(list.contains("Test4"));
    }

    @Test
    public void testAddAtIndex() {
        list.add(1, "Test4");
        Assertions.assertEquals("Test4", list.get(1));
    }

    @Test
    public void testRemoveObject() {
        list.remove("Test1");
        Assertions.assertFalse(list.contains("Test1"));
    }

    @Test
    public void testRemoveIf() {
        list.removeIf(s -> s.startsWith("Test"));
        Assertions.assertTrue(list.isEmpty());
    }

    @Test
    public void testContainsAll() {
        List<String> sublist = new ArrayList<>();
        sublist.add("Test1");
        sublist.add("Test2");
        Assertions.assertTrue(list.containsAll(sublist));
    }

    @Test
    public void testAddAll() {
        List<String> additionalList = new ArrayList<>();
        additionalList.add("Test4");
        additionalList.add("Test5");
        list.addAll(additionalList);
        Assertions.assertTrue(list.containsAll(additionalList));
    }

    @Test
    public void testAddAllAtIndex() {
        List<String> additionalList = new ArrayList<>();
        additionalList.add("Test4");
        additionalList.add("Test5");
        list.addAll(1, additionalList);
        Assertions.assertEquals("Test4", list.get(1));
        Assertions.assertEquals("Test5", list.get(2));
    }

    @Test
    public void testRemoveAll() {
        List<String> sublist = new ArrayList<>();
        sublist.add("Test1");
        sublist.add("Test2");
        list.removeAll(sublist);
        Assertions.assertFalse(list.contains("Test1"));
        Assertions.assertFalse(list.contains("Test2"));
    }

    @Test
    public void testRetainAll() {
        List<String> sublist = new ArrayList<>();
        sublist.add("Test1");
        sublist.add("Test2");
        list.retainAll(sublist);
        Assertions.assertTrue(list.contains("Test1"));
        Assertions.assertTrue(list.contains("Test2"));
        Assertions.assertFalse(list.contains("Test3"));
    }

    @Test
    public void testClear() {
        list.clear();
        Assertions.assertTrue(list.isEmpty());
    }

    @Test
    public void testGet() {
        Assertions.assertEquals("Test1", list.get(0));
    }

    @Test
    public void testSet() {
        list.set(0, "Test4");
        Assertions.assertEquals("Test4", list.get(0));
    }

    @Test
    public void testRemoveAtIndex() {
        list.remove(0);
        Assertions.assertFalse(list.contains("Test1"));
    }

    @Test
    public void testIndexOf() {
        Assertions.assertEquals(0, list.indexOf("Test1"));
    }

    @Test
    public void testLastIndexOf() {
        list.add("Test1");
        Assertions.assertEquals(3, list.lastIndexOf("Test1"));
    }

    /*
     * In-house feature testing
     */

    @Test
    public void testFindAndRemove() {
        Assertions.assertNull(list.findAndRemove(e -> e.equals("Test4")));
        Assertions.assertEquals(3, list.size());

        Assertions.assertEquals("Test2", list.findAndRemove(e -> e.equals("Test2")));
        Assertions.assertEquals(2, list.size());
    }

    @Test
    public void testTimeoutFindAndRemove() {
        Assertions.assertNull(list.findAndRemove(e -> e.equals("Test4")));
        Assertions.assertEquals(3, list.size());

        timeSource.setTime(TIMEOUT + 1);

        Assertions.assertNull(list.findAndRemove(e -> e.equals("Test2")));
        // size returns the size of the backing list
        // the find and remove call does not remove stale records
        Assertions.assertEquals(3, list.size());
    }

    @Test
    public void testRemoveStale() {
        timeSource.setTime(TIMEOUT - 1);
        list.add("Test4");

        timeSource.setTime(TIMEOUT + 1);
        list.removeStale();

        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("Test4", list.get(0));
    }

    @Test
    public void testGetTimeout() {
        timeSource.setTime(TIMEOUT - 1);
        list.add("Test4");

        timeSource.setTime(TIMEOUT + 1);
        Assertions.assertEquals(4, list.size());
        Assertions.assertNull(list.get(0));
        Assertions.assertNull(list.get(0));
        Assertions.assertNull(list.get(0));
        Assertions.assertEquals("Test4", list.get(0));
    }
}
