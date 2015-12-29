package cobra.textclean;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.util.CountingCache;
import cobra.textclean.util.MD5;



/**
 * <p>removes sentences from text based on frequent occurences</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 23, 2015
 *
 */
public class RemoveSentences {
	private final static Logger logger = LoggerFactory.getLogger(RemoveSentences.class);

	private static final String MODEL_NAME = "repeating-sentences.model";
    private CountingCache repeatingSentenceModel;
	private SentenceDetectorME sentenceDetector;
	private SentenceModel sentModel;
    
    public RemoveSentences() throws IOException {
    	repeatingSentenceModel = loadModel();
		InputStream modelIn = this.getClass().getClassLoader().getResourceAsStream("en-sent.bin");
		//logger.debug("Loading sentence model...");
		sentModel = new SentenceModel(modelIn);
		sentenceDetector = new SentenceDetectorME(sentModel);
    }
    public static boolean modelExists() {
    	return RemoveSentences.class.getClassLoader().getResource(MODEL_NAME + ".gz")!=null;
    }
	/**
	 * Loads and returns the spelling model - a map of words and counts
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CountingCache loadModel() throws IOException {
		logger.debug("loading Sentence Reduction Model from {}",MODEL_NAME+".gz");
		InputStream in = RemoveSentences.class.getClassLoader().getResourceAsStream(MODEL_NAME + ".gz");
		CountingCache repeatingSentenceModel = new CountingCache();
		GZIPInputStream zis = new GZIPInputStream(in);
		DataInputStream dis = new DataInputStream(zis);
		boolean eof=false;
		while (!eof) {
			try {
			String k = dis.readUTF();
			Integer v = dis.readInt();
			logger.debug("{}/{}",k,v);
			repeatingSentenceModel.put(k, v);
			} catch (EOFException e) {
				eof=true;
			}
		}
		return repeatingSentenceModel;
	}
	public String reduceSentences(String text) {
		StringBuilder sb = new StringBuilder();
    	Span[] sentences = sentenceDetector.sentPosDetect(text);
    	for (Span sentence : sentences) {
    		CharSequence senText = sentence.getCoveredText(text);
    		//logger.debug("Sentence {}/{}: {}",sentence.getStart(),sentence.getEnd(),senText);
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
