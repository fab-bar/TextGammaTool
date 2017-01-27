package io;

import java.util.HashMap;
import java.util.List;

import corpus.Annotator;
import corpus.TextUnit;
import corpus.Unit;

public abstract class CoraFileReader extends AnnotatedTextFileReader {

	private String splitSymbol;
	private String mergeSymbol;

	public CoraFileReader(String mergeSymbol, String splitSymbol) {
		this.mergeSymbol = mergeSymbol;
		this.splitSymbol = splitSymbol;
	}

	protected void addToken(StringBuffer documentText, List<Unit> annotations, Annotator creator,
			String token_text, HashMap<String, String> features) {

		int TokenBeg = documentText.length();
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

		TextUnit current_tok = new TextUnit(creator, TokenBeg, TokenEnd, features, token_text);
		annotations.add(current_tok);
	}

}
