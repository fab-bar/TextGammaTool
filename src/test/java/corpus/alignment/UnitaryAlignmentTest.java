package corpus.alignment;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.Test;

import corpus.AnnotationSet;
import corpus.Annotator;
import corpus.Unit;
import corpus.alignment.UnitaryAlignment;
import corpus.alignment.dissimilarity.NominalFeatureDissimilarity;

public class UnitaryAlignmentTest {

	@Test(expected=IllegalArgumentException.class)
	public void testCreationWithAnnotationsFromOneCreator() {
		List<Unit> units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("1"), null, 0, 4, null));
		
		AnnotationSet as = new AnnotationSet(units);
		
		new UnitaryAlignment(Arrays.asList(as.getAnnotations()), new HashSet<Annotator>(Arrays.asList(as.getAnnotators())));
		
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreationWithAnnotationsFromUnknownCreator() {
		List<Unit> units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 4, null));
		
		AnnotationSet as = new AnnotationSet(units);
		
		new UnitaryAlignment(Arrays.asList(as.getAnnotations()), new HashSet<Annotator>(Arrays.asList(as.getAnnotators()[0])));
		
	}


	@Test
	public void testArity() {
		
		List<Unit> units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 3, null));
		
		AnnotationSet as = new AnnotationSet(units);
		
		UnitaryAlignment a = new UnitaryAlignment(Arrays.asList(as.getAnnotations()), new HashSet<Annotator>(Arrays.asList(as.getAnnotators())));
		assertEquals(2, a.arity());
	}

	@Test
	public void testGetDisorder() {
		
		List<Unit> units = new ArrayList<Unit>(3);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 3, null));
		
		AnnotationSet as = new AnnotationSet(units);
		
		UnitaryAlignment a = new UnitaryAlignment(Arrays.asList(as.getAnnotations()), new HashSet<Annotator>(Arrays.asList(as.getAnnotators())));
		assertEquals(0, a.getDisorder(new NominalFeatureDissimilarity()), 0);
		
		units.add(new Unit(new Annotator("3"), null, 3, 4, null));
		
		as = new AnnotationSet(units);
		
		a = new UnitaryAlignment(Arrays.asList(as.getAnnotations()), new HashSet<Annotator>(Arrays.asList(as.getAnnotators())));
		assertEquals(2/3.0, a.getDisorder(new NominalFeatureDissimilarity()), 0.0001);

		units = new ArrayList<Unit>(1);
		units.add(new Unit(new Annotator("3"), null, 3, 4, null));

		a = new UnitaryAlignment(units, new HashSet<Annotator>(Arrays.asList(as.getAnnotators())));
		assertEquals(2/3.0, a.getDisorder(new NominalFeatureDissimilarity()), 0.0001);

	}

	@Test
	public void testEqualsObject() {

		List<Unit> units = new ArrayList<Unit>(2);

		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 3, null));

		Set<Annotator> annotators = new HashSet<Annotator>();

		annotators.add(new Annotator("1"));
		annotators.add(new Annotator("2"));

		UnitaryAlignment a = new UnitaryAlignment(units, annotators);

		annotators.add(new Annotator("3"));

		UnitaryAlignment b = new UnitaryAlignment(units, annotators);

		// different arity
		assertFalse(Objects.equals(a,b));

		units.clear();
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("3"), null, 0, 3, null));

		annotators = new HashSet<Annotator>();

		annotators.add(new Annotator("1"));
		annotators.add(new Annotator("3"));

		UnitaryAlignment c = new UnitaryAlignment(units, annotators);

		// different annotators
		assertFalse(Objects.equals(a,c));

		units.clear();
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 2, null));

		annotators = new HashSet<Annotator>();

		annotators.add(new Annotator("1"));
		annotators.add(new Annotator("2"));

		UnitaryAlignment d = new UnitaryAlignment(units, annotators);

		// different units
		assertFalse(Objects.equals(a,d));

		units.clear();
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 3, null));

		UnitaryAlignment e = new UnitaryAlignment(units, annotators);

		// equal alignments
		assertTrue(Objects.equals(a,e));
	}

	@Test
	public void testCompareToUnitaryAlignment() {

		List<Unit> units = new ArrayList<Unit>(2);

		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 3, null));

		Set<Annotator> annotators = new HashSet<Annotator>();

		annotators.add(new Annotator("1"));
		annotators.add(new Annotator("2"));

		UnitaryAlignment a = new UnitaryAlignment(units, annotators);

		annotators.add(new Annotator("3"));

		UnitaryAlignment b = new UnitaryAlignment(units, annotators);

		// different arity
		assertEquals(-1, a.compareTo(b));
		assertEquals(1, b.compareTo(a));

		units.clear();
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("3"), null, 0, 3, null));

		annotators = new HashSet<Annotator>();

		annotators.add(new Annotator("1"));
		annotators.add(new Annotator("3"));

		UnitaryAlignment c = new UnitaryAlignment(units, annotators);

		// different annotators
		assertEquals(-1, a.compareTo(c));
		assertEquals(1, c.compareTo(a));

		units.clear();
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 2, null));

		annotators = new HashSet<Annotator>();

		annotators.add(new Annotator("1"));
		annotators.add(new Annotator("2"));

		UnitaryAlignment d = new UnitaryAlignment(units, annotators);

		// different units
		assertEquals(-1, d.compareTo(a));
		assertEquals(1, a.compareTo(d));

		units.clear();
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 3, null));

		UnitaryAlignment e = new UnitaryAlignment(units, annotators);

		// equal alignments
		assertEquals(0, a.compareTo(e));
	}

}
