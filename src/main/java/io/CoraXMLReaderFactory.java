package io;

import java.util.HashMap;
import java.util.Map;

public class CoraXMLReaderFactory extends AnnotatedTextFileReaderFactory {

	@Override
	public Map<String, String> getOptions() {
		Map<String, String> arguments = new HashMap<String, String>();
		arguments.put("mergeSymbol", "The symbol used to merge dipl tokens");
		arguments.put("splitSymbol", "The symbol used to split dipl tokens");
		return arguments;
	}

	@Override
	protected AnnotatedTextFileReader instantiateAnnotatedTextFileReader(Map<String, String> options) {
		return new CoraXMLReader(options.get("mergeSymbol"), options.get("splitSymbol"));
	}
	
}
