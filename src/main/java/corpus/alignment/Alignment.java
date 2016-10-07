package corpus.alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import corpus.AnnotationSet;
import corpus.Annotator;
import corpus.Unit;
import corpus.alignment.dissimilarity.Dissimilarity;

public class Alignment {
	
	Set<UnitaryAlignment> alignments;
	AnnotationSet annoset;
	
	public Alignment(Set<UnitaryAlignment> uas, AnnotationSet as) {

		if (as.getNumberOfAnnotators() == 1)
			throw new IllegalArgumentException("An Alignment needs to have at least 2 annotators.");

		Set<Annotator> creators = new HashSet<Annotator>(Arrays.asList(as.getAnnotators()));
		
		Set<Unit> units = new HashSet<Unit>();
		
		for (UnitaryAlignment ua: uas) {
			if(!creators.equals(ua.getAnnotators()))
				throw new IllegalArgumentException("Not all unitary alignments have the same set of annotors.");
			
			for (Annotator creator: creators) {
				Unit unit = ua.getUnit(creator);
				if (unit != null) {
					if(units.contains(unit))
						throw new IllegalArgumentException("A unit is contained twice in the unitary alignments.");
					else if (!as.contains(unit))
						throw new IllegalArgumentException("A unit is contained the unitary alignments but not in the annotation set.");
					else
						units.add(unit);
				}
			}
		}
		
		if(!Objects.equals(units, new HashSet<Unit>(Arrays.asList(as.getAnnotations()))))
			throw new IllegalArgumentException("Not all units from the set are contained in the unitary alignments.");
		
		this.alignments = uas;
		this.annoset = as;
	}
	
	public double getDisorder(Dissimilarity d) {
		
		double disorder = 0;
		
		for (UnitaryAlignment alignment: alignments)
			disorder += alignment.getDisorder(d);
		
		return disorder/this.annoset.getAverageNumberOfAnnotations();
	}

	@Override
	public String toString() {
		return this.toString(new ArrayList<String>());
	}

	public String toString(List<String> attributes) {
		List<UnitaryAlignment> units_this = new ArrayList<UnitaryAlignment>(new TreeSet<UnitaryAlignment>(this.alignments));
		List<String> uas = new ArrayList<String>(units_this.size());
		for (UnitaryAlignment ua: units_this)
			uas.add(ua.toString(attributes));

		return String.join("\n", uas);
	}
}
