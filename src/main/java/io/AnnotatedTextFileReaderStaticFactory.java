package io;

public class AnnotatedTextFileReaderStaticFactory {
	
	public static AnnotatedTextFileReaderFactory createReaderFactory(String type) {
		if (type.equals("CoraXML")) {
			return new CoraXMLReaderFactory();
		}
		else if (type.equals("CoraXMLReN")) {
			return new CoraXMLReNReaderFactory();
		}
		return null;
	}

}
