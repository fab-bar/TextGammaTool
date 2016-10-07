package corpus;

import java.util.HashMap;
import java.util.List;

public class TextUnit extends Unit {
	
	String text;

	public TextUnit(Annotator creator, int beg, int end, HashMap<String, String> featureset, String text) {
		super(creator, "textunit", beg, end, featureset);
		if (text == null)
			text = "";
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o) && this.text.equals(((TextUnit)o).getText());
	}

	@Override
	public TextUnit cloneWithDifferentLabel(String type, String label) {

		HashMap<String, String> feat = this.getFeatureStructure();
		feat.put(type, label);
		return new TextUnit(this.getCreator(), this.getBegin(), this.getEnd(), feat, this.text);

	}

	@Override
	public TextUnit cloneWithDifferentOffsets(int begin, int end) {
		return new TextUnit(this.getCreator(), begin, end, this.getFeatureStructure(), this.text);
	}

	@Override
	public TextUnit cloneWithDifferentCreator(Annotator creator) {
		return new TextUnit(creator, this.getBegin(), this.getEnd(), this.getFeatureStructure(), this.text);
	}

	public TextUnit cloneWithDifferentText(String text) {
		return new TextUnit(this.getCreator(), this.getBegin(), this.getEnd(), this.getFeatureStructure(), text);
	}

	@Override
	public String toString(List<String> attributes) {
		StringBuffer ret = new StringBuffer();
		ret.append(this.getText());
		for (String attribute: attributes) {
			ret.append("\t");
			if (this.getAttributeValue(attribute) != null)
				ret.append(this.getAttributeValue(attribute));
			else
				ret.append("--");
		}
		return ret.toString();
	}

}
