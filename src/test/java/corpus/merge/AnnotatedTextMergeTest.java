package corpus.merge;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import corpus.AnnotatedText;
import corpus.Annotator;
import corpus.Text;
import corpus.TextUnit;
import corpus.Unit;
import corpus.alignment.Alignment;
import corpus.alignment.dissimilarity.NominalFeatureTextDissimilarity;
import corpus.merge.AnnotatedTextMerge;

public class AnnotatedTextMergeTest {

	@Test
	public void testMergeAnnotatedTextsWithSegmentation() {
		
		char openUnit = '{';
		char closeUnit = '}';
		char gap = '_';

		// Case 1: identical texts
		List<Unit> annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,4,null,"kauf"));
		annots1.add(new TextUnit(new Annotator("A"), 4,8,null, "mann"));
		AnnotatedText text1 = new AnnotatedText(new Text("kaufmann"), annots1);
		
		List<Unit> annots2 = new ArrayList<Unit>(2);
		annots2.add(new TextUnit(new Annotator("B"), 0,4,null,"kauf"));
		annots2.add(new TextUnit(new Annotator("B"), 4,8,null,"mann"));
		AnnotatedText text2 = new AnnotatedText(new Text("kaufmann"), annots2);

		Set<Alignment> harm = 
				AnnotatedTextMerge.mergeAnnotatedTextsWithSegmentation(text1, text2, openUnit, closeUnit, gap);

		assertEquals(1, harm.size());
		Alignment al = harm.toArray(new Alignment[0])[0];
		assertEquals(0, al.getDisorder(new NominalFeatureTextDissimilarity()), 0);

		// Case 2: Alignable but different TextUnits
		annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,5,null,"kauff"));
		annots1.add(new TextUnit(new Annotator("A"), 5,9,null, "mann"));
		text1 = new AnnotatedText(new Text("kauffmann"), annots1);
		
		annots2 = new ArrayList<Unit>(2);
		annots2.add(new TextUnit(new Annotator("B"), 0,4,null,"kauf"));
		annots2.add(new TextUnit(new Annotator("B"), 4,9,null,"mmann"));
		text2 = new AnnotatedText(new Text("kaufmmann"), annots2);

		harm = 
				AnnotatedTextMerge.mergeAnnotatedTextsWithSegmentation(text1, text2, openUnit, closeUnit, gap);

		assertEquals(1, harm.size());
		al = harm.toArray(new Alignment[0])[0];
		assertEquals(1, al.getDisorder(new NominalFeatureTextDissimilarity()), 0);

		// Case 3: segmentation completely different
		annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,1,null,"k"));
		annots1.add(new TextUnit(new Annotator("A"), 1,8,null, "aufmann"));
		text1 = new AnnotatedText(new Text("kaufmann"), annots1);

		annots2 = new ArrayList<Unit>(2);
		annots2.add(new TextUnit(new Annotator("B"), 0,4,null,"kauf"));
		annots2.add(new TextUnit(new Annotator("B"), 4,8,null,"mann"));
		text2 = new AnnotatedText(new Text("kaufmann"), annots2);

		harm = 
				AnnotatedTextMerge.mergeAnnotatedTextsWithSegmentation(text1, text2, openUnit, closeUnit, gap);

		assertEquals(1, harm.size());
		al = harm.toArray(new Alignment[0])[0];
		assertEquals(2, al.getDisorder(new NominalFeatureTextDissimilarity()), 0);
		
		// Case 4: two possible alignments
		annots1 = new ArrayList<Unit>(2);
		annots1.add(new TextUnit(new Annotator("A"), 0,5,null,"a"));
		annots1.add(new TextUnit(new Annotator("A"), 6,11,null, "b"));
		text1 = new AnnotatedText(new Text("sonst sonst"), annots1);

		annots2 = new ArrayList<Unit>(2);
		annots2.add(new TextUnit(new Annotator("B"), 0,5,null,"a"));
		text2 = new AnnotatedText(new Text("sonst"), annots2);

		harm = 
				AnnotatedTextMerge.mergeAnnotatedTextsWithSegmentation(text1, text2, openUnit, closeUnit, gap);

		assertEquals(2, harm.size());
		
//		for (int i=0; i < harm.size(); i++) {
//				System.out.println(harm.toArray(new Alignment[0])[i]);
//				System.out.println(harm.toArray(new Alignment[0])[i].getDisorder(new SimpleTextUnitDissimilarity()));
//		}
		
		// Case 5: three possible alignments
		annots1 = new ArrayList<Unit>(3);
		annots1.add(new TextUnit(new Annotator("A"), 0,2,null,"a"));
		annots1.add(new TextUnit(new Annotator("A"), 3,5,null, "b"));
		annots1.add(new TextUnit(new Annotator("A"), 6,8,null, "c"));
		text1 = new AnnotatedText(new Text("so so so"), annots1);

		annots2 = new ArrayList<Unit>(2);
		annots2.add(new TextUnit(new Annotator("B"), 0,2,null,"b"));
		annots2.add(new TextUnit(new Annotator("B"), 3,5,null,"c"));
		text2 = new AnnotatedText(new Text("so so"), annots2);

		harm = 
				AnnotatedTextMerge.mergeAnnotatedTextsWithSegmentation(text1, text2, openUnit, closeUnit, gap);

		assertEquals(3, harm.size());
		
//		for (int i=0; i < harm.size(); i++) {
//				System.out.println(harm.toArray(new Alignment[0])[i]);
//				System.out.println(harm.toArray(new Alignment[0])[i].getDisorder(new SimpleTextUnitDissimilarity()));
//		}
	}

}
