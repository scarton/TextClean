package cobra.textclean;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

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
 * <p>removes sentences from text based on frequent occurences. Uses RepeatingSentence Model</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 23, 2015
 *
 */
public class RemoveSentencesSNLP {
	private final static Logger logger = LoggerFactory.getLogger(RemoveSentencesSNLP.class);

	public static final int COUNTMIN=100;
    private CountingCache repeatingSentenceModel;
	private Properties props = new Properties();
	private StanfordCoreNLP pipeline;
	private String results;
    
    public RemoveSentencesSNLP() throws IOException {
    	repeatingSentenceModel = RepeatingSentenceModel.loadModel();
		//logger.debug("Loading sentence model...");
//		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		props.setProperty("annotators", "tokenize, ssplit");
		props.setProperty("ssplit.newlineIsSentenceBreak", "two");
		props.setProperty("tokenize.options", "ptb3Escaping=false");
		pipeline = new StanfordCoreNLP(props);
    }
	public void reduceSentences(String text) {
		StringBuilder sb = new StringBuilder();
		Annotation document = new Annotation(text);
		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    	for (CoreMap sentence : sentences) {
    		StringBuilder senText = new StringBuilder();
    		for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
    			String word = token.get(TextAnnotation.class);
    			senText.append((senText.length()>0?" ":"")+word);
			}
    		String sen = senText.toString();
//    		logger.debug("Sentence: {}",senText.toString());
    		String md5 = MD5.hash(sen,32);
    		if (repeatingSentenceModel.containsKey(md5)) {
//    			logger.debug("Ignoring Sentence with code {}: {}",md5,sen);
    		} else {
    			if (sb.length()>0)
    				sb.append(". \n");
    			sb.append(sen);
//    			logger.debug("Appending Sentence: {}",sen);
    		}
	    }
    	results = sb.toString();
	}
	public String getResults() {
		return results;
	}
}