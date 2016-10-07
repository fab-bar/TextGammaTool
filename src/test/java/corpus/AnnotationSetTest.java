package corpus;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import corpus.AnnotationSet;
import corpus.Annotator;
import corpus.Unit;

public class AnnotationSetTest {

	@Test
	public void testGetNumberOfAnnotators() {
		List<Unit> annots = new ArrayList<Unit>(3);
		annots.add(new Unit(new Annotator("A"), 2,3,null));
		annots.add(new Unit(new Annotator("B"), 1,2,null));
		annots.add(new Unit(new Annotator("A"), 1,2,null));
		
		AnnotationSet set = new AnnotationSet(annots);
		assertEquals(2, set.getNumberOfAnnotators());
	}
	
	@Test
	public void testGetNumberOfAnnotations() {
		List<Unit> annots = new ArrayList<Unit>(3);
		annots.add(new Unit(new Annotator("A"), 2,3,null));
		annots.add(new Unit(new Annotator("B"), 2,3,null));
		annots.add(new Unit(new Annotator("A"), 2,3,null));
		
		AnnotationSet set = new AnnotationSet(annots);
		assertEquals(2, set.getNumberOfAnnotations());
	}

	@Test
	public void testGetAverageNumberOfAnnotations() {
		List<Unit> annots = new ArrayList<Unit>(3);
		annots.add(new Unit(new Annotator("A"), 2,3,null));
		annots.add(new Unit(new Annotator("B"), 1,2,null));
		annots.add(new Unit(new Annotator("A"), 1,2,null));
		
		AnnotationSet set = new AnnotationSet(annots);
		assertEquals(1.5, set.getAverageNumberOfAnnotations(), 0);
	}

	@Test
	public void testGetAnnotators() {

		Set<Annotator> annotators = new HashSet<Annotator>();
		annotators.add(new Annotator("A"));
		annotators.add(new Annotator("B"));

		List<Unit> annots = new ArrayList<Unit>(3);
		annots.add(new Unit(new Annotator("A"), 2,3,null));
		annots.add(new Unit(new Annotator("B"), 1,2,null));
		annots.add(new Unit(new Annotator("A"), 1,2,null));

		AnnotationSet set = new AnnotationSet(annots);
		assertEquals(2, set.getAnnotators().length);
		for (int i = 0; i < set.getAnnotators().length; i++) {
			assertTrue(annotators.contains(set.getAnnotators()[i]));
		}
	}

	@Test
	public void testGetAnnotations() {
		List<Unit> annots = new ArrayList<Unit>(2);
		annots.add(new Unit(null, 2,3,null));
		annots.add(new Unit(null, 1,2,null));

		AnnotationSet set = new AnnotationSet(annots);

		assertFalse(Arrays.equals(set.getAnnotations(), annots.toArray(new Unit[2])));
		Collections.sort(annots);
		assertTrue(Arrays.equals(set.getAnnotations(), annots.toArray(new Unit[2])));
	}

	@Test
	public void testGetAnnotationsType() {
		List<Unit> annots = new ArrayList<Unit>(2);
		annots.add(new Unit(null, "A", 2,3,null));
		annots.add(new Unit(null, "", 1,2,null));
		annots.add(new Unit(null, null, 2,3,null));

		AnnotationSet set = new AnnotationSet(annots);

		assertEquals(1, set.getAnnotations("A").length);
		assertEquals(2, set.getAnnotations("").length);
		assertNotEquals(set.getAnnotations("A"), set.getAnnotations(""));
	}

	@Test
	public void testGetAnnotationsCreator() {

		Annotator a = new Annotator("A");
		Annotator b = new Annotator("");

		List<Unit> annots = new ArrayList<Unit>(2);
		annots.add(new Unit(a, null, 2,3,null));
		annots.add(new Unit(b, null, 1,2,null));
		annots.add(new Unit(null, null, 2,3,null));

		AnnotationSet set = new AnnotationSet(annots);

		assertEquals(1, set.getAnnotations(a).length);
		assertEquals(2, set.getAnnotations(b).length);
		assertNotEquals(set.getAnnotations(a), set.getAnnotations(b));
	}

	@Test
	public void testEquals() {
		List<Unit> annots = new ArrayList<Unit>(3);
		annots.add(new Unit(new Annotator("A"), 2,3,null));
		annots.add(new Unit(new Annotator("B"), 1,2,null));
		annots.add(new Unit(new Annotator("A"), 1,2,null));

		AnnotationSet set1 = new AnnotationSet(annots);
		AnnotationSet set2 = new AnnotationSet(annots);

		annots = new ArrayList<Unit>(3);
		annots.add(new Unit(new Annotator("A"), 2,3,null));
		annots.add(new Unit(new Annotator("B"), 1,2,null));
		annots.add(new Unit(new Annotator("B"), 1,2,null));

		AnnotationSet set3 = new AnnotationSet(annots);

		annots = new ArrayList<Unit>(3);
		annots.add(new Unit(new Annotator("A"), 1,3,null));
		annots.add(new Unit(new Annotator("B"), 1,2,null));
		annots.add(new Unit(new Annotator("A"), 1,2,null));

		AnnotationSet set4 = new AnnotationSet(annots);

		assertTrue(set1.equals(set1));
		assertTrue(set1.equals(set2));
		assertTrue(set2.equals(set1));
		assertFalse(set1.equals(null));
		assertFalse(set1.equals(set3));
		assertFalse(set3.equals(set1));
		assertFalse(set1.equals(set4));
		assertFalse(set4.equals(set1));
	}

}
