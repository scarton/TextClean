package cobra.textclean;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.util.CountingCache;
import cobra.textclean.util.MD5;

/**
 * <p>Builds a list of MD5 hashes of sentences that repeat regularly.</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */
public class CollectSentencesONLP {
	private final static Logger logger = LoggerFactory.getLogger(CollectSentencesONLP.class);
	private CountingCache sentenceCache;
	private SentenceDetectorME sentenceDetector;
	private SentenceModel sentModel;
	
	public CollectSentencesONLP() throws InvalidFormatException, IOException {
		InputStream modelIn = this.getClass().getClassLoader().getResourceAsStream("en-sent.bin");
		//logger.debug("Loading sentence model...");
		sentModel = new SentenceModel(modelIn);
		sentenceDetector = new SentenceDetectorME(sentModel);
		sentenceCache = RepeatingSentenceModel.loadModel();
	}
	public void parseSentences(String text) {
    	Span[] sentences = sentenceDetector.sentPosDetect(text);
    	for (Span sentence : sentences) {
    		CharSequence senText = sentence.getCoveredText(text);
    		logger.debug("Sentence {}/{}: {}",sentence.getStart(),sentence.getEnd(),senText);
    		String md5 = MD5.hash(senText.toString(),32);
    		sentenceCache.incr(md5);
	    }
	}
}