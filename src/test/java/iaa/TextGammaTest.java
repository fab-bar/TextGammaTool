package iaa;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.junit.Test;

import corpus.AnnotatedText;
import corpus.Annotator;
import corpus.Text;
import corpus.TextUnit;
import corpus.Unit;
import corpus.alignment.dissimilarity.NominalFeatureTextDissimilarity;
import iaa.TextGamma;
import iaa.textgamma.DisorderSampler;

public class TextGammaTest {

	@Test
	public void testGetObservedDisorder() {
		
		char openUnit = '{';
		char closeUnit = '}';
		char gap = '_';

		List<Unit> annots1 = new ArrayList<Unit>(3);
		annots1.add(new TextUnit(new Annotator("A"), 0,2,null,"a"));
		annots1.add(new TextUnit(new Annotator("A"), 3,5,null, "b"));
		annots1.add(new TextUnit(new Annotator("A"), 6,8,null, "c"));
		AnnotatedText text1 = new AnnotatedText(new Text("so so so"), annots1);

		List<Unit> annots2 = new ArrayList<Unit>(2);
		annots2.add(new TextUnit(new Annotator("B"), 0,2,null,"b"));
		annots2.add(new TextUnit(new Annotator("B"), 3,5,null,"c"));
		AnnotatedText text2 = new AnnotatedText(new Text("so so"), annots2);

		assertEquals(0.4, TextGamma.getObservedDisorder(text1, text2, new NominalFeatureTextDissimilarity(), openUnit, closeUnit, gap), 0.001);
	}

	@Test
	public void testgetExpectedDisorderSamplingWithNormalDistribution() {
		
		int precision_correct = 0;
		int too_small = 0;
		int too_big = 0;
		for (int i = 0; i < 100; i++) {
			double eDisorder = TextGamma.getExpectedDisorder(new DisorderSampler() {
				
				NormalDistribution sn = new NormalDistribution(1, 1);

				@Override
				public Double sampleDisorder() {
					double res = sn.sample();
					return res;
				}
				
			}, new Float(0.02), new Float(0.05));

			if (1-0.02 <= eDisorder && eDisorder <= 1+0.02)
				precision_correct += 1;
			else if (eDisorder < 1-0.02)
				too_small += 1;
			else
				too_big += 1;
		}
		// should be about 95%
		assertTrue(precision_correct/((double)too_small+too_big+precision_correct) >= 0.93);
		
	}

}