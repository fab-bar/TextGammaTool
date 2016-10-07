package io;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import corpus.AnnotatedText;
import corpus.Annotator;

public abstract class AnnotatedTextFileReader {
	
	public AnnotatedText readFile(String filename, Annotator creator) throws Exception {
		return this.readFile(new File(filename).toURI().toURL(), creator);
	}
	public AnnotatedText readFile(URL fileurl, Annotator creator) throws Exception {
		return this.readFile(fileurl.openStream(), creator);
	}

	public abstract AnnotatedText readFile(InputStream input, Annotator creator) throws Exception;

}
