package corpus.shuffling;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import corpus.AnnotatedText;
import corpus.AnnotationSet;
import corpus.Annotator;
import corpus.Text;
import corpus.TextUnit;
import corpus.Unit;
import corpus.shuffling.AnnotationSetShuffle;
import corpus.shuffling.AnnotationSetShuffle.SegmentationChangetype;
import corpus.shuffling.AnnotationSetShuffle.TextChangetype;

public class AnnotationSetShuffleTest {

	private class FakeRandom extends Random {

		private static final long serialVersionUID = 1L;
		List<Integer> numberSequence;
		Iterator<Integer> it;

		public FakeRandom(List<Integer> numberSequence) {
			if (numberSequence.isEmpty())
				throw new IllegalArgumentException("Sequence may not be empty.");

			this.numberSequence = numberSequence;
			this.it = this.numberSequence.iterator();
		}

		@Override
		public int nextInt() {

			if (!it.hasNext())
				throw new RuntimeException("Sequence exhausted.");

			return it.next();
		}

		@Override
		public int nextInt(int max) {

			int next = this.nextInt();

			if(next >= max)
				throw new RuntimeException("Next above max.");

			return next;

		}
	}

	public void testShuffleText() {
		List<Unit> annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,4,null,"kauf"));
		annots1.add(new TextUnit(new Annotator("A"), 4,8,null, "mann"));
		AnnotatedText text1 = new AnnotatedText(new Text("kaufmann"), annots1);

		for (double i = 0; i <= 1; i += 0.1) {
			System.out.println(AnnotationSetShuffle.shuffleText(text1, i).getText().getTextualContent());
		}

	}

	public void testShuffleSegmentation() {
		List<Unit> annots = new ArrayList<Unit>(4);

		annots.add(new Unit(new Annotator("B"), 0,1,null));
		annots.add(new Unit(new Annotator("A"), 0,2,null));
		annots.add(new Unit(new Annotator("B"), 1,3,null));
		annots.add(new Unit(new Annotator("A"), 2,3,null));

		AnnotationSet orig = new AnnotationSet(annots);

		for (double i = 0; i <= 1; i += 0.1) {
			System.out.println(AnnotationSetShuffle.shuffleSegmentation(orig, i).getNumberOfAnnotations());
		}

	}

	public void testShuffleAttributeValues() {
		List<Unit> annots = new ArrayList<Unit>(4);

		HashMap<String, String> feats1 = new HashMap<String, String>();
		feats1.put("pos", "A");

		HashMap<String, String> feats2 = new HashMap<String, String>();
		feats2.put("pos", "B");


		annots.add(new Unit(new Annotator("B"), 0,1,feats1));
		annots.add(new Unit(new Annotator("A"), 0,2,feats1));
		annots.add(new Unit(new Annotator("B"), 1,3,feats2));
		annots.add(new Unit(new Annotator("A"), 2,3,feats2));

		AnnotationSet orig = new AnnotationSet(annots);

		for (double i = 0; i <= 1; i += 0.1) {
			AnnotationSet res = AnnotationSetShuffle.shuffleAttributeValues(orig, "pos", i);
			for (Unit annotation: res.getAnnotations())
				System.out.print(annotation.getAttributeValue("pos"));
			System.out.println();
		}

	}

	@Test
	public void testChangeText() {

		List<Unit> annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,4,null,"kauf"));
		annots1.add(new TextUnit(new Annotator("A"), 4,8,null, "mann"));
		AnnotatedText text1 = new AnnotatedText(new Text("kaufmann"), annots1);

		List<Pair<Character, Double>> p = new ArrayList<Pair<Character, Double>>(1);
		p.add(new Pair<Character, Double>('F', 1.0));
		EnumeratedDistribution<Character> gen = new EnumeratedDistribution<Character>(p);

		List<Pair<TextChangetype, Double>> c = new ArrayList<Pair<TextChangetype, Double>>(1);
		c.add(new Pair<TextChangetype, Double>(TextChangetype.INSERTION, 1.0));
		EnumeratedDistribution<TextChangetype> changeChooser = new EnumeratedDistribution<TextChangetype>(c);

		AnnotatedText shuffeledText = AnnotationSetShuffle.changeText(text1, 2, changeChooser, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 0, 1, 4))));

		assertEquals("FkaufmannF", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(5, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(5, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(10, shuffeledText.getAnnotations()[1].getEnd());

		c = new ArrayList<Pair<TextChangetype, Double>>(1);
		c.add(new Pair<TextChangetype, Double>(TextChangetype.DELETION, 1.0));
		changeChooser = new EnumeratedDistribution<TextChangetype>(c);

		shuffeledText = AnnotationSetShuffle.changeText(text1, 2, changeChooser, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 0, 1, 3))));

		assertEquals("aufman", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(3, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(3, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(6, shuffeledText.getAnnotations()[1].getEnd());

		c = new ArrayList<Pair<TextChangetype, Double>>(1);
		c.add(new Pair<TextChangetype, Double>(TextChangetype.SUBSTITUTION, 1.0));
		changeChooser = new EnumeratedDistribution<TextChangetype>(c);

		shuffeledText = AnnotationSetShuffle.changeText(text1, 2, changeChooser, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 0, 1, 3))));

		assertEquals("FaufmanF", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(8, shuffeledText.getAnnotations()[1].getEnd());
	}

	@Test
	public void testChangeTextInsertion() {

		List<Unit> annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,4,null,"kauf"));
		annots1.add(new TextUnit(new Annotator("A"), 4,8,null, "mann"));
		AnnotatedText text1 = new AnnotatedText(new Text("kaufmann"), annots1);

		List<Pair<Character, Double>> p = new ArrayList<Pair<Character, Double>>(1);
		p.add(new Pair<Character, Double>('F', 1.0));
		EnumeratedDistribution<Character> gen = new EnumeratedDistribution<Character>(p);

		AnnotatedText shuffeledText = AnnotationSetShuffle.changeTextInsertion(text1, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 0))));

		assertEquals("Fkaufmann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(5, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(5, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(9, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextInsertion(text1, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(1, 4))));

		assertEquals("kaufmannF", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(9, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextInsertion(text1, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0,4))));

		assertEquals("kaufFmann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(5, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(5, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(9, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextInsertion(text1, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(1,0))));

		assertEquals("kaufFmann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(9, shuffeledText.getAnnotations()[1].getEnd());

	}
	@Test
	public void testChangeTextDeletion() {
		List<Unit> annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,4,null,"kauf"));
		annots1.add(new TextUnit(new Annotator("A"), 4,8,null, "mann"));
		AnnotatedText text1 = new AnnotatedText(new Text("kaufmann"), annots1);

		AnnotatedText shuffeledText = AnnotationSetShuffle.changeTextDeletion(text1,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 0))));

		assertEquals("aufmann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(3, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(3, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(7, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextDeletion(text1,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(1, 3))));

		assertEquals("kaufman", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(7, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextDeletion(text1,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0,3))));

		assertEquals("kaumann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(3, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(3, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(7, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextDeletion(text1,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(1,0))));

		assertEquals("kaufann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(7, shuffeledText.getAnnotations()[1].getEnd());

		// delete a character that is a TextUnit
		annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,1,null,"a"));
		annots1.add(new TextUnit(new Annotator("A"), 2,6,null, "test"));
		text1 = new AnnotatedText(new Text("a test"), annots1);

		shuffeledText = AnnotationSetShuffle.changeTextDeletion(text1,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0,0))));

		assertEquals(" test", shuffeledText.getText().getTextualContent());
		assertEquals(1, shuffeledText.getNumberOfAnnotations());
		assertEquals(1, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(5, shuffeledText.getAnnotations()[0].getEnd());
	}

	@Test
	public void testChangeTextSubstitution() {

		List<Unit> annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,4,null,"kauf"));
		annots1.add(new TextUnit(new Annotator("A"), 4,8,null, "mann"));
		AnnotatedText text1 = new AnnotatedText(new Text("kaufmann"), annots1);

		List<Pair<Character, Double>> p = new ArrayList<Pair<Character, Double>>(1);
		p.add(new Pair<Character, Double>('F', 1.0));
		EnumeratedDistribution<Character> gen = new EnumeratedDistribution<Character>(p);

		AnnotatedText shuffeledText = AnnotationSetShuffle.changeTextSubstitution(text1, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 0))));

		assertEquals("Faufmann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(8, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextSubstitution(text1, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(1, 3))));

		assertEquals("kaufmanF", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(8, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextSubstitution(text1, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0,3))));

		assertEquals("kauFmann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(8, shuffeledText.getAnnotations()[1].getEnd());

		shuffeledText = AnnotationSetShuffle.changeTextSubstitution(text1, gen,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(1,0))));

		assertEquals("kaufFann", shuffeledText.getText().getTextualContent());
		assertEquals(0, shuffeledText.getAnnotations()[0].getBegin());
		assertEquals(4, shuffeledText.getAnnotations()[0].getEnd());
		assertEquals(4, shuffeledText.getAnnotations()[1].getBegin());
		assertEquals(8, shuffeledText.getAnnotations()[1].getEnd());

	}

	@Test
	public void testChangeSegmentation() {

		List<Unit> annots = new ArrayList<Unit>(4);

		annots.add(new Unit(new Annotator("B"), 0,1,null));
		annots.add(new Unit(new Annotator("A"), 0,2,null));
		annots.add(new Unit(new Annotator("B"), 1,3,null));
		annots.add(new Unit(new Annotator("A"), 2,3,null));

		AnnotationSet split = new AnnotationSet(annots);

		annots = new ArrayList<Unit>(2);
		annots.add(new Unit(new Annotator("A"), 0,3,null));
		annots.add(new Unit(new Annotator("B"), 0,3,null));

		AnnotationSet merged = new AnnotationSet(annots);

		AnnotationSet set;

		// Merge
		List<Pair<SegmentationChangetype, Double>> c = new ArrayList<Pair<SegmentationChangetype, Double>>(1);
		c.add(new Pair<SegmentationChangetype, Double>(SegmentationChangetype.MERGE, 1.0));
		EnumeratedDistribution<SegmentationChangetype> changeChooser =
				new EnumeratedDistribution<SegmentationChangetype>(c);

		set = AnnotationSetShuffle.changeSegmentation(split, 2, changeChooser,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0,1))));
		assertEquals(merged, set);

		// Split
		c = new ArrayList<Pair<SegmentationChangetype, Double>>(1);
		c.add(new Pair<SegmentationChangetype, Double>(SegmentationChangetype.SPLIT, 1.0));
		changeChooser = new EnumeratedDistribution<SegmentationChangetype>(c);

		set = AnnotationSetShuffle.changeSegmentation(merged, 2, changeChooser,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0,1,2,0))));
		assertEquals(split, set);
	}

	@Test
	public void testChangeSegmentationMerge() {

		List<Unit> annots = new ArrayList<Unit>(4);

		annots.add(new Unit(new Annotator("B"), 0,1,null));
		annots.add(new Unit(new Annotator("A"), 0,2,null));
		annots.add(new Unit(new Annotator("B"), 1,3,null));
		annots.add(new Unit(new Annotator("A"), 2,3,null));

		AnnotationSet orig = new AnnotationSet(annots);
		AnnotationSet set, res;

		// Merge Bs
		annots = new ArrayList<Unit>(3);

		annots.add(new Unit(new Annotator("A"), 0,2,null));
		annots.add(new Unit(new Annotator("B"), 0,3,null));
		annots.add(new Unit(new Annotator("A"), 2,3,null));

		res = new AnnotationSet(annots);

		set = AnnotationSetShuffle.changeSegmentationMerge(orig,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0))));
		assertEquals(res, set);

		// Merge As
		annots = new ArrayList<Unit>(3);

		annots.add(new Unit(new Annotator("A"), 0,3,null));
		annots.add(new Unit(new Annotator("B"), 0,1,null));
		annots.add(new Unit(new Annotator("B"), 1,3,null));

		res = new AnnotationSet(annots);

		set = AnnotationSetShuffle.changeSegmentationMerge(orig,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(1))));
		assertEquals(res, set);

		// Merge nothing
		res = orig;

		set = AnnotationSetShuffle.changeSegmentationMerge(orig,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(2))));
		assertEquals(res, set);
		set = AnnotationSetShuffle.changeSegmentationMerge(orig, 0,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(3))));
		assertEquals(res, set);

		// Merge TextUnits
		annots = new ArrayList<Unit>(2);

		annots.add(new TextUnit(new Annotator("B"), 0,1,null, "kauf"));
		annots.add(new TextUnit(new Annotator("B"), 1,2,null, "mann"));

		orig = new AnnotationSet(annots);

		annots = new ArrayList<Unit>(1);

		annots.add(new TextUnit(new Annotator("B"), 0,2,null, "kaufmann"));

		res = new AnnotationSet(annots);

		set = AnnotationSetShuffle.changeSegmentationMerge(orig,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0))));
		assertEquals(res, set);

	}

	@Test
	public void testChangeSegmentationMergeWithGap() {

		List<Unit> annots = new ArrayList<Unit>(2);

		annots.add(new Unit(new Annotator("B"), 0,1,null));
		annots.add(new Unit(new Annotator("B"), 2,3,null));

		AnnotationSet orig = new AnnotationSet(annots);

		// Merge without gap
		AnnotationSet set = AnnotationSetShuffle.changeSegmentationMerge(orig, 0,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0))));
		assertEquals(orig, set);

		// Merge with gap
		annots = new ArrayList<Unit>(1);

		annots.add(new Unit(new Annotator("B"), 0,3,null));

		AnnotationSet res = new AnnotationSet(annots);

		set = AnnotationSetShuffle.changeSegmentationMerge(orig, 1,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0))));
		assertEquals(res, set);

		// Merge TextUnits
		annots = new ArrayList<Unit>(2);

		annots.add(new TextUnit(new Annotator("B"), 0,1,null, "kauf"));
		annots.add(new TextUnit(new Annotator("B"), 2,3,null, "mann"));

		orig = new AnnotationSet(annots);

		annots = new ArrayList<Unit>(1);

		annots.add(new TextUnit(new Annotator("B"), 0,3,null, "kauf mann"));

		res = new AnnotationSet(annots);

		set = AnnotationSetShuffle.changeSegmentationMerge(orig, 1,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 0))));
		assertEquals(res, set);
	}

	@Test
	public void testChangeSegmentationSplit() {
		List<Unit> annots = new ArrayList<Unit>(4);

		annots.add(new Unit(new Annotator("A"), 0,5,null));

		AnnotationSet orig = new AnnotationSet(annots);

		AnnotationSet res;

		for (int i=0; i < orig.getAnnotations()[0].getEnd() - 1; i++) {
			res = AnnotationSetShuffle.changeSegmentationSplit(orig,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, i))));

			assertEquals(2, res.getNumberOfAnnotations());

			assertEquals(0, res.getAnnotations()[0].getBegin());
			assertEquals(i+1, res.getAnnotations()[0].getEnd());
			assertEquals(i+1, res.getAnnotations()[1].getBegin());
			assertEquals(5, res.getAnnotations()[1].getEnd());
		}

		// Test TextUnit

		annots = new ArrayList<Unit>(1);

		annots.add(new TextUnit(new Annotator("B"), 0, 8, null, "kaufmann"));

		orig = new AnnotationSet(annots);
		res = AnnotationSetShuffle.changeSegmentationSplit(orig,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 3))));

		assertEquals(0, res.getAnnotations()[0].getBegin());
		assertEquals(4, res.getAnnotations()[0].getEnd());
		assertEquals("kauf", ((TextUnit)res.getAnnotations()[0]).getText());
		assertEquals(4, res.getAnnotations()[1].getBegin());
		assertEquals(8, res.getAnnotations()[1].getEnd());
		assertEquals("mann", ((TextUnit)res.getAnnotations()[1]).getText());

		res = AnnotationSetShuffle.changeSegmentationSplit(orig,
				new FakeRandom(new ArrayList<Integer>(Arrays.asList(0, 6))));

		assertEquals(0, res.getAnnotations()[0].getBegin());
		assertEquals(7, res.getAnnotations()[0].getEnd());
		assertEquals("kaufman", ((TextUnit)res.getAnnotations()[0]).getText());
		assertEquals(7, res.getAnnotations()[1].getBegin());
		assertEquals(8, res.getAnnotations()[1].getEnd());
		assertEquals("n", ((TextUnit)res.getAnnotations()[1]).getText());

	}

	@Test
	public void testRandomizeAttributeValues() {
		List<Unit> annots = new ArrayList<Unit>(3);
		HashMap<String, String> feats = new HashMap<String, String>();
		feats.put("pos", "A");

		annots.add(new Unit(new Annotator("A"), 2,3,feats));
		annots.add(new Unit(new Annotator("B"), 1,2,feats));
		annots.add(new Unit(new Annotator("A"), 1,2,feats));

		List<Pair<String, Double>> p = new ArrayList<Pair<String, Double>>(1);
		p.add(new Pair<String, Double>("B", 1.0));
		EnumeratedDistribution<String> gen = new EnumeratedDistribution<String>(p);
		AnnotationSet set = AnnotationSetShuffle.randomizeAttributeValues(new AnnotationSet(annots), "pos", gen);

		assertEquals(2, set.getNumberOfAnnotators());
		assertEquals(3, set.getNumberOfAnnotations());

		for (Unit a: set.getAnnotations())
			assertEquals("B", a.getAttributeValue("pos"));
		
	}

	@Test
	public void testChangeAttributeValue() {

		List<Unit> annots = new ArrayList<Unit>(3);
		HashMap<String, String> feats = new HashMap<String, String>();
		feats.put("pos", "A");

		annots.add(new Unit(new Annotator("A"), 0,1,feats));
		annots.add(new Unit(new Annotator("B"), 1,2,feats));
		annots.add(new Unit(new Annotator("A"), 2,3,feats));

		List<Pair<String, Double>> p = new ArrayList<Pair<String, Double>>(1);
		p.add(new Pair<String, Double>("B", 1.0));
		EnumeratedDistribution<String> gen = new EnumeratedDistribution<String>(p);

		for (int i = 0; i < 3; i++){
			AnnotationSet set = AnnotationSetShuffle.changeAttributeValue(new AnnotationSet(annots), "pos", gen,
					new FakeRandom(new ArrayList<Integer>(Arrays.asList(i))));

			assertEquals(2, set.getNumberOfAnnotators());
			assertEquals(3, set.getNumberOfAnnotations());

			for (int j = 0; j < 3; j++)
				if (j==i)
					assertEquals("B", set.getAnnotations()[j].getAttributeValue("pos"));
				else
					assertEquals("A", set.getAnnotations()[j].getAttributeValue("pos"));
		}

	}

	@Test
	public void testChangeAttributeValues() {

		List<Unit> annots = new ArrayList<Unit>(3);
		HashMap<String, String> feats = new HashMap<String, String>();
		feats.put("pos", "A");

		annots.add(new Unit(new Annotator("A"), 0,1,feats));
		annots.add(new Unit(new Annotator("B"), 1,2,feats));
		annots.add(new Unit(new Annotator("A"), 2,3,feats));

		List<Pair<String, Double>> p = new ArrayList<Pair<String, Double>>(1);
		p.add(new Pair<String, Double>("B", 1.0));
		EnumeratedDistribution<String> gen = new EnumeratedDistribution<String>(p);

		for (int i = 1; i < 3; i++){
			AnnotationSet set = AnnotationSetShuffle.changeAttributeValues(new AnnotationSet(annots), "pos", 2, gen,
					new FakeRandom(new ArrayList<Integer>(Arrays.asList(i, 0))));

			assertEquals(2, set.getNumberOfAnnotators());
			assertEquals(3, set.getNumberOfAnnotations());

			for (int j = 0; j < 3; j++)
				if (j==i || j==0)
					assertEquals("B", set.getAnnotations()[j].getAttributeValue("pos"));
				else
					assertEquals("A", set.getAnnotations()[j].getAttributeValue("pos"));
		}

	}
}
