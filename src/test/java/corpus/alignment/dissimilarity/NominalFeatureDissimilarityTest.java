package corpus.alignment.dissimilarity;

import static org.junit.Assert.*;

import org.junit.Test;

import corpus.Annotator;
import corpus.Unit;
import corpus.alignment.dissimilarity.Dissimilarity;
import corpus.alignment.dissimilarity.NominalFeatureDissimilarity;

public class NominalFeatureDissimilarityTest {

	@Test
	public void testDissimilarity() {
		Dissimilarity diss = new NominalFeatureDissimilarity();
		
		Unit u = new Unit(new Annotator("1"), null, 0, 3, null);
		Unit v = new Unit(new Annotator("2"), null, 0, 3, null);
		assertEquals(0, diss.dissimilarity(u, v), 0);

		u = new Unit(new Annotator("1"), "textunit", 0, 3, null);
		v = new Unit(new Annotator("2"), null, 0, 3, null);
		assertEquals(1, diss.dissimilarity(u, v), 0);
		
		u = new Unit(new Annotator("1"), null, 3, 4, null);
		v = new Unit(new Annotator("2"), null, 0, 3, null);
		assertEquals(1, diss.dissimilarity(u, v), 0);
		
		v = u.cloneWithDifferentLabel("pos", "A");
		assertEquals(1, diss.dissimilarity(u, v), 0);
		
		u = v;
		assertEquals(0, diss.dissimilarity(u, v), 0);

		assertEquals(1, diss.dissimilarity(u, null), 0);
		assertEquals(1, diss.dissimilarity(null, v), 0);

		assertEquals(0, diss.dissimilarity(null, null), 0);
	}

}
