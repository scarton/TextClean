package cobra.textclean;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.util.CountingCache;

/**
 * <p>Methods to load and to serialize the Repeating Sentence Model</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 30, 2015
 *
 */
public class RepeatingSentenceModel {
	private final static Logger logger = LoggerFactory.getLogger(RepeatingSentenceModel.class);
	private static final String MODEL_NAME = "repeating-sentences.model";
	public static final int COUNTMIN=100;
	private RepeatingSentenceModel () {}
    public static boolean modelExists() {
    	return RepeatingSentenceModel.class.getClassLoader().getResource(MODEL_NAME + ".gz")!=null;
    }
	/**
	 * Loads and returns the spelling model - a map of words and counts
	 * 
	 * @return
	 * @throws IOException
	 */
	public static CountingCache loadModel() throws IOException {
		return loadModel(COUNTMIN);
	}
	public static CountingCache loadModel(Integer threshold) throws IOException {
		CountingCache repeatingSentenceModel = new CountingCache();
		if (modelExists()) {
			logger.debug("loading Sentence Reduction Model from {}",MODEL_NAME+".gz");
			InputStream in = RepeatingSentenceModel.class.getClassLoader().getResourceAsStream(MODEL_NAME + ".gz");
			GZIPInputStream zis = new GZIPInputStream(in);
			DataInputStream dis = new DataInputStream(zis);
			boolean eof=false;
			while (!eof) {
				try {
					String k = dis.readUTF();
					Integer v = dis.readInt();
					if (v>threshold) {
	//					logger.debug("{}/{}",k,v);
						repeatingSentenceModel.put(k, v);
					}
				} catch (EOFException e) {
					eof=true;
				}
			}
		}
		return repeatingSentenceModel;
	}
	public static void exportRepeatingSentenceModel(CountingCache sentenceCache, File path, int threshold)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(path.getPath() + "/" + MODEL_NAME + ".gz");
		GZIPOutputStream zos = new GZIPOutputStream(fos);
		DataOutputStream dos = new DataOutputStream(zos);
		
		for (Entry<String, Integer> entry : sentenceCache.entrySet()) {
			if (entry.getValue()>=threshold) {
				dos.writeUTF(entry.getKey());
				dos.writeInt(entry.getValue());
			}
		}
		dos.close();
	}

}
