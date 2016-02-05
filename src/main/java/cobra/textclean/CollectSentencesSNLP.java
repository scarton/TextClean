package cobra.textclean;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import opennlp.tools.util.InvalidFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.util.CountingCache;
import cobra.textclean.util.MD5;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

/**
 * <p>Builds a list of MD5 hashes of sentences that repeat regularly.</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */
public class CollectSentencesSNLP {
	private final static Logger logger = LoggerFactory.getLogger(CollectSentencesSNLP.class);
	private CountingCache sentenceCache;
	private Properties props = new Properties();
	private StanfordCoreNLP pipeline;
	
	public CollectSentencesSNLP() throws InvalidFormatException, IOException {
//		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.setProperty("annotators", "tokenize, ssplit");
		props.setProperty("ssplit.newlineIsSentenceBreak", "two");
		props.setProperty("tokenize.options", "ptb3Escaping=false");
		pipeline = new StanfordCoreNLP(props);
		sentenceCache = RepeatingSentenceModel.loadModel();
	}
	public void parseSentences(String text) {
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    	for (CoreMap sentence : sentences) {
    		StringBuilder senText = new StringBuilder();
    		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
    			String word = token.get(TextAnnotation.class);
    			senText.append((senText.length()>0?" ":"")+word);
			}
//    		logger.debug("Sentence: {}",senText.toString());
    		String md5 = MD5.hash(senText.toString(),32);
    		sentenceCache.incr(md5);
	    }
	}
	public CountingCache getSentenceCache() {
		return sentenceCache;
	}
}