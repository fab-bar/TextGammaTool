package corpus;

public class Text {
	
	String text;

	public Text(String text) {
		this.text = text;
	}

	public String getTextualContent() {
		return text;
	}
	
	public int getLength() {
		return text.length();
	}
	
	@Override
	public boolean equals(Object t) {
		if (t == null)
			return false;
		if (getClass() != t.getClass())
			return false;
		
		return this.text.equals(((Text)t).getTextualContent());
	}
}
