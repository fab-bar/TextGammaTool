package corpus.alignment;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import corpus.AnnotationSet;
import corpus.Annotator;
import corpus.Unit;
import corpus.alignment.Alignment;
import corpus.alignment.UnitaryAlignment;
import corpus.alignment.dissimilarity.NominalFeatureDissimilarity;

public class AlignmentTest {

	@Test(expected=IllegalArgumentException.class)
	public void testAlignmentWithDifferingCreators() {
		
		Set<UnitaryAlignment> uas = new HashSet<UnitaryAlignment>();
		
		List<Unit> annotations = new ArrayList<Unit>(4);
		
		Set<Annotator> creators = new HashSet<Annotator>();
		creators.add(new Annotator("1"));
		creators.add(new Annotator("2"));
				
		List<Unit> units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 4, null));
		
		uas.add(new UnitaryAlignment(units, creators));
		
		annotations.addAll(units);
		
		creators.add(new Annotator("3"));
		
		units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 1, 3, null));
		units.add(new Unit(new Annotator("3"), null, 0, 4, null));
		
		uas.add(new UnitaryAlignment(units, creators));
		
		annotations.addAll(units);
		
		new Alignment(uas, new AnnotationSet(annotations));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAlignmentWithUnitContainedTwice() {
		
		Set<UnitaryAlignment> uas = new HashSet<UnitaryAlignment>();
		
		List<Unit> annotations = new ArrayList<Unit>(4);
		
		Set<Annotator> creators = new HashSet<Annotator>();
		creators.add(new Annotator("1"));
		creators.add(new Annotator("2"));
				
		List<Unit> units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 4, null));
		
		uas.add(new UnitaryAlignment(units, creators));
		
		annotations.addAll(units);
		
		units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 1, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 4, null));
		
		uas.add(new UnitaryAlignment(units, creators));
		
		annotations.addAll(units);
		
		new Alignment(uas, new AnnotationSet(annotations));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAlignmentWithUnitNotContainedInAS() {
		
		Set<UnitaryAlignment> uas = new HashSet<UnitaryAlignment>();
		
		List<Unit> annotations = new ArrayList<Unit>(2);
		
		Set<Annotator> creators = new HashSet<Annotator>();
		creators.add(new Annotator("1"));
		creators.add(new Annotator("2"));
				
		List<Unit> units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 4, null));
		
		uas.add(new UnitaryAlignment(units, creators));
		
		annotations.addAll(units);
		
		units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 1, 3, null));
		
		uas.add(new UnitaryAlignment(units, creators));
			
		new Alignment(uas, new AnnotationSet(annotations));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAlignmentWithUnitNotContainedInUA() {
		
		Set<UnitaryAlignment> uas = new HashSet<UnitaryAlignment>();
		
		List<Unit> annotations = new ArrayList<Unit>(3);
		
		Set<Annotator> creators = new HashSet<Annotator>();
		creators.add(new Annotator("1"));
		creators.add(new Annotator("2"));
				
		List<Unit> units = new ArrayList<Unit>(2);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 4, null));
		
		uas.add(new UnitaryAlignment(units, creators));
		
		annotations.addAll(units);
		annotations.add(new Unit(new Annotator("1"), null, 1, 3, null));
				
		new Alignment(uas, new AnnotationSet(annotations));
	}

	@Test
	public void testGetDisorder() {
		
		Set<UnitaryAlignment> uas = new HashSet<UnitaryAlignment>();
		
		Set<Annotator> creators = new HashSet<Annotator>();
		creators.add(new Annotator("1"));
		creators.add(new Annotator("2"));
		creators.add(new Annotator("3"));
		
		List<Unit> annotations = new ArrayList<Unit>(4);
		
		List<Unit> units = new ArrayList<Unit>(3);
		
		units.add(new Unit(new Annotator("1"), null, 0, 3, null));
		units.add(new Unit(new Annotator("2"), null, 0, 3, null));
		units.add(new Unit(new Annotator("3"), null, 0, 3, null));
			
		uas.add(new UnitaryAlignment(units, creators));
			
		annotations.addAll(units);
		
		units = new ArrayList<Unit>(1);
		units.add(new Unit(new Annotator("3"), null, 3, 4, null));

		uas.add(new UnitaryAlignment(units, creators));
		
		annotations.addAll(units);

		Alignment a = new Alignment(uas, new AnnotationSet(annotations));

		double averageAnnotations = 4/3.0;
		double disorder_1 = 0;
		double disorder_2 = 2/3.0;
		assertEquals((disorder_1 + disorder_2)/averageAnnotations, a.getDisorder(new NominalFeatureDissimilarity()), 0.0001);
		
	}

}
