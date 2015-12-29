package cobra.textclean;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

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
public class CollectSentences {
	private final static Logger logger = LoggerFactory.getLogger(CollectSentences.class);
	private static final String MODEL_NAME = "repeating-sentences.model";
	private CountingCache sentenceCache;
	private SentenceDetectorME sentenceDetector;
	private SentenceModel sentModel;
	
	public CollectSentences() throws InvalidFormatException, IOException {
		InputStream modelIn = this.getClass().getClassLoader().getResourceAsStream("en-sent.bin");
		//logger.debug("Loading sentence model...");
		sentModel = new SentenceModel(modelIn);
		sentenceDetector = new SentenceDetectorME(sentModel);
		if (RemoveSentences.modelExists()) {
			sentenceCache = RemoveSentences.loadModel();
		} else {
			sentenceCache = new CountingCache();
		}
	}
	public void parseSentences(String text) {
    	Span[] sentences = sentenceDetector.sentPosDetect(text);
    	for (Span sentence : sentences) {
    		CharSequence senText = sentence.getCoveredText(text);
    		//logger.debug("Sentence {}/{}: {}",sentence.getStart(),sentence.getEnd(),senText);
    		String md5 = MD5.hash(senText.toString(),32);
    		sentenceCache.incr(md5);
	    }
	}
	public void exportRepeatingSentenceModel(File path, int threshold)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(path.getPath() + "/" + MODEL_NAME + ".gz");
		GZIPOutputStream zos = new GZIPOutputStream(fos);
		DataOutputStream dos = new DataOutputStream(zos);
		
		for (Entry<String, Integer> entry : sentenceCache.entrySet()) {
			if (entry.getValue()>=threshold) {
				dos.writeUTF(entry.getKey());
				dos.writeInt(entry.getValue());
				logger.debug("{}/{}",entry.getKey(),entry.getValue());
			}
		}
		dos.close();
	}
}
