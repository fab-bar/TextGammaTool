package io;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Test;

import corpus.AnnotatedText;
import corpus.Annotator;
import corpus.TextUnit;

public class CoraXMLReaderTest {

	@Test
	public void testReadCoraXML() throws Exception {	

		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource("cora_ren.xml");
		
		AnnotatedTextFileReader reader = new CoraXMLReader("#", "§"); 
		AnnotatedText testtext = reader.readFile(resource, new Annotator("a"));
		
		assertEquals(10, testtext.getAnnotations().length);
		
		assertEquals("DDARTA", testtext.getAnnotations()[1].getAttributeValue("pos"));
		assertEquals("textunit", testtext.getAnnotations()[1].getType());
		assertEquals("De", ((TextUnit)testtext.getAnnotations()[1]).getText());

		assertEquals("NE", testtext.getAnnotations()[testtext.getAnnotations().length-1].getAttributeValue("pos"));
		assertEquals("textunit", testtext.getAnnotations()[testtext.getAnnotations().length-1].getType());
		assertEquals("Griseldis", ((TextUnit)testtext.getAnnotations()[testtext.getAnnotations().length-1]).getText());
	}

}
