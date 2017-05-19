package corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Unit implements Comparable<Unit> {
	
	private Annotator creator;
	private String type;
	private HashMap<String, String> featureset = new HashMap<String, String>();
	
	private int beg;
	private int end;

	@SuppressWarnings("unchecked")
	private void setFeatureset(HashMap<String, String> featureset) {
		if (featureset != null)
			// clone the map to make Unit immutable
			this.featureset = (HashMap<String, String>) featureset.clone();
	}
	
	public Unit(Annotator creator, String type, int beg, int end, HashMap<String, String> featureset) {
		if (creator == null)
			this.creator = new Annotator("");
		else
			this.creator = creator;

		if (type == null)
			this.type = "";
		else
			this.type = type;
		
		if (beg >= end)
			throw new IllegalArgumentException("Begin has to be smaller than end.");

		this.beg = beg;
		this.end = end;

		this.setFeatureset(featureset);
	}
	
	public Unit(Annotator creator, int beg, int end, HashMap<String, String> featureset) {
		this(creator, null, beg, end, featureset);
	}


	public Annotator getCreator() {
		return this.creator;
	}
	
	public String getType() {
		return this.type;
	}
	
	public int getBegin() {
		return this.beg;
	}
	
	public int getEnd() {
		return this.end;
	}
	
	public Set<String> getAttributes(){
		return featureset.keySet();
	}
	
	public String getAttributeValue(String attribute) {
		return featureset.get(attribute);
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> getFeatureStructure() {
		return (HashMap<String, String>) this.featureset.clone();
	}

	public boolean isCoextensive(Unit seg) {
		return (this.getBegin() == seg.getBegin()) && (this.getEnd() == seg.getEnd());
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		
		Unit seg = (Unit)o;
		// same type?
		if (!java.util.Objects.equals(this.getType(), seg.getType()))
			return false;

		// same creator?
		if (!java.util.Objects.equals(this.getCreator(), seg.getCreator()))
			return false;
	
		// same span?
		if (!this.isCoextensive(seg))
			return false;
		
		// same attributes?
		if (!this.getAttributes().equals(seg.getAttributes()))
			return false;
		// same attribute values?
		for (String attribute: this.getAttributes()) {
			if (!java.util.Objects.equals(this.getAttributeValue(attribute), seg.getAttributeValue(attribute)))
				return false;
		}
		
		return true;
	}
	
	public boolean overlaps(Unit seg) {		
	    return !(seg.getEnd() <= this.getBegin() ||
	    	     seg.getBegin() >= this.getEnd());
	}

	@Override
	public int compareTo(Unit y) {

		if(y==null)
			return -1;

		if (this.equals(y))
			return 0;

		// first: start offset
		if(this.getBegin() < y.getBegin())
			return -1;
		if(this.getBegin() > y.getBegin())
			return 1;
		
		// second: end offset
		if(this.getEnd() < y.getEnd())
			return -1;
		if(this.getEnd() > y.getEnd())
			return 1;
		
		// sort by Type
		if (!java.util.Objects.equals(this.getType(), y.getType()))
			if (this.getType() != null && y.getType() != null)
				return this.getType().compareTo(y.getType());
			// typeless before typed
			else if (this.getType() == null)
				return -1;
			else
				return 1;
			
		// sort by Creator
		if (!java.util.Objects.equals(this.getCreator(), y.getCreator()))
			if (this.getCreator() != null && y.getCreator() != null)
				return this.getCreator().getName().compareTo(y.getCreator().getName());
			// without creator before with creator
			else if (this.getCreator() == null)
				return -1;
			else
				return 1;

		// sort by number of attributes
		if (this.getAttributes().size() != y.getAttributes().size())
			return Integer.compare(this.getAttributes().size(), y.getAttributes().size());
		
		// sort by attributes names
		List<String> attributelistX = new ArrayList<String>(this.getAttributes());
		java.util.Collections.sort(attributelistX);
		
		List<String> attributelistY = new ArrayList<String>(y.getAttributes());
		java.util.Collections.sort(attributelistY);
		
		if(!attributelistX.equals(attributelistY))
			return String.join("", attributelistX).compareTo(String.join("", attributelistY));

		// sort by attribute values (in order of names)
		for(int i=0; i < attributelistX.size(); i++) {
			String attr = attributelistX.get(i);
			if(!this.getAttributeValue(attr).equals(y.getAttributeValue(attr))) {
				return this.getAttributeValue(attr).compareTo(y.getAttributeValue(attr));
			}
		}
		
		// annotations are equal 
		// (but one is instantiation of subclass)
		return 0;
		
	}
	
	public Unit cloneWithDifferentLabel(String type, String label) {

		HashMap<String, String> feat = this.getFeatureStructure();
		feat.put(type, label);
		return new Unit(this.creator, this.type, this.beg, this.end, feat);
	}

	public Unit cloneWithDifferentOffsets(int begin, int end) {
		return new Unit(this.creator, this.type, begin, end, this.featureset);

	}
	public Unit cloneWithDifferentCreator(Annotator creator) {
		return new Unit(creator, this.type, this.beg, this.end, this.featureset);
	}

	@Override
	public int hashCode() {
		int hash = 0;
		hash += this.getCreator().hashCode();
		hash += this.getType().hashCode();
		hash += this.featureset.hashCode();
		hash += this.getBegin() + this.getEnd();

		return hash;
	}

	@Override
	public String toString() {
		return this.toString(new ArrayList<String>());
	}

	public String toString(List<String> attributes) {
		StringBuffer ret = new StringBuffer();
		ret.append(String.valueOf(this.getBegin()));
		ret.append("-");
		ret.append(String.valueOf(this.getEnd()));
		for (String attribute: attributes) {
			ret.append("\t");
			if (this.getAttributeValue(attribute) != null)
				ret.append(this.getAttributeValue(attribute));
		}
		return ret.toString();
	}

}
