package corpus;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.TreeSet;

// a set of units created by a given set of annotators;
// all units are linked to the same (implicit) continuum
public class AnnotationSet {
	
	HashSet<Annotator> annotators = new HashSet<Annotator>();
	TreeSet<Unit> annotations = new TreeSet<Unit>();
	HashSet<String> attributes = new HashSet<String>();
	HashSet<String> types = new HashSet<String>();

	int lowestOffset = Integer.MAX_VALUE;
	int highestOffset = 0;

	public AnnotationSet(Collection<Unit> annotations) {
		for (Unit annot: annotations) {
			
			if(annot.getBegin() < this.lowestOffset)
				this.lowestOffset = annot.getBegin();
			if(annot.getEnd() > this.highestOffset)
				this.highestOffset = annot.getEnd();

			types.add(annot.getType());
			attributes.addAll(annot.getAttributes());
			
			this.annotators.add(annot.getCreator());
			this.annotations.add(annot);
		}
	}
	
	public int getNumberOfAnnotators() {
		return annotators.size();
	}
	public int getNumberOfAnnotations() {
		return annotations.size();
	}
	
	public double getAverageNumberOfAnnotations() {
		return this.getNumberOfAnnotations()/((double)this.getNumberOfAnnotators());
	}
	
	public Annotator[] getAnnotators() {
		return annotators.toArray(new Annotator[annotators.size()]);
	}

	public Unit[] getAnnotations() {
		return  annotations.toArray(new Unit[annotations.size()]);
	}

	public String[] getAttributes() {
		return this.attributes.toArray(new String[0]);
	}

	public String[] getTypes() {
		return this.types.toArray(new String[0]);
	}

	public Unit[] getAnnotations(String type) {

		LinkedList<Unit> units = new LinkedList<Unit>();

		for (Unit annot: this.getAnnotations()) {
			if (java.util.Objects.equals(annot.getType(), type))
				units.add(annot);
		}

		return units.toArray(new Unit[units.size()]);
	}

	public Unit[] getAnnotations(Annotator creator) {
		LinkedList<Unit> units = new LinkedList<Unit>();

		for (Unit annot: this.getAnnotations()) {
			if (java.util.Objects.equals(annot.getCreator(), creator))
				units.add(annot);
		}

		return units.toArray(new Unit[units.size()]);
	}

	public boolean contains(Unit u) {
		return this.annotations.contains(u);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;

		AnnotationSet comp = (AnnotationSet)o;
		boolean no_diff = true;
		for(int i = 0; i < this.getAnnotations().length; i++) {
			no_diff = no_diff && this.getAnnotations()[i].equals(comp.getAnnotations()[i]);
		}

		return no_diff;
	}

	@Override
	public int hashCode() {
		return this.annotations.hashCode() + this.annotators.hashCode();
	}

}
