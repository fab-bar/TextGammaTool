package io;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedTextFileReaderStaticFactory {

	private static final Map<String, Class<? extends AnnotatedTextFileReaderFactory>> reader_types;
	static {
		Map<String, Class<? extends AnnotatedTextFileReaderFactory>> tmp_map;
		tmp_map = new HashMap<>();
		tmp_map.put("CoraXML", CoraXMLReaderFactory.class);
		tmp_map.put("CoraXMLReN", CoraXMLReNReaderFactory.class);
		tmp_map.put("CoraTSV", CoraTSVReaderFactory.class);
		tmp_map.put("CoraTSVReN", CoraTSVReNReaderFactory.class);
		// add further supported file types...
		reader_types = Collections.unmodifiableMap(tmp_map);
	}

	public static String[] getFileReaderTypes() {
		return  reader_types.keySet().toArray(new String[0]);
	}

	public static AnnotatedTextFileReaderFactory createReaderFactory(String type) throws InstantiationException, IllegalAccessException {
		if (reader_types.containsKey(type))
			return reader_types.get(type).newInstance();
		else
			return null;
	}

}
