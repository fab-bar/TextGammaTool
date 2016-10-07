package corpus.alignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.util.CombinatoricsUtils;

import corpus.Annotator;
import corpus.Unit;
import corpus.alignment.dissimilarity.Dissimilarity;

public class UnitaryAlignment implements Comparable<UnitaryAlignment> {
	
	int begin;
	int end;

	final HashMap<Annotator, Unit> units;
	
	public UnitaryAlignment(Collection<Unit> units, Set<Annotator> annotators) {
		
		this.begin = Integer.MAX_VALUE;
		this.end = Integer.MIN_VALUE;

		this.units = new HashMap<Annotator, Unit>();
		for (Unit unit: units) {
			if (this.units.containsKey(unit.getCreator())) {
				throw new IllegalArgumentException("Unitary alignment may not contain two units from the same creator");
			}
			if (!annotators.contains(unit.getCreator())) {
				throw new IllegalArgumentException("Unitary alignment may only contain units created by annotators from the given set.");
			}

			if (unit.getBegin() < this.begin)
				this.begin = unit.getBegin();
			if (unit.getEnd() > this.end)
				this.end = unit.getEnd();

			this.units.put(unit.getCreator(), unit);
		}
		
		// add empty elements
		for (Annotator annot: annotators) {
			if (!this.units.containsKey(annot))
				this.units.put(annot, null);
		}
	}

	public int arity() {
		return this.units.size();
	}
	
	public int getBegin() {
		return this.begin;
	}
	public int getEnd() {
		return this.end;
	}

	public Set<Annotator> getAnnotators() {
		return this.units.keySet();
	}
	
	public Unit getUnit(Annotator creator) {
		return units.get(creator);
	}

	public double getDisorder(Dissimilarity d) {
		
		double dissim = 0;
		
		Unit[] annots = units.values().toArray(new Unit[0]);
		
		for (int i = 0; i < annots.length; i++)
			for (int j = i+1; j < annots.length; j++)
				dissim += d.dissimilarity(annots[i], annots[j]);
		
		return dissim/(double)CombinatoricsUtils.binomialCoefficient(this.arity(), 2);

	}

	@Override
	public int hashCode() {
		return this.units.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;

		UnitaryAlignment comp = (UnitaryAlignment)o;

		if (this.arity() != comp.arity())
			return false;

		if (!Objects.equals(this.getAnnotators(), comp.getAnnotators()))
			return false;

		for(Annotator creator: this.getAnnotators())
			if(!Objects.equals(this.getUnit(creator), comp.getUnit(creator)))
				return false;

		return true;
	}

	@Override
	public int compareTo(UnitaryAlignment ua) {

		if (this.equals(ua))
			return 0;

		if (ua == null)
			return -1;

		// order by arity
		if (this.arity() != ua.arity())
			return Integer.compare(this.arity(), ua.arity());

		// order by annotator sets if they are not equal
		if(!Objects.equals(this.getAnnotators(), ua.getAnnotators())) {

			// size of the sets are equal (due to equal arity)
			// order by the first differing Annotator

			List<Annotator> annot_this = new ArrayList<Annotator>(new TreeSet<Annotator>(this.getAnnotators()));
			List<Annotator> annot_comp = new ArrayList<Annotator>(new TreeSet<Annotator>(ua.getAnnotators()));

			for (int i=0; i < annot_this.size(); i++)
				if (!Objects.equals(annot_this.get(i), annot_comp.get(i)))
						return annot_this.get(i).compareTo(annot_comp.get(i));
		}

		// order by the span covered by the units in this alignment
		if (this.getBegin() != ua.getBegin())
			return Integer.compare(this.getBegin(), ua.getBegin());
		if (this.getEnd() != ua.getEnd())
			return Integer.compare(this.getEnd(), ua.getEnd());

		// the numbers of units and the span covered are the same
		// annotations must differ
		// order by the first differing annotation
		for (Annotator creator: new ArrayList<Annotator>(new TreeSet<Annotator>(this.getAnnotators())))
			if (!Objects.equals(this.getUnit(creator), ua.getUnit(creator)))
				if (this.getUnit(creator) == null)
					return 1;
				else
					return this.getUnit(creator).compareTo(ua.getUnit(creator));

		// should never be reached
		return 0;
	}

	@Override
	public String toString() {
		return this.toString(new ArrayList<String>());
	}

	public String toString(List<String> attributes) {
		List<String> units = new ArrayList<String>(this.arity());

		StringBuffer empty = new StringBuffer();
		empty.append("--");
		for (int i=0; i < attributes.size(); i++)
			empty.append("\t--");
		String empty_unit = empty.toString();

		for (Annotator creator: new ArrayList<Annotator>(new TreeSet<Annotator>(this.getAnnotators())))
			if (this.getUnit(creator) != null)
				units.add(this.getUnit(creator).toString(attributes));
			else
				units.add(empty_unit);

		return String.join("\t", units);
	}
}
