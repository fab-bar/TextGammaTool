package corpus.alignment.dissimilarity;

import java.util.Objects;

import corpus.TextUnit;
import corpus.Unit;

public class NominalFeatureTextDissimilarity extends NominalFeatureDissimilarity {
	
	@Override
	public double dissimilarity(Unit u, Unit v) {
		
		if (u == null && v == null)
			return 0;

		if (u == null)
			return 1;
		
		if (v == null)
			return 1;
		
		if (!(u instanceof TextUnit && v instanceof TextUnit))
			throw new IllegalArgumentException("Units have to be TextUnits");
		
		int text_diff = 0;
		if (!Objects.equals(((TextUnit) u).getText(), ((TextUnit) v).getText()))
				text_diff += 1;
		
		return pos_dissimilarity(u, v) + 
				(this.numberOfDissimilarFeatures(u, v) + text_diff)/
				((float) this.getAttributes(u, v).size() + 1);
	}

}
