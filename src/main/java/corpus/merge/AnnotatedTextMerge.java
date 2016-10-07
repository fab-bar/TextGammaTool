package corpus.merge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import corpus.AnnotatedText;
import corpus.AnnotationSet;
import corpus.Annotator;
import corpus.Unit;
import corpus.alignment.Alignment;
import corpus.alignment.UnitaryAlignment;
import corpus.merge.textalignment.PairwiseDPTextAlignment;
import corpus.merge.textalignment.TextAlignment;

public class AnnotatedTextMerge {
	
	/* 
	 * merges two AnnotatedText's using Needleman-Wunsch'-
	 * the annotations are not allowed to overlap
	 * returns a set of all possible segmentations of the resulting "Text"
	 * 
	 */
	public static Set<Alignment> mergeAnnotatedTextsWithSegmentation(AnnotatedText t1, AnnotatedText t2, char openUnit, char closeUnit, char gap) {
		return AnnotatedTextMerge.mergeAnnotatedTextsWithSegmentation(t1, t2, openUnit, closeUnit, gap, -1);
	}

	public static Set<Alignment> mergeAnnotatedTextsWithSegmentation(AnnotatedText t1, AnnotatedText t2, char openUnit, char closeUnit, char gap, int levenshtein_threshold) {
		
		// assure that the characters denoting beginning and end of units and gaps differ from each other
		if (openUnit == closeUnit) {
			throw new IllegalArgumentException("Characters denoting the start and the end of a unit must differ.");
		}
		if (openUnit == gap) {
			throw new IllegalArgumentException("Characters denoting the start of a unit and a gap must differ.");
		}
		if (closeUnit == gap) {
			throw new IllegalArgumentException("Characters denoting the end of a unit and a gap must differ.");
		}
		
		// assure that the characters denoting beginning and end of units and gaps do not appear in the text
		if (t1.getText().getTextualContent().indexOf(openUnit) != -1 ) {
			throw new IllegalArgumentException("The character denoting the start of a unit may not appear in the text.");			
		}
		else if (t1.getText().getTextualContent().indexOf(closeUnit) != -1 ) {
			throw new IllegalArgumentException("The character denoting the end of a unit may not appear in the text.");
		}
		else if (t1.getText().getTextualContent().indexOf(gap) != -1 ) {
			throw new IllegalArgumentException("The character denoting a gap may not appear in the text.");
		}

		StringBuffer text1 = new StringBuffer(t1.getText().getTextualContent());
		StringBuffer text2 = new StringBuffer(t2.getText().getTextualContent());
		
		Unit[] a1 = t1.getAnnotations();
		Unit[] a2 = t2.getAnnotations();
		
		text1 = insertAnnotationsInText(text1, a1, openUnit, closeUnit);
		text2 = insertAnnotationsInText(text2, a2, openUnit, closeUnit);
		
		// align
		TextAlignment nw = new PairwiseDPTextAlignment(text1.toString().toCharArray(),
				                                 text2.toString().toCharArray(),
				                                 gap, openUnit, closeUnit);
		
		// get the alignment cost and throw an Exception if cost is above a threshold
		if (levenshtein_threshold > -1) {
			int lev = nw.getInsertions() + nw.getDeletions() + nw.getSubstitutions();
			if (lev > levenshtein_threshold)
				throw new IllegalArgumentException("The texts are more different than given threshold.");
		}
		
		List<String[]> alignments = nw.getAlignments();
		
		// create annotation sets with the new annotations  
	    Set<List<Unit>> ret = new HashSet<List<Unit>>();
	    int best_overlap = 0;
	    
	    for (String[] alignment: alignments) {
	    	
			List<Unit[]> annot_list = new ArrayList<Unit[]>(2);
			annot_list.add(a1);
			annot_list.add(a2);
			
			List<Unit[]> aligned_annots = getUnitsFromText(Arrays.asList(alignment), annot_list, openUnit, closeUnit); 
	    	Unit[] units1 = aligned_annots.get(0);
	    	Unit[] units2 = aligned_annots.get(1);

	    	int overlap = 0;
	    	for (Unit u1: units1) {
	    		for (Unit u2: units2) {
	    			if (u1.getBegin() == u2.getBegin() && u1.getEnd() == u2.getEnd())
	    				overlap++;
	    		}
	    	}
	    	
	    	// add only those with the best overlap
	    	if (overlap >= best_overlap) {
	    		// found an alignment with better overlap than all alignments before
	    		if (overlap > best_overlap) {
	    			ret.clear();
	    			best_overlap = overlap;
	    		}
	    	
	    		List<Unit> annotset = new ArrayList<Unit>(units1.length + units2.length);
	    		annotset.addAll(Arrays.asList(units1));
	    		annotset.addAll(Arrays.asList(units2));
	    	
	    		ret.add(annotset);
	    	}
	    }
	    
	    // create alignments
	    Set<Alignment> alignment_set = new HashSet<Alignment>();
	    for (List<Unit> annotations: ret) {
	    	Set<UnitaryAlignment> uas = new HashSet<UnitaryAlignment>();  
	    	AnnotationSet annoset = new AnnotationSet(annotations);
	    	List<Unit> alignlist = new ArrayList<Unit>(annoset.getNumberOfAnnotators());
	    	Unit last_unit = null;
	    	
	    	for (Unit u: annoset.getAnnotations()) {
	    		if (last_unit != null && !(u.getBegin() == last_unit.getBegin() && u.getEnd() == last_unit.getEnd())) {
	    			uas.add(new UnitaryAlignment(alignlist, new HashSet<Annotator>(Arrays.asList(annoset.getAnnotators()))));
	    			alignlist.clear();	    				
	    		}
	    		alignlist.add(u);
    			last_unit = u;
	    	}
	    	if (!alignlist.isEmpty())
	    		uas.add(new UnitaryAlignment(alignlist, new HashSet<Annotator>(Arrays.asList(annoset.getAnnotators()))));
	    	
	    	alignment_set.add(new Alignment(uas, annoset));
	    }
		
		return alignment_set;

	}
	
	private static StringBuffer insertAnnotationsInText(StringBuffer text1, Unit[] annotations, char openUnit, char closeUnit) {
		int pos = 0;
		int offset = 0;
		for (Unit annot: annotations) {
			if (annot.getBegin() + offset < pos) {
				// units overlap
				throw new IllegalArgumentException("Text contains overlapping units.");
			}
			text1.insert(annot.getBegin() + offset, openUnit);
			offset = offset + 2;
			pos = annot.getEnd() + offset; 
			text1.insert(pos-1, closeUnit);
		}
		
		return text1;
	}
	
	private static List<Unit[]> getUnitsFromText(List<String> texts, List<Unit[]> annotations, char openUnit, char closeUnit) {
		
		if (texts.isEmpty())
			return null;
		
		if (texts.size() != annotations.size()) {
			// number of texts has to be equal to the number of annotation lists 
			throw new IllegalArgumentException("Numbers of annotation lists and texts differ.");			
		}
		int text_length = texts.get(0).length();
		for (String text: texts)
			if (text.length() != text_length)
				throw new IllegalArgumentException("Texts have to be of the same length.");
	
		List<Unit[]> new_annotations = new ArrayList<Unit[]>(annotations.size());
		List<Integer> current_begins = new ArrayList<Integer>(annotations.size());
		List<Integer> annot_numbers = new ArrayList<Integer>(annotations.size());
		for (int i = 0; i < annotations.size(); i++) {
			new_annotations.add(new Unit[annotations.get(i).length]);
			current_begins.add(0);
			annot_numbers.add(0);
		}
		
		int offset = 0;
		for (int i = 0; i < texts.get(0).length(); i++) {
			
			// only change offset once per position
			boolean offset_change = false;
			
			for (int j = 0; j < texts.size(); j++) {
				String text = texts.get(j);
				if (text.charAt(i) == openUnit) {
					current_begins.set(j, i-offset);
					offset_change = true;
				}
				else if (text.charAt(i) == closeUnit) {
					new_annotations.get(j)[annot_numbers.get(j)] = 
							annotations.get(j)[annot_numbers.get(j)].cloneWithDifferentOffsets(
									current_begins.get(j), i-offset);

					annot_numbers.set(j, annot_numbers.get(j)+1);
					offset_change = true;
				}
			}
			
			if (offset_change)
				offset += 1;
		}
		return new_annotations;
	}
	

}
