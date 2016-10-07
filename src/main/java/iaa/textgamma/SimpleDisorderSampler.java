package iaa.textgamma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import corpus.AnnotatedText;
import corpus.AnnotationSet;
import corpus.Annotator;
import corpus.Unit;
import corpus.alignment.dissimilarity.Dissimilarity;
import corpus.shuffling.AnnotationSetShuffle;
import corpus.shuffling.AnnotationSetShuffle.SegmentationChangetype;
import corpus.shuffling.AnnotationSetShuffle.TextChangetype;
import iaa.TextGamma;

public class SimpleDisorderSampler extends DisorderSampler {

	private AnnotatedText text;
	private Dissimilarity d;
	private char openUnit;
	private char closeUnit;
	private char gap;

	private BinomialDistribution text_changes;
	private EnumeratedDistribution<TextChangetype> changeChooserText;
	private EnumeratedDistribution<Character> characterGenerator;

	private BinomialDistribution seg_changes;
	private EnumeratedDistribution<SegmentationChangetype> changeChooserSeg;

	private Map<String, EnumeratedDistribution<String>> labelGenerators;

  private void initialize(AnnotatedText text, char openUnit, char closeUnit, char gap,
                                Dissimilarity d, 
                                double text_changes, EnumeratedDistribution<Character> characterGenerator, 
                                double seg_changes,
                                Map<String, EnumeratedDistribution<String>> labelGenerators) {
      this.text = text;
      this.openUnit = openUnit;
      this.closeUnit = closeUnit;
      this.gap = gap;

      this.d = d;

      this.text_changes = new BinomialDistribution(text.getNumberOfAnnotations(), text_changes);
      this.characterGenerator = characterGenerator;

      this.seg_changes = new BinomialDistribution(text.getNumberOfAnnotations(), seg_changes);
      
      this.labelGenerators = labelGenerators;
  }

  // creates a SimpleDisorderSampler with uniform distribution over change types
  public SimpleDisorderSampler(AnnotatedText text, char openUnit, char closeUnit, char gap,
		  Dissimilarity d, 
		  double text_changes, EnumeratedDistribution<Character> characterGenerator,
		  double seg_changes,
		  Map<String,EnumeratedDistribution<String>> labelGenerators) {

	  this.initialize(text, openUnit, closeUnit, gap, d, text_changes, characterGenerator, seg_changes, labelGenerators);
	  
	  // set probabilities of change types to equal probabilities
	  double prop = 1/(double)TextChangetype.values().length;
	  List<Pair<TextChangetype, Double>> pt = new ArrayList<Pair<TextChangetype, Double>>(TextChangetype.values().length);
	  for (TextChangetype type: TextChangetype.values())
		  pt.add(new Pair<TextChangetype,Double>(type, prop));
	  this.changeChooserText = new EnumeratedDistribution<TextChangetype>(pt);
		
	  prop = 1/(double)SegmentationChangetype.values().length;
	  List<Pair<SegmentationChangetype, Double>> ps = new ArrayList<Pair<SegmentationChangetype, Double>>(SegmentationChangetype.values().length);
	  for (SegmentationChangetype type: SegmentationChangetype.values())
		  ps.add(new Pair<SegmentationChangetype,Double>(type, prop));
	  this.changeChooserSeg = new EnumeratedDistribution<SegmentationChangetype>(ps);



}

	public SimpleDisorderSampler(AnnotatedText text, char openUnit, char closeUnit, char gap,
			Dissimilarity d,
			double text_changes, EnumeratedDistribution<TextChangetype> changeChooserText, EnumeratedDistribution<Character> characterGenerator,
			double seg_changes, EnumeratedDistribution<SegmentationChangetype> changeChooserSeg,
			Map<String,EnumeratedDistribution<String>> labelGenerators) {

		this.initialize(text, openUnit, closeUnit, gap, d, text_changes, characterGenerator, seg_changes, labelGenerators);

		this.changeChooserText = changeChooserText;
		this.changeChooserSeg = changeChooserSeg;
	}


	@Override
	public Double sampleDisorder() {

		// 1. apply textual changes
		AnnotatedText vers1 = AnnotationSetShuffle.changeText(text, this.text_changes.sample(), this.changeChooserText, this.characterGenerator);
		AnnotatedText vers2 = AnnotationSetShuffle.changeText(text, this.text_changes.sample(), this.changeChooserText, this.characterGenerator);

		// 2. apply segmentation changes
		AnnotationSet set1 = AnnotationSetShuffle.changeSegmentation(new AnnotationSet(Arrays.asList(vers1.getAnnotations("textunit"))),
				this.seg_changes.sample(), this.changeChooserSeg);
		AnnotationSet set2 = AnnotationSetShuffle.changeSegmentation(new AnnotationSet(Arrays.asList(vers2.getAnnotations("textunit"))),
				this.seg_changes.sample(), this.changeChooserSeg);

		// 3. apply categorization changes
		for (String attribute: this.text.getAttributes()) {
			EnumeratedDistribution<String> labelGenerator = labelGenerators.get(attribute);
			if (labelGenerator == null)
				throw new IllegalArgumentException("No generator for attribute " + attribute + " given.");
			set1 = AnnotationSetShuffle.randomizeAttributeValues(set1, attribute, labelGenerator);
			set2 = AnnotationSetShuffle.randomizeAttributeValues(set2, attribute, labelGenerator);
		}

		// change the annotators
		Annotator a = new Annotator("A");
		Annotator b = new Annotator("B");

		Unit[] set1_arr = set1.getAnnotations();
		Unit[] set2_arr = set2.getAnnotations();

		for (int i = 0; i < set1_arr.length; i++) {
			set1_arr[i] = set1_arr[i].cloneWithDifferentCreator(a);
		}
		for (int i = 0; i < set2_arr.length; i++) {
			set2_arr[i] = set2_arr[i].cloneWithDifferentCreator(b);
		}

		return TextGamma.getObservedDisorder(new AnnotatedText(vers1.getText(), Arrays.asList(set1_arr)), 
				new AnnotatedText(vers2.getText(), Arrays.asList(set2_arr)), 
				d, openUnit, closeUnit, gap);
	}
}
