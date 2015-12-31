package cobra.textclean;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.util.CountingCache;
import cobra.textclean.util.MD5;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;


/**
 * <p>removes sentences from text based on frequent occurences</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 23, 2015
 *
 */
public class RemoveSentencesONLP {
	private final static Logger logger = LoggerFactory.getLogger(RemoveSentencesONLP.class);

    private CountingCache repeatingSentenceModel;
	private SentenceDetectorME sentenceDetector;
	private SentenceModel sentModel;
    
    public RemoveSentencesONLP() throws IOException {
    	repeatingSentenceModel = RepeatingSentenceModel.loadModel();
		InputStream modelIn = this.getClass().getClassLoader().getResourceAsStream("en-sent.bin");
		//logger.debug("Loading sentence model...");
		sentModel = new SentenceModel(modelIn);
		sentenceDetector = new SentenceDetectorME(sentModel);
    }

	public String reduceSentences(String text) {
		StringBuilder sb = new StringBuilder();
    	Span[] sentences = sentenceDetector.sentPosDetect(text);
    	for (Span sentence : sentences) {
    		CharSequence senText = sentence.getCoveredText(text);
    		logger.debug("Sentence {}/{}: {}",sentence.getStart(),sentence.getEnd(),senText);
    		String md5 = MD5.hash(senText.toString(),32);
    		if (repeatingSentenceModel.containsKey(md5)) {
//    			logger.debug("Ignoring Sentence with code {}",md5);
    		} else {
    			sb.append(senText.toString());
    		}
	    }
    	return sb.toString();
	}
}