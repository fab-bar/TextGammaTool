package ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import corpus.AnnotatedText;
import corpus.AnnotationSet;
import corpus.Annotator;
import corpus.TextUnit;
import corpus.Unit;
import corpus.alignment.dissimilarity.Dissimilarity;
import corpus.alignment.dissimilarity.NominalFeatureTextDissimilarity;
import corpus.shuffling.AnnotationSetShuffle;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import org.dkpro.statistics.agreement.distance.NominalDistanceFunction;
import org.dkpro.statistics.agreement.unitizing.KrippendorffAlphaUnitizingAgreement;
import org.dkpro.statistics.agreement.unitizing.UnitizingAnnotationStudy;
import iaa.TextGamma;
import iaa.textgamma.DisorderSampler;
import iaa.textgamma.SimpleDisorderSampler;
import io.AnnotatedTextFileReader;
import io.AnnotatedTextFileReaderFactory;
import io.AnnotatedTextFileReaderStaticFactory;

public class TextGammaEvaluation {
	
	private static AnnotatedText setCreator(AnnotatedText text, String creator) {
		
		// change the annotators
		Annotator a = new Annotator(creator);
		Unit[] orig_set = text.getAnnotations();
		for (int i = 0; i < orig_set.length; i++) {
			orig_set[i] = orig_set[i].cloneWithDifferentCreator(a);
		}
		
		return new AnnotatedText(text.getText(), Arrays.asList(orig_set));
		
	}
	
	private static DisorderSampler createSampler(AnnotatedText orig, List<AnnotatedText> texts,
			double expected_text_change, double expected_seg_change,
			char openUnit, char closeUnit, char gap, Dissimilarity d) {

		// estimate character and label distribution
		ArrayList<Unit> units = new ArrayList<Unit> (orig.getNumberOfAnnotations()*texts.size());
		for (AnnotatedText text: texts)
			units.addAll(Arrays.asList(text.getAnnotations()));

		// get distribution of characters
		int numChars = 0;
		HashMap<Character, Integer> characters = new HashMap<Character, Integer>();
		for (Unit u: units)
			if (u instanceof TextUnit)
				for (char c: ((TextUnit) u).getText().toCharArray()) {
					numChars += 1;
					if (characters.containsKey(c))
						characters.put(c, characters.get(c) + 1);
					else
						characters.put(c, 1);
				}
		List<Pair<Character, Double>> pc = new ArrayList<Pair<Character, Double>>(characters.size());
		for (Character c: characters.keySet()) {
			pc.add(new Pair<Character,Double>(c, characters.get(c)/(double)numChars));
		}
		EnumeratedDistribution<Character> characterGenerator = new EnumeratedDistribution<Character>(pc);

		// get distribution of labels
		Set<String> attributes = new HashSet<String>();
		for (AnnotatedText text: texts)
			attributes.addAll(Arrays.asList(text.getAttributes()));
		Map<String,EnumeratedDistribution<String>> labelGenerators = new HashMap<String,EnumeratedDistribution<String>>();
		HashMap<String, Integer> labels = new HashMap<String, Integer>();
		for (String attribute: attributes) {
			for (Unit u: units) {
				String label = u.getAttributeValue(attribute);
				if (labels.containsKey(u.getAttributeValue(attribute)))
					labels.put(label, labels.get(label) + 1);
				else
					labels.put(label, 1);
			}
			List<Pair<String, Double>> pl = new ArrayList<Pair<String, Double>>(labels.size());
			for (String label: labels.keySet()) {
				pl.add(new Pair<String,Double>(label, labels.get(label)/(double)units.size()));
			}
			EnumeratedDistribution<String> labelGenerator = new EnumeratedDistribution<String>(pl);
		
			labelGenerators.put(attribute, labelGenerator);
		}

		
		return new SimpleDisorderSampler(orig, openUnit, closeUnit, gap, d,
				expected_text_change, characterGenerator,
				expected_seg_change,
				labelGenerators);
	}
	
	public static void evaluate(AnnotatedText base, AnnotatedText text, AnnotatedTextShuffler shuffler,
			double expected_text_change, double expected_seg_change,
			Dissimilarity d, char openUnit, char closeUnit, char gap, BigDecimal step_size) {

		AnnotatedText orig = setCreator(text, "A");
		AnnotatedText comp = setCreator(text, "B");
		
		List<AnnotatedText> texts = new ArrayList<AnnotatedText>(2);
		texts.add(orig);
		texts.add(comp);

		for (BigDecimal effect= new BigDecimal(0); effect.doubleValue()<=1; effect = effect.add(step_size)) {
			
			AnnotatedText changed = shuffler.changeText(comp, effect);
			texts.set(1, changed);
			
			double observedDisorder = TextGamma.getObservedDisorder(orig, changed, d, openUnit, closeUnit, gap);	
			double expectedDisorder = TextGamma.getExpectedDisorder(createSampler(base, texts,
					expected_text_change, expected_seg_change, openUnit, closeUnit, gap, d), 
					(float)0.01, (float)0.05);
			
			System.out.println(shuffler.getChangeType() + "\t" +
					effect.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "\t" + 
					observedDisorder + "\t" +
					expectedDisorder + "\t" + expected_text_change + "\t" + expected_seg_change + "\t" +
					(1-observedDisorder/expectedDisorder));
		}

		
	}

	public static void evaluate(AnnotatedText base, AnnotatedText text, AnnotatedTextShuffler shuffler,
			List<Double> expected_text_changes, List<Double> expected_seg_changes,
			Dissimilarity d, char openUnit, char closeUnit, char gap, BigDecimal step_size) {
		Iterator<Double> expt_it = expected_text_changes.iterator();
		Iterator<Double> exps_it = expected_seg_changes.iterator();

		while (expt_it.hasNext() && exps_it.hasNext()) {
			double expected_text_change = expt_it.next();
			double expected_seg_change = exps_it.next();
			evaluate(base, text, shuffler, expected_text_change, expected_seg_change, d, openUnit, closeUnit, gap, step_size);
		}

	}
	
	public static void evaluateCategorizationAlpha(AnnotatedText text, CategoryShuffler shuffler, BigDecimal step_size) {

		Unit[] orig_annots = text.getAnnotations();
		for (BigDecimal effect= new BigDecimal(0); effect.doubleValue()<=1; effect = effect.add(step_size)) {
			
			AnnotatedText changed = shuffler.changeText(text, effect);
			Unit[] chgd_annots = changed.getAnnotations();

			CodingAnnotationStudy study = new CodingAnnotationStudy(2);
			for(int i=0; i < orig_annots.length; i++) {
				Set<String> attribs = new HashSet<String>();
				attribs.addAll(orig_annots[i].getAttributes());
				attribs.addAll(chgd_annots[i].getAttributes());
				for (String attr: attribs)
					study.addItem(orig_annots[i].getAttributeValue(attr), chgd_annots[i].getAttributeValue(attr));	
			}

			KrippendorffAlphaAgreement alpha = new KrippendorffAlphaAgreement(
					study, new NominalDistanceFunction());

			System.out.println(shuffler.getChangeType() + "\t" +
							effect.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "\t" + 
							alpha.calculateObservedDisagreement() + "\t" +
							alpha.calculateExpectedDisagreement() + "\t" + 
							"NA\tNA\t" +
							alpha.calculateAgreement());
		}
	}

	private static void addAnnotationsToUnitizingAnnotationStudy(UnitizingAnnotationStudy study, Unit[] annots, int annotator) {
		for(int i=0; i < annots.length; i++) {
			for (String attr: annots[i].getAttributes()) {
				study.addUnit(annots[i].getBegin(), annots[i].getEnd() - annots[i].getBegin(),
						annotator, annots[i].getAttributeValue(attr));
			}
		}		
	}
	
	public static void evaluateCategorizationAndSegmentationAlpha(AnnotatedText text, CategoryAndSegmentationShuffler shuffler, BigDecimal step_size) {
		Unit[] orig_annots = text.getAnnotations();
		for (BigDecimal effect= new BigDecimal(0); effect.doubleValue()<=1; effect = effect.add(step_size)) {

			AnnotatedText changed = shuffler.changeText(text, effect);
			Unit[] chgd_annots = changed.getAnnotations();

			UnitizingAnnotationStudy study = new UnitizingAnnotationStudy(2, text.getText().getLength());
			addAnnotationsToUnitizingAnnotationStudy(study, orig_annots, 0);
			addAnnotationsToUnitizingAnnotationStudy(study, chgd_annots, 1);

			KrippendorffAlphaUnitizingAgreement alpha = new KrippendorffAlphaUnitizingAgreement(study);

			System.out.println(shuffler.getChangeType() + "\t" +
							effect.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "\t" + 
							"NA\t" +
							"NA\t" + 
							"NA\tNA\t" +
							alpha.calculateAgreement());
		}
		
	}

	public static void evaluateCategorization(AnnotatedText base, AnnotatedText text,
			List<Double> expected_text_changes, List<Double> expected_seg_changes,
			Dissimilarity d, char openUnit, char closeUnit, char gap, BigDecimal step_size) {
		evaluateCategorizationAlpha(text, new CategoryShuffler(), step_size);
		evaluate(base, text, new CategoryShuffler(), expected_text_changes, expected_seg_changes, new NominalFeatureTextDissimilarity(), '«', '»', '―', new BigDecimal(0.05));
	}

	public static void evaluateCategorizationAndSegmentation(AnnotatedText base, AnnotatedText text,
			BigDecimal seg_factor, BigDecimal cat_factor,
			List<Double> expected_text_changes, List<Double> expected_seg_changes,
			Dissimilarity d, char openUnit, char closeUnit, char gap, BigDecimal step_size) {
		evaluateCategorizationAndSegmentationAlpha(text, new CategoryAndSegmentationShuffler(seg_factor, cat_factor), step_size);
		evaluate(base, text, new CategoryAndSegmentationShuffler(seg_factor, cat_factor), expected_text_changes, expected_seg_changes, new NominalFeatureTextDissimilarity(), '«', '»', '―', new BigDecimal(0.05));
	}
	
	
	public static AnnotatedText loadFileFromCommandlineArguments(String[] args) {

		AnnotatedText text = null;
		try {
			AnnotatedTextFileReaderFactory reader_fact = AnnotatedTextFileReaderStaticFactory.createReaderFactory(args[0]);
			Map<String, String> options = new HashMap<String, String>();
			if (!args[1].equals("_")) {
				for(String par: args[1].split(",")) {
					String[] pair = par.split("=");
					options.put(pair[0], pair[1]);
				}
			}
			AnnotatedTextFileReader reader = reader_fact.createAnnotatedTextFileReader(options);
			text = reader.readFile(args[2], new Annotator("a"));
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return text;
		
	}

	public static void main(String[] args) {

		AnnotatedText text = loadFileFromCommandlineArguments(args);

		char openUnit = args[3].charAt(0);
		char closeUnit = args[4].charAt(0);
		char gap = args[5].charAt(0);

		// simulate base text
		AnnotatedText base = new CompleteShuffler(new BigDecimal(0.5), new BigDecimal(0.1), new BigDecimal(1)).changeText(text, new BigDecimal(0.25));

		List<Double> expected_text_changes = new ArrayList<Double>(Arrays.asList(new Double[] {0.25, 0.0, 0.0}));
		List<Double> expected_seg_changes  = new ArrayList<Double>(Arrays.asList(new Double[] {0.04, 0.04, 0.0}));

		evaluateCategorization(base, text, expected_text_changes, expected_seg_changes, new NominalFeatureTextDissimilarity(), openUnit, closeUnit, gap, new BigDecimal(0.05));
		evaluateCategorizationAndSegmentation(base, text, new BigDecimal(0.1), new BigDecimal(1), expected_text_changes, expected_seg_changes, new NominalFeatureTextDissimilarity(), openUnit, closeUnit, gap, new BigDecimal(0.05));
		evaluate(base, text, new CompleteShuffler(new BigDecimal(0.5), new BigDecimal(0.1), new BigDecimal(1)), expected_text_changes, expected_seg_changes, new NominalFeatureTextDissimilarity(), openUnit, closeUnit, gap, new BigDecimal(0.05));
		
	}

}

abstract class AnnotatedTextShuffler {
	public abstract String getChangeType();
	public abstract AnnotatedText changeText(AnnotatedText orig, BigDecimal effect);
}

class CategoryShuffler extends AnnotatedTextShuffler{
	
	@Override
	public String getChangeType() {
		return "Category";
	}

	@Override
	public AnnotatedText changeText(AnnotatedText orig, BigDecimal effect) {
		
		AnnotationSet set1 = new AnnotationSet(Arrays.asList(orig.getAnnotations("textunit")));
		// apply categorization changes
		for (String attribute: set1.getAttributes()) {
			set1 = AnnotationSetShuffle.shuffleAttributeValues(set1, attribute, effect.doubleValue());
		}
		
		return new AnnotatedText(orig.getText(), Arrays.asList(set1.getAnnotations()));
		
	}

}

class SegmentationShuffler extends AnnotatedTextShuffler{
	
	@Override
	public String getChangeType() {
		return "Segmentation";
	}

	@Override
	public AnnotatedText changeText(AnnotatedText orig, BigDecimal effect) {
	
		// apply segmentation changes
		AnnotationSet set1 = AnnotationSetShuffle.shuffleSegmentation(new AnnotationSet(Arrays.asList(orig.getAnnotations("textunit"))), effect.doubleValue());

		return new AnnotatedText(orig.getText(), Arrays.asList(set1.getAnnotations())); 
	
	}

}

class TextShuffler extends AnnotatedTextShuffler{
	
	@Override
	public String getChangeType() {
		return "Text";
	}

	@Override
	public AnnotatedText changeText(AnnotatedText orig, BigDecimal effect) {		
		return AnnotationSetShuffle.shuffleText(orig, effect.doubleValue());
	}
	
}

class TextAndCategoryShuffler extends AnnotatedTextShuffler{
	
	@Override
	public String getChangeType() {
		return "Text-Category";
	}

	@Override
	public AnnotatedText changeText(AnnotatedText orig, BigDecimal effect) {

		return new CategoryShuffler().changeText(
				new TextShuffler().changeText(orig, effect), effect);
		
	}
	
}

class CategoryAndSegmentationShuffler extends AnnotatedTextShuffler{

	BigDecimal seg_factor;
	BigDecimal cat_factor;

	public CategoryAndSegmentationShuffler(BigDecimal seg_factor, BigDecimal cat_factor) {
		this.seg_factor = seg_factor;
		this.cat_factor = cat_factor;
	}


	@Override
	public String getChangeType() {
		return "Segmentation-Category," +
		seg_factor.setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "," +
		cat_factor.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
	}

	@Override
	public AnnotatedText changeText(AnnotatedText orig, BigDecimal effect) {

		return new CategoryShuffler().changeText(
				new SegmentationShuffler().changeText(
						orig, effect.multiply(seg_factor)),
				effect.multiply(cat_factor));
	}
}

class CompleteShuffler extends AnnotatedTextShuffler {
	
	BigDecimal text_factor;
	BigDecimal seg_factor;
	BigDecimal cat_factor;
	
	public CompleteShuffler(BigDecimal text_factor, BigDecimal seg_factor, BigDecimal cat_factor) {
		this.text_factor = text_factor;
		this.seg_factor = seg_factor;
		this.cat_factor = cat_factor;
	}

	@Override
	public String getChangeType() {
		return "Full," + 
				text_factor.setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "," +
				seg_factor.setScale(1, BigDecimal.ROUND_HALF_UP).toString() + "," +
				cat_factor.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
	}

	@Override
	public AnnotatedText changeText(AnnotatedText orig, BigDecimal effect) {
		
		return new CategoryShuffler().changeText(
				new SegmentationShuffler().changeText(
						new TextShuffler().changeText(orig, 
								effect.multiply(text_factor)), 
						effect.multiply(seg_factor)),
				effect.multiply(cat_factor));
	}
	
}
