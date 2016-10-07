package io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import corpus.AnnotatedText;
import corpus.Annotator;
import corpus.Text;
import corpus.TextUnit;
import corpus.Unit;


public class CoraXMLReader extends AnnotatedTextFileReader {
	
	private String splitSymbol;
	private String mergeSymbol;

	public CoraXMLReader(String mergeSymbol, String splitSymbol) {
		this.mergeSymbol = mergeSymbol;
		this.splitSymbol = splitSymbol;
	}

	@Override
	public AnnotatedText readFile(InputStream input, Annotator creator) throws ParserConfigurationException, SAXException, IOException {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(input);
		NodeList cora_tokens = document.getElementsByTagName("mod");
		
		List<Unit> annotations = new ArrayList<Unit>(cora_tokens.getLength());
		
		StringBuffer documentText = new StringBuffer();
		
		for(int i=0; i < cora_tokens.getLength(); i++) {
			Node token = cora_tokens.item(i);
			
			int TokenBeg = documentText.length();
			String token_text = token.getAttributes().getNamedItem("trans").getTextContent();
			
			boolean text_token = true;
			
			token_text = token_text.replace(this.mergeSymbol, " ");
			
			if (token_text.contains(this.splitSymbol)) {
				text_token = false;
				token_text = token_text.replace(this.splitSymbol, "");
			}
			
			documentText.append(token_text);
			int TokenEnd = documentText.length();

			if (text_token) {
				documentText.append(" ");
			}

			// read all annotations (i.e. subelements of mod that have a "tag" attribute)
			HashMap<String, String> features = new HashMap<String, String>();
			NodeList annotNodes = ((Element)token).getChildNodes();
			for (int j = 0; j < annotNodes.getLength(); j++) {
				if(annotNodes.item(j).hasAttributes()) {
					Node tag = annotNodes.item(j).getAttributes().getNamedItem("tag");
					if (tag != null) {
						features.put(annotNodes.item(j).getNodeName(), tag.getTextContent());
					}
				}
			}
			
			TextUnit current_tok = new TextUnit(creator, TokenBeg, TokenEnd, features, token_text);
			annotations.add(current_tok);
			
		}
		
		return new AnnotatedText(new Text(documentText.toString()), annotations);
	}

}