package corpus.merge.textalignment;

import java.util.List;

public abstract class TextAlignment {

	public abstract List<String[]> getAlignments();
	public abstract int getInsertions();
	public abstract int getDeletions();
	public abstract int getSubstitutions();
	public abstract int getLength();

}
