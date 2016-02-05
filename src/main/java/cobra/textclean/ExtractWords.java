package cobra.textclean;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.util.CountingCache;

/**
 * <p>Extracts words from a text. Uses the Lucene analyzer. Converts to Lower case, spell checks, and stems.</p>
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Dec 22, 2015
 *
 */
public class ExtractWords {
	private final static Logger logger = LoggerFactory
			.getLogger(ExtractWords.class);
	private static final int MINWORDLEN = 3;
	private List<String> stopwords;
	private CountingCache scrubbedWords;
	private Spelling spelling;

	public void extractWords(String text) {
		try {

			Analyzer analyzer = new StandardAnalyzer();
			StringReader in = new StringReader(text);
			TokenStream ts = analyzer.tokenStream("content", in);
			ts.reset();
			ts = new LowerCaseFilter(ts);

			CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
			List<String> words = new ArrayList<>();
			while (ts.incrementToken()) {
				char[] termBuffer = termAtt.buffer();
				int termLen = termAtt.length();
				String w = new String(termBuffer, 0, termLen);
				words.add(spelling==null?w:spelling.correct(w)[0]);
			}
			ts.end();
			ts.close();
			analyzer.close();
			scrubbedWords = new CountingCache();
			for (String word : words) {
				if (word.length() >= MINWORDLEN && (stopwords==null || !stopwords.contains(word))) {
					scrubbedWords.incr(word);
				} else {
					logger.debug("Ignoring word: {}", word);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public CountingCache getScrubbedWords() {
		return scrubbedWords;
	}

	public void setStopwords(List<String> stopwords) {
		this.stopwords = stopwords;
	}

	public void setSpelling(Spelling spelling) {
		this.spelling = spelling;
	}

}
