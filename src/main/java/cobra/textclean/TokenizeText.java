package cobra.textclean;

import java.io.IOException;
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

/**
 * <p>Generates tokenized words. To perform either spell checking or stopwording, 
 * after instancing this class, call the loadSpelling and loadStopwording methods.
 * This will cause these processes to run against each word when extracting.
 * </p>
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Dec 30, 2015
 *
 */
public class TokenizeText {
	final static Logger logger = LoggerFactory.getLogger(TokenizeText.class);
	private static final int MINWORDLEN = 3;
	private List<String> scrubbedWords;
	private Spelling spelling;
	private Stopwording stopWording;

	/**
	 * <p>Creates a spelling tool and sets it for use.</p>
	 * @throws IOException
	 */
	public void loadSpelling() throws IOException {
		spelling = new Spelling();
	}
	/**
	 * <p>Creates a stopwording tool and sets it for use.</p>
	 * @throws IOException
	 */
	public void loadStopWording() throws IOException {
		stopWording = new Stopwording();
	}

	public void extractWords(String text) throws IOException {
		Analyzer analyzer = new StandardAnalyzer();
		// String joinedFields = Joiner.on(" ").join(fields).replaceAll("\\s+",
		// " ");
		StringReader in = new StringReader(text);
		TokenStream ts = analyzer.tokenStream("content", in);
		ts.reset();
		ts = new LowerCaseFilter(ts);

		scrubbedWords = new ArrayList<>();
		CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
		while (ts.incrementToken()) {
			char[] termBuffer = termAtt.buffer();
			int termLen = termAtt.length();
			String w = new String(termBuffer, 0, termLen);
			if (w.length()<MINWORDLEN)
				w = "";
			else {
				if (spelling!=null)
					w = spelling.correct(w);
				if (stopWording!=null)
					w = stopWording.stopWord(w);
			}
			if (w.length()>0)
				scrubbedWords.add(w);
		}
		ts.end();
		ts.close();
		analyzer.close();
	}
	public List<String> getScrubbedWords() {
		return scrubbedWords;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String w : scrubbedWords) {
			if (sb.length()>0)
				sb.append('\n');
			sb.append(w);
		}
		return sb.toString();
	}
}
