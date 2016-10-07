package corpus.alignment.dissimilarity;

import static org.junit.Assert.*;

import org.junit.Test;

import corpus.Annotator;
import corpus.TextUnit;
import corpus.Unit;
import corpus.alignment.dissimilarity.Dissimilarity;
import corpus.alignment.dissimilarity.NominalFeatureTextDissimilarity;

public class NominalFeatureTextDissimilarityTest {

	@Test
	public void testDissimilarity() {
		Dissimilarity diss = new NominalFeatureTextDissimilarity();
		
		Unit u = new TextUnit(new Annotator("1"), 0, 3, null, "Test");
		Unit v = new TextUnit(new Annotator("2"), 0, 3, null, "Test");
		assertEquals(0, diss.dissimilarity(u, v), 0);

		u = new TextUnit(new Annotator("1"), 0, 3, null, "Test");
		v = new TextUnit(new Annotator("2"), 0, 3, null, "Tester");
		assertEquals(1, diss.dissimilarity(u, v), 0);
		
		u = new TextUnit(new Annotator("1"), 3, 4, null, "Test");
		v = new TextUnit(new Annotator("2"), 0, 3, null, "Test");
		assertEquals(1, diss.dissimilarity(u, v), 0);
		
		v = u.cloneWithDifferentLabel("pos", "A");
		assertEquals(0.5, diss.dissimilarity(u, v), 0);
		
		u = new TextUnit(new Annotator("1"), 3, 4, null, "Tester");
		assertEquals(1, diss.dissimilarity(u, v), 0);
		
		u = new TextUnit(new Annotator("1"), 0, 3, null, "Tester");
		assertEquals(2, diss.dissimilarity(u, v), 0);
		
		u = v;
		assertEquals(0, diss.dissimilarity(u, v), 0);

		assertEquals(1, diss.dissimilarity(u, null), 0);
		assertEquals(1, diss.dissimilarity(null, v), 0);

		assertEquals(0, diss.dissimilarity(null, null), 0);
	}

}
