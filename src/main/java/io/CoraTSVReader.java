package io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import corpus.AnnotatedText;
import corpus.Annotator;
import corpus.Text;
import corpus.Unit;

public class CoraTSVReader extends CoraFileReader {

	public CoraTSVReader(String mergeSymbol, String splitSymbol) {
		super(mergeSymbol, splitSymbol);
	}

	@Override
	public AnnotatedText readFile(InputStream input, Annotator creator) throws Exception {

		// TODO get number of entries??
		List<Unit> annotations = new ArrayList<Unit>(0);
		StringBuffer documentText = new StringBuffer();

		// read tokens
		Iterable<CSVRecord> tokens =
				CSVFormat.newFormat('\t').withFirstRecordAsHeader().parse(new InputStreamReader(input));


		for(CSVRecord token: tokens) {

			HashMap<String, String> features = new HashMap<String, String>();
			for (Entry<String, String> feat: token.toMap().entrySet()) {
				if (!feat.getKey().equals("trans")) {
					features.put(feat.getKey(), feat.getValue());
				}
			}

			addToken(documentText, annotations, creator, token.get("trans"), features);
		}

		return new AnnotatedText(new Text(documentText.toString()), annotations);
	}

}
