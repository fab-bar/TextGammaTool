package ui;

import java.math.BigDecimal;
import java.util.Arrays;

import corpus.AnnotatedText;
import corpus.Annotator;
import corpus.Unit;
import corpus.merge.AnnotatedTextMerge;

public class EvaluateNumberOfAlignments {
	
	private static AnnotatedText setCreator(AnnotatedText text, String creator) {
		
		// change the annotators
		Annotator a = new Annotator(creator);
		Unit[] orig_set = text.getAnnotations();
		for (int i = 0; i < orig_set.length; i++) {
			orig_set[i] = orig_set[i].cloneWithDifferentCreator(a);
		}
		
		return new AnnotatedText(text.getText(), Arrays.asList(orig_set));
		
	}

	public static void main(String[] args) {

		AnnotatedText text = TextGammaEvaluation.loadFileFromCommandlineArguments(args);

		char openUnit = args[3].charAt(0);
		char closeUnit = args[4].charAt(0);
		char gap = args[5].charAt(0);
		
		CompleteShuffler shuffler = new CompleteShuffler(new BigDecimal(0.5), new BigDecimal(0.1), new BigDecimal(1));
		AnnotatedText changed = setCreator(text, "B");
		changed = shuffler.changeText(changed, new BigDecimal(1));
		
		System.out.println(AnnotatedTextMerge.mergeAnnotatedTextsWithSegmentation(text, changed, openUnit, closeUnit, gap).size());

	}

}
