package corpus.shuffling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import corpus.AnnotatedText;
import corpus.AnnotationSet;
import corpus.Text;
import corpus.TextUnit;
import corpus.Unit;

public class AnnotationSetShuffle {
	
	// 1. Change the text for AnnotatedText
	// Important: the functions changing the text assume a segmentation, i.e. the units may not overlap
	//            this is not tested; if this assumption is violated, some units may not be adapted correctly

	public static AnnotatedText shuffleText(AnnotatedText orig, double m) {
		if (!(0 <= m && m <= 1))
			throw new IllegalArgumentException("Magnitude has to be between 0 and 1");

		int numChanges = new Float(orig.getNumberOfAnnotations()*m).intValue();

		double prop = 1/(double)TextChangetype.values().length;
		List<Pair<TextChangetype, Double>> pt = new ArrayList<Pair<TextChangetype, Double>>(TextChangetype.values().length);
		for (TextChangetype type: TextChangetype.values())
			pt.add(new Pair<TextChangetype,Double>(type, prop));
		EnumeratedDistribution<TextChangetype> changes = new EnumeratedDistribution<TextChangetype>(pt);

		int numChars = 0;
		HashMap<Character, Integer> characters = new HashMap<Character, Integer>();
		for (Unit u: orig.getAnnotations())
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
		EnumeratedDistribution<Character> characterGen = new EnumeratedDistribution<Character>(pc);

		return changeText(orig, numChanges, changes, characterGen);
	}

	public enum TextChangetype
	{
	  INSERTION, DELETION, SUBSTITUTION
	}

	public static AnnotatedText changeText(AnnotatedText orig, int changes,
			EnumeratedDistribution<TextChangetype> changeChooser,
			EnumeratedDistribution<Character> characterGenerator) {
		return AnnotationSetShuffle.changeText(orig, changes, changeChooser, characterGenerator, new Random());
	}
	public static AnnotatedText changeText(AnnotatedText orig, int changes,
			EnumeratedDistribution<TextChangetype> changeChooser,
			EnumeratedDistribution<Character> characterGenerator,
			Random positionChooser) {

		StringBuffer text = new StringBuffer(orig.getText().getTextualContent());
		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));

		Set<Integer> changed_units = new HashSet<Integer>();

		// avoid infinite loop if changes > number of annotations!
		if (changes > annots.size()) {
			changes = annots.size();
		}

		for (int i=0; i < changes; i++) {

			boolean changed = false;
			while (!changed) {
				TextChangetype type = changeChooser.sample();
				if (type.equals(TextChangetype.INSERTION)) {
					changeTextInsertion(text, annots, characterGenerator, positionChooser, changed_units);
					changed = true;
				}
				else if (type.equals(TextChangetype.DELETION))
					// changeTextDeletion(text, annots, positionChooser, changed_units);
					changed = changeTextDeletionWithoutUnit(text, annots, positionChooser, changed_units);
				else if (type.equals(TextChangetype.SUBSTITUTION)) {
					changeTextSubstitution(text, annots, characterGenerator, positionChooser, changed_units);
					changed = true;
				}
			}

		}

		return new AnnotatedText(new Text(text.toString()), annots);
	}
	
	private static int pickUnitToChange(Random positionGenerator,
			int num_annotations,
			Set<Integer> changed_units) {
		
		// avoid infinite loops if all units are marked as changed
		// should not happen as the number of changes is restricted to the number of units!
		if (changed_units.size() == num_annotations)
			new RuntimeException("All units have been changed; but more changes are needed");

		int unit_offset = positionGenerator.nextInt(num_annotations);
		while(changed_units.contains(unit_offset))
			unit_offset = positionGenerator.nextInt(num_annotations);
		changed_units.add(unit_offset);
		
		return unit_offset;
	}
	
	private static int pickTextOffset(Random positionGenerator, Unit u) {
		// pick a position for the change
		int pos_offset = positionGenerator.nextInt(u.getEnd() - u.getBegin());
		return u.getBegin() + pos_offset;

	}

	public static AnnotatedText changeTextInsertion(AnnotatedText orig,
			EnumeratedDistribution<Character> characterGenerator) {
		return AnnotationSetShuffle.changeTextInsertion(orig, characterGenerator, new Random());
	}
	public static AnnotatedText changeTextInsertion(AnnotatedText orig,
			EnumeratedDistribution<Character> characterGenerator,
			Random positionGenerator) {

		StringBuffer text = new StringBuffer(orig.getText().getTextualContent());
		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));

		AnnotationSetShuffle.changeTextInsertion(text, annots, characterGenerator, positionGenerator, new HashSet<Integer>());

		return new AnnotatedText(new Text(text.toString()), annots);
	}
	private static void changeTextInsertion(StringBuffer orig, List<Unit> annotations,
			EnumeratedDistribution<Character> characterGenerator,
			Random positionGenerator, Set<Integer> changed_units) {

		// pick a unit and position to change
		int unit_offset = pickUnitToChange(positionGenerator, annotations.size(), changed_units);
		Unit u = annotations.get(unit_offset);
		// pick a position for the change
		int pos_offset = positionGenerator.nextInt(u.getEnd() - u.getBegin() + 1);
		int pos = u.getBegin() + pos_offset;

		// update the text
		orig.insert(pos, characterGenerator.sample().charValue());

		// move the end of the unit to the right, change text if textunit
		u = u.cloneWithDifferentOffsets(u.getBegin(), u.getEnd() + 1);
		if (u instanceof TextUnit)
			u = ((TextUnit)u).cloneWithDifferentText(orig.substring(u.getBegin(), u.getEnd()));
		annotations.set(unit_offset, u);

		// move all following units to the right
		for (int i=unit_offset+1 ; i < annotations.size(); i++ ) {
			u = annotations.get(i);
			annotations.set(i, u.cloneWithDifferentOffsets(u.getBegin() + 1, u.getEnd() + 1));
		}

	}

	public static AnnotatedText changeTextDeletion(AnnotatedText orig) {
		return AnnotationSetShuffle.changeTextDeletion(orig, new Random());
	}
	public static AnnotatedText changeTextDeletion(AnnotatedText orig, Random positionGenerator) {

		StringBuffer text = new StringBuffer(orig.getText().getTextualContent());
		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));

		AnnotationSetShuffle.changeTextDeletion(text, annots, positionGenerator, new HashSet<Integer>());

		return new AnnotatedText(new Text(text.toString()), annots);
	}
	private static void changeTextDeletion(StringBuffer orig, List<Unit> annotations,
			Random positionGenerator, Set<Integer> changed_units) {

		// pick a unit and position to change
		int unit_offset = pickUnitToChange(positionGenerator, annotations.size(), changed_units);
		Unit u = annotations.get(unit_offset);
		int pos = pickTextOffset(positionGenerator, u);

		// update the text
		orig.deleteCharAt(pos);

		if (u.getEnd() - u.getBegin() > 1) {
			// move the end of the unit to the left, change text if textunit
			u = u.cloneWithDifferentOffsets(u.getBegin(), u.getEnd() - 1);
			if (u instanceof TextUnit)
				u = ((TextUnit)u).cloneWithDifferentText(orig.substring(u.getBegin(), u.getEnd()));
			annotations.set(unit_offset, u);
		}
		else {
			// remove the unit if it only spans the deleted char
			annotations.remove(unit_offset);
			unit_offset -= 1;
		}

		// move all following units to the left
		for (int i=unit_offset+1 ; i < annotations.size(); i++ ) {
			u = annotations.get(i);
			annotations.set(i, u.cloneWithDifferentOffsets(u.getBegin() - 1, u.getEnd() - 1));
		}
	}
	private static boolean changeTextDeletionWithoutUnit(StringBuffer orig, List<Unit> annotations,
			Random positionGenerator, Set<Integer> changed_units) {

		// pick a unit to change
		int unit_offset = positionGenerator.nextInt(annotations.size());
		while(changed_units.contains(unit_offset))
			unit_offset = positionGenerator.nextInt(annotations.size());

		Unit u = annotations.get(unit_offset);
		// pick a position for the change
		int pos_offset = positionGenerator.nextInt(u.getEnd() - u.getBegin());

		int pos = annotations.get(unit_offset).getBegin() + pos_offset;

		if (u.getEnd() - u.getBegin() == 1)
			// don't change the text
			return false;

		changed_units.add(unit_offset);

		// update the text
		orig.deleteCharAt(pos);

		// move the end of the unit to the left, change text if textunit
		u = u.cloneWithDifferentOffsets(u.getBegin(), u.getEnd() - 1);
		if (u instanceof TextUnit)
			u = ((TextUnit)u).cloneWithDifferentText(orig.substring(u.getBegin(), u.getEnd()));
		annotations.set(unit_offset, u);

		// move all following units to the left
		for (int i=unit_offset+1 ; i < annotations.size(); i++ ) {
			u = annotations.get(i);
			annotations.set(i, u.cloneWithDifferentOffsets(u.getBegin() - 1, u.getEnd() - 1));
		}

		return true;
	}


	public static AnnotatedText changeTextSubstitution(AnnotatedText orig,
			EnumeratedDistribution<Character> characterGenerator) {
		return AnnotationSetShuffle.changeTextSubstitution(orig, characterGenerator, new Random());
	}
	public static AnnotatedText changeTextSubstitution(AnnotatedText orig,
			EnumeratedDistribution<Character> characterGenerator,
			Random positionGenerator) {

		StringBuffer text = new StringBuffer(orig.getText().getTextualContent());
		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));

		AnnotationSetShuffle.changeTextSubstitution(text, annots, characterGenerator, positionGenerator, new HashSet<Integer>());

		return new AnnotatedText(new Text(text.toString()), annots);
	}
	private static void changeTextSubstitution(StringBuffer orig, List<Unit> annotations,
			EnumeratedDistribution<Character> characterGenerator,
			Random positionGenerator, Set<Integer> changed_units) {

		// pick a unit and position to change
		int unit_offset = pickUnitToChange(positionGenerator, annotations.size(), changed_units);
		Unit u = annotations.get(unit_offset);
		int pos = pickTextOffset(positionGenerator, u);

		// update the text
		char orig_char = orig.charAt(pos);
		Character new_char = characterGenerator.sample();
		while (new_char.charValue() == orig_char) {
			new_char = characterGenerator.sample();
		}
		orig.replace(pos, pos+1, new_char.toString());

		// change text of the unit if textunit
		if (u instanceof TextUnit) {
			u = ((TextUnit)u).cloneWithDifferentText(orig.substring(u.getBegin(), u.getEnd()));
			annotations.set(unit_offset, u);
		}

	}

	// 2. Change the segmentation for AnnotationSet

	public static AnnotationSet shuffleSegmentation(AnnotationSet orig, double m) {
		if (!(0 <= m && m <= 1))
			throw new IllegalArgumentException("Magnitude has to be between 0 and 1");

		int numChanges = new Float(orig.getNumberOfAnnotations()*m).intValue();

		double prop = 1/(double)SegmentationChangetype.values().length;
		List<Pair<SegmentationChangetype, Double>> ps = new ArrayList<Pair<SegmentationChangetype, Double>>(SegmentationChangetype.values().length);
		for (SegmentationChangetype type: SegmentationChangetype.values())
			ps.add(new Pair<SegmentationChangetype,Double>(type, prop));
		EnumeratedDistribution<SegmentationChangetype> changes = new EnumeratedDistribution<SegmentationChangetype>(ps);

		return changeSegmentation(orig, numChanges, changes);
	}

	public enum SegmentationChangetype
	{
		MERGE, SPLIT
	}

	public static AnnotationSet changeSegmentation(AnnotationSet orig, int changes,
			EnumeratedDistribution<SegmentationChangetype> changeChooser) {
		return AnnotationSetShuffle.changeSegmentation(orig, changes, 0, changeChooser);
	}
	public static AnnotationSet changeSegmentation(AnnotationSet orig, int changes, int merge_gap,
			EnumeratedDistribution<SegmentationChangetype> changeChooser) {
		return AnnotationSetShuffle.changeSegmentation(orig, changes, merge_gap, changeChooser, new Random());
	}
	public static AnnotationSet changeSegmentation(AnnotationSet orig, int changes,
			EnumeratedDistribution<SegmentationChangetype> changeChooser,
			Random positionChooser) {
		return changeSegmentation(orig, changes, 0, changeChooser, positionChooser);
	}
	public static AnnotationSet changeSegmentation(AnnotationSet orig, int changes, int merge_gap,
			EnumeratedDistribution<SegmentationChangetype> changeChooser,
			Random positionChooser) {

		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));

		for (int i=0; i < changes; i++) {

			SegmentationChangetype type = changeChooser.sample();
			if (type.equals(SegmentationChangetype.MERGE))
				changeSegmentationMerge(annots, merge_gap, positionChooser);
			if (type.equals(SegmentationChangetype.SPLIT))
				changeSegmentationSplit(annots, positionChooser);
		}

		return new AnnotationSet(annots);
	}

	public static AnnotationSet changeSegmentationMerge(AnnotationSet orig) {
		return changeSegmentationMerge(orig, new Random());
	}
	public static AnnotationSet changeSegmentationMerge(AnnotationSet orig, Random positionGenerator) {
		return changeSegmentationMerge(orig, 0, positionGenerator);
	}
	public static AnnotationSet changeSegmentationMerge(AnnotationSet orig, int gap) {
		return changeSegmentationMerge(orig, gap, new Random());
	}
	public static AnnotationSet changeSegmentationMerge(AnnotationSet orig, int gap, Random positionGenerator) {
		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));
		AnnotationSetShuffle.changeSegmentationMerge(annots, gap, positionGenerator);
		return new AnnotationSet(annots);
	}
	private static void changeSegmentationMerge(List<Unit> annotations,	int gap, Random positionGenerator) {
		// pick a unit to merge
		int unit_offset = positionGenerator.nextInt(annotations.size());
	    Unit base = annotations.get(unit_offset);
	    Unit merge = null;

        int searchOffset = 1;
        while(unit_offset + searchOffset < annotations.size() &&
                annotations.get(unit_offset + searchOffset).getBegin() <= base.getEnd() + gap) {

            merge = annotations.get(unit_offset + searchOffset);
            // test if merge is a valid merge_candidate
            if (merge.getBegin() >= base.getEnd() &&
                    Objects.equals(base.getCreator(), merge.getCreator()) &&
                    Objects.equals(base.getType(), merge.getType()))
                break;

            merge = null;
            searchOffset += 1;
	    }

	    if (merge != null) {
            annotations.set(unit_offset, base.cloneWithDifferentOffsets(base.getBegin(), merge.getEnd()));
	        if (base instanceof TextUnit)
	            annotations.set(unit_offset,
	                    ((TextUnit)annotations.get(unit_offset)).cloneWithDifferentText(((TextUnit) base).getText() +
	                            (merge.getBegin() - base.getEnd() > 0 ? " ": "") +
                                ((TextUnit) merge).getText()));
            annotations.remove(unit_offset + searchOffset);
	    }

	}

	public static AnnotationSet changeSegmentationSplit(AnnotationSet orig) {
		return changeSegmentationSplit(orig, new Random());
	}
	public static AnnotationSet changeSegmentationSplit(AnnotationSet orig, Random positionGenerator) {
		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));
		AnnotationSetShuffle.changeSegmentationSplit(annots, positionGenerator);
		return new AnnotationSet(annots);
	}
	private static void changeSegmentationSplit(List<Unit> annotations,	Random positionGenerator) {

		// pick a unit to change
		int unit_offset = positionGenerator.nextInt(annotations.size());
		Unit u = annotations.get(unit_offset);

		if (u.getEnd() - u.getBegin() <= 1)
			// split not possible
			return;

		// pick a position for the split
		int pos = positionGenerator.nextInt(u.getEnd() - u.getBegin() - 1) + 1;

		Unit u1 = u.cloneWithDifferentOffsets(u.getBegin(), u.getBegin() + pos);
		Unit u2 = u.cloneWithDifferentOffsets(u.getBegin() + pos, u.getEnd());

		if (u instanceof TextUnit) {
			u1 = ((TextUnit)u1).cloneWithDifferentText(((TextUnit) u).getText().substring(0, pos));
			u2 = ((TextUnit)u2).cloneWithDifferentText(((TextUnit) u).getText().substring(pos));
		}

		annotations.set(unit_offset, u1);
		annotations.add(unit_offset+1, u2);
	}

	// 3. Create random labels for AnnotationSet

	public static AnnotationSet randomizeAttributeValues(AnnotationSet orig, String attribute, EnumeratedDistribution<String> labelGenerator) {
	
		List<Unit> relabeled = new LinkedList<Unit>();

		Unit[] orig_annots = orig.getAnnotations();

		for (int i=0; i < orig_annots.length; i++)
			relabeled.add(orig_annots[i].cloneWithDifferentLabel(attribute, labelGenerator.sample()));

		return new AnnotationSet(relabeled);
	}

	public static AnnotationSet shuffleAttributeValues(AnnotationSet orig, String attribute, double m) {
		if (!(0 <= m && m <= 1))
			throw new IllegalArgumentException("Magnitude has to be between 0 and 1");

		int numChanges = new Float(orig.getNumberOfAnnotations()*m).intValue();

		HashMap<String, Integer> labels = new HashMap<String, Integer>();
		for (Unit u: orig.getAnnotations()) {
			String label = u.getAttributeValue(attribute);
			if (labels.containsKey(u.getAttributeValue(attribute)))
				labels.put(label, labels.get(label) + 1);
			else
				labels.put(label, 1);
		}

		List<Pair<String, Double>> ps = new ArrayList<Pair<String, Double>>(labels.size());
		for (String label: labels.keySet()) {
			ps.add(new Pair<String,Double>(label, labels.get(label)/(double)orig.getNumberOfAnnotations()));
		}
		EnumeratedDistribution<String> labelGenerator = new EnumeratedDistribution<String>(ps);

		return changeAttributeValues(orig, attribute, numChanges, labelGenerator);
	}

	public static AnnotationSet changeAttributeValues(AnnotationSet orig, String attribute,
			int changes, EnumeratedDistribution<String> labelGenerator) {
		return AnnotationSetShuffle.changeAttributeValues(orig, attribute, changes, labelGenerator, new Random());
	}
	public static AnnotationSet changeAttributeValues(AnnotationSet orig, String attribute,
			int changes,EnumeratedDistribution<String> labelGenerator,
			Random positionChooser) {

		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));

		Set<Integer> changed_units = new HashSet<Integer>();

		for (int i=0; i < changes; i++) {
			changeAttributeValue(annots, attribute, labelGenerator, positionChooser, changed_units);
		}
		return new AnnotationSet(annots);

	}

	public static AnnotationSet changeAttributeValue(AnnotationSet orig, String attribute,
			EnumeratedDistribution<String> labelGenerator) {
		return changeAttributeValue(orig, attribute, labelGenerator, new Random());
	}
	public static AnnotationSet changeAttributeValue(AnnotationSet orig, String attribute,
			EnumeratedDistribution<String> labelGenerator, Random positionGenerator) {

		List<Unit> annots = new LinkedList<Unit>(Arrays.asList(orig.getAnnotations()));
		AnnotationSetShuffle.changeAttributeValue(annots, attribute, labelGenerator, positionGenerator, new HashSet<Integer>());
		return new AnnotationSet(annots);
	}
	private static void changeAttributeValue(List<Unit> annotations, String attribute,
			EnumeratedDistribution<String> labelGenerator, Random positionGenerator, Set<Integer> changed_units) {

		// pick a unit to change
		int unit_offset = pickUnitToChange(positionGenerator, annotations.size(), changed_units);

		String orig_label = annotations.get(unit_offset).getAttributeValue(attribute);
		String new_label = labelGenerator.sample();
		while(new_label.equals(orig_label))
			new_label = labelGenerator.sample();

		annotations.set(unit_offset,
				annotations.get(unit_offset).cloneWithDifferentLabel(attribute, new_label));
	}

}
