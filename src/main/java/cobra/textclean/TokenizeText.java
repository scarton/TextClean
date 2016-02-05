package cobra.textclean;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.util.InvalidFormatException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

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
	private List<String> scrubbedForms;
	private List<String> scrubbedSentences;
	private Spelling spelling;
	private Stopwording stopWording;
	private Properties props = new Properties();
	private StanfordCoreNLP pipeline;

	/**
	 * <p>Creates a spelling tool and sets it for use.</p>
	 * @throws IOException
	 */
	public void loadSpelling() throws IOException {
		spelling = new Spelling();
		spelling.loadModels();
	}
	/**
	 * <p>Creates a stopwording tool and sets it for use.</p>
	 * @throws IOException
	 */
	public void loadStopWording() throws IOException {
		stopWording = new Stopwording();
	}
	public void loadSNLP() throws InvalidFormatException, IOException {
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
//		props.setProperty("ssplit.newlineIsSentenceBreak", "two");
		pipeline = new StanfordCoreNLP(props);
	}
	
	public String stemTerm (String term) {
	    PorterStemmer stemmer = new PorterStemmer();
	    return stemmer.stem(term);
	}
	public void extractWordsSNLP(String text) {
		scrubbedWords = new ArrayList<>();
		scrubbedForms = new ArrayList<>();
		scrubbedSentences = new ArrayList<>();
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    	for (CoreMap sentence : sentences) {
    		StringBuilder senText = new StringBuilder();
    		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
        		StringBuilder formsText = new StringBuilder();
    			String tk = token.get(TextAnnotation.class);
    			String w = tk;
    			String pos = token.get(PartOfSpeechAnnotation.class);
    			String lemma = token.get(LemmaAnnotation.class);
    			if (w.length()<MINWORDLEN)
    				w = "";
    			else {
    				if (!pos.equals("NNP")) {
	    				if (spelling!=null)
	    					w = spelling.correct(w)[0];
	    				if (stopWording!=null)
	    					w = stopWording.stopWord(w);
    				}
    			}
    			if (w.length()>0) {
    				scrubbedWords.add(w);
    				if (!pos.equals("NNP")) {
    					senText.append((senText.length()>0?" ":"")+stemTerm(w));
    				} else {
    					senText.append((senText.length()>0?" ":"")+w);
    				}
	    			formsText.append(tk);
	    			formsText.append('\t'+pos);
	    			formsText.append("\t"+w);
	    			formsText.append("\t"+lemma);
	    			formsText.append("\t"+stemTerm(w));
	    			scrubbedForms.add(formsText.toString());
    			}
			}
    		scrubbedSentences.add(senText.toString());
//    		logger.debug("Sentence: {}",senText.toString());
	    }
	}

	public void extractWords(String text) throws IOException {
		Analyzer analyzer = new StandardAnalyzer();
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
					w = spelling.correct(w)[0];
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
	public String getScrubbedForms() {
		StringBuilder sb = new StringBuilder();
		sb.append("Token\tPOS\tSpelled\tLemma\tStemmed");
		for (String w : scrubbedForms) {
			if (sb.length()>0)
				sb.append('\n');
			sb.append(w);
		}
		return sb.toString();
	}
	public String getScrubbedSentences() {
		StringBuilder sb = new StringBuilder();
		for (String w : scrubbedSentences) {
			if (sb.length()>0)
				sb.append(".\n");
			sb.append(w);
		}
		return sb.toString();
	}
}
