package io;

import java.util.HashMap;
import java.util.Map;

public class CoraTSVReNReaderFactory extends AnnotatedTextFileReaderFactory {

	@Override
	public Map<String, String> getOptions() {
		return new HashMap<String, String>();
	}

	@Override
	protected AnnotatedTextFileReader instantiateAnnotatedTextFileReader(Map<String, String> options) {
		return new CoraTSVReader("#", "§");
	}

}
