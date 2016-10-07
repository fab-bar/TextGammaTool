package ui;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import corpus.AnnotatedText;
import corpus.Unit;

public class CreateDataForGammaSoftware {

	public static void printDataForGammaEvaluation(AnnotatedText text, BigDecimal seg_factor, BigDecimal cat_factor, BigDecimal step_size) throws FileNotFoundException {
		printDataForGammaEvaluation(text, new CategoryAndSegmentationShuffler(seg_factor, cat_factor), step_size);
	}
	public static void printDataForGammaEvaluation(AnnotatedText text, CategoryAndSegmentationShuffler shuffler, BigDecimal step_size) throws FileNotFoundException {
		Unit[] orig_annots = text.getAnnotations();
		for (BigDecimal effect= new BigDecimal(0); effect.doubleValue()<=1; effect = effect.add(step_size)) {
			
			PrintWriter pw = new PrintWriter("shuffled" + effect.toString());
			
			AnnotatedText changed = shuffler.changeText(text, effect);
			Unit[] chgd_annots = changed.getAnnotations();
			
			for(int i=0; i < orig_annots.length; i++) {
				pw.println("A," +
								   orig_annots[i].getAttributeValue("pos") + "," +
							       orig_annots[i].getBegin() + "," + 
							       orig_annots[i].getEnd());	
			}
			for(int i=0; i < chgd_annots.length; i++) {
				pw.println("B," +
							       chgd_annots[i].getAttributeValue("pos") + "," +
							       chgd_annots[i].getBegin() + "," + 
							       chgd_annots[i].getEnd());
			}
			
			pw.close();

		}
	}
	
	public static void main(String[] args) {

		AnnotatedText text = TextGammaEvaluation.loadFileFromCommandlineArguments(args);
		try {
			printDataForGammaEvaluation(text, new BigDecimal(0.1), new BigDecimal(1), new BigDecimal(0.05));
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}