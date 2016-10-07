package corpus.alignment.dissimilarity;

import corpus.Unit;

public interface Dissimilarity {
	public double dissimilarity(Unit u, Unit v);
}
