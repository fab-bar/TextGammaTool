package corpus;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import corpus.Annotator;
import corpus.Unit;

public class UnitTest {

	@Test(expected=IllegalArgumentException.class)
	public void createUnitBeginBiggerEnd() {
	    new Unit(null, 1, 0, null);
	}

	@Test
	public void testEqualsObject() {
		assertTrue(new Unit(new Annotator("1"), 0, 3, null).equals(new Unit(new Annotator("1"), null, 0, 3, null)));
		assertTrue(new Unit(new Annotator("1"), "Test", 0, 3, null).equals(new Unit(new Annotator("1"), "Test", 0, 3, null)));
		assertFalse(new Unit(new Annotator("1"), "Test", 0, 3, null).equals(new Unit(null, "Test", 0, 3, null)));
		assertFalse(new Unit(null, null, 0, 3, null).equals(new Unit(null, "Test", 0, 3, null)));
		assertFalse(new Unit(null, null, 0, 3, null).equals(new Unit(null, null, 1, 3, null)));
	}
	
	@Test
	public void testOverlaps() {
		assertTrue(new Unit(null, null, 0, 3, null).overlaps(new Unit(null, null, 2, 4, null)));
		assertTrue(new Unit(null, null, 0, 3, null).overlaps(new Unit(null, null, 0, 3, null)));
		assertFalse(new Unit(null, null, 0, 3, null).overlaps(new Unit(null, null, 3, 5, null)));
		assertFalse(new Unit(null, null, 0, 3, null).overlaps(new Unit(null, null, 4, 5, null)));
		assertFalse(new Unit(null, null, 4, 5, null).overlaps(new Unit(null, null, 0, 3, null)));
		assertFalse(new Unit(null, null, 4, 5, null).overlaps(new Unit(null, null, 0, 4, null)));
	}
	
	@Test
	public void testCompareToUnit() {
		assertEquals(new Unit(null, null, 0, 3, null).compareTo(new Unit(null, null, 2, 4, null)), -1);
		assertEquals(new Unit(null, null, 3, 4, null).compareTo(new Unit(null, null, 2, 4, null)), 1);
		assertEquals(new Unit(null, null, 2, 3, null).compareTo(new Unit(null, null, 2, 4, null)), -1);
		assertEquals(new Unit(null, null, 2, 4, null).compareTo(new Unit(null, null, 2, 3, null)), 1);

		assertEquals(new Unit(null, "a", 2, 4, null).compareTo(new Unit(null, "b", 2, 4, null)), -1);
		assertEquals(new Unit(null, "b", 2, 4, null).compareTo(new Unit(null, "a", 2, 4, null)), 1);
		
		assertEquals(new Unit(null, null, 2, 4, null).compareTo(new Unit(null, "b", 2, 4, null)), -1);
		assertEquals(new Unit(null, "a", 2, 4, null).compareTo(new Unit(null, null, 2, 4, null)), 1);
		
		assertEquals(new Unit(new Annotator("1"), null, 2, 4, null).compareTo(new Unit(new Annotator("2"), null, 2, 4, null)), -1);
		assertEquals(new Unit(new Annotator("2"), null, 2, 4, null).compareTo(new Unit(new Annotator("1"), null, 2, 4, null)), 1);
		assertEquals(new Unit(null, null, 2, 4, null).compareTo(new Unit(new Annotator("2"), null, 2, 4, null)), -1);
		assertEquals(new Unit(new Annotator("2"), null, 2, 4, null).compareTo(new Unit(null, null, 2, 4, null)), 1);
		
		HashMap<String, String> a1 = new HashMap<String, String>();
		HashMap<String, String> a2 = new HashMap<String, String>();
		HashMap<String, String> b = new HashMap<String, String>();
		
		a1.put("a", "a");
		a2.put("a", "b");
		b.put("b", "a");
				
		assertEquals(new Unit(null, null, 2, 4, a1).compareTo(new Unit(null, null, 2, 4, b)), -1);
		assertEquals(new Unit(null, null, 2, 4, b).compareTo(new Unit(null, null, 2, 4, a1)), 1);
		assertEquals(new Unit(null, null, 2, 4, a1).compareTo(new Unit(null, null, 2, 4, a2)), -1);
		assertEquals(new Unit(null, null, 2, 4, a2).compareTo(new Unit(null, null, 2, 4, a1)), 1);
		assertEquals(new Unit(null, null, 2, 4, null).compareTo(new Unit(null, null, 2, 4, a1)), -1);
		assertEquals(new Unit(null, null, 2, 4, a1).compareTo(new Unit(null, null, 2, 4, null)), 1);
		
		assertEquals(new Unit(null, null, 2, 4, null).compareTo(new Unit(null, null, 2, 4, null)), 0);
	
	}

	@Test
	public void testHash() {
		assertEquals(new Unit(new Annotator("1"), 0, 3, null).hashCode(), new Unit(new Annotator("1"), null, 0, 3, null).hashCode());
		assertEquals(new Unit(new Annotator("1"), "Test", 0, 3, null).hashCode(), new Unit(new Annotator("1"), "Test", 0, 3, null).hashCode());

		HashMap<String, String> a1 = new HashMap<String, String>();
		HashMap<String, String> a2 = new HashMap<String, String>();

		a1.put("a", "b");
		a2.put("a", "b");

		assertEquals(new Unit(null, null, 2, 4, a1).hashCode(), new Unit(null, null, 2, 4, a2).hashCode());
	}

}