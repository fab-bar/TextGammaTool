package corpus;

import java.util.Collection;

public class AnnotatedText extends AnnotationSet {
	
	Text text;

	public AnnotatedText(Text text, Collection<Unit> annotations) {
		super(annotations);
		
		if (this.lowestOffset < 0)
			throw new IndexOutOfBoundsException("Lowest offset < 0");
		if (this.highestOffset > text.getLength())
			throw new IndexOutOfBoundsException("Highest offset > length of text");

		this.text = text;
	}
	
	public Text getText() {
		return this.text;
	}

}
