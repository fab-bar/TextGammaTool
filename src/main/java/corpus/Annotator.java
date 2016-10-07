package corpus;

public class Annotator implements Comparable<Annotator> {

	String name;
	
	public Annotator(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		
		if (this.name != null)
			return this.name.equals(((Annotator)o).getName());
		else
			return ((Annotator) o).getName() == null;
	}

	@Override
	public int compareTo(Annotator y) {
		return this.getName().compareTo(y.getName());
	}

	@Override
    public int hashCode() {
		return this.getName().hashCode();
	}
		
}
