package io;

import java.util.Map;

public abstract class AnnotatedTextFileReaderFactory {
	
	public abstract Map<String, String> getOptions();
	public final AnnotatedTextFileReader createAnnotatedTextFileReader(Map<String, String> options) throws IllegalArgumentException {
		for (String option: this.getOptions().keySet())
			if (!options.containsKey(option))
				throw new IllegalArgumentException("Missing parameter: " + option);
		
		return this.instantiateAnnotatedTextFileReader(options);
	}
	protected abstract AnnotatedTextFileReader instantiateAnnotatedTextFileReader(Map<String, String> options);

}
