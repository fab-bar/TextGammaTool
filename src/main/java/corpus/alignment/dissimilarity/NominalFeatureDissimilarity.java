package corpus.alignment.dissimilarity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import corpus.Unit;

public class NominalFeatureDissimilarity implements Dissimilarity {

	@Override
	public double dissimilarity(Unit u, Unit v) {
		
		if (u == null && v == null)
			return 0;

		if (u == null)
			return 1;
		
		if (v == null)
			return 1;
		
		if (!Objects.equals(u.getType(), v.getType()))
			return 1;
		
		return pos_dissimilarity(u, v) + feat_dissimilarity(u, v);
	}
	
	protected Set<String> getAttributes(Unit u, Unit v) {
		Set<String> attributes = new HashSet<String>();
		attributes.addAll(u.getAttributes());
		attributes.addAll(v.getAttributes());

		return attributes;
	}
	
	protected int numberOfDissimilarFeatures(Unit u, Unit v) {
		
		Set<String> attributes = this.getAttributes(u, v);
		
		int diffs = 0;
		for (String attr: attributes)
			if (!Objects.equals(u.getAttributeValue(attr), v.getAttributeValue(attr)))
				diffs += 1;
		
		return diffs;
	}

	private double feat_dissimilarity(Unit u, Unit v) {
		
		Set<String> attributes = this.getAttributes(u, v);
		
		if (attributes.isEmpty())
			return 0;
		
		return this.numberOfDissimilarFeatures(u,v)/((float) attributes.size());
	}

	protected double pos_dissimilarity(Unit u, Unit v) {
		if (u.getBegin() == v.getBegin() && u.getEnd() == v.getEnd())
			return 0;
		else
			return 1;
	}

}
