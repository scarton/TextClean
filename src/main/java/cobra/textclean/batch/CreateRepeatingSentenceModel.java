package cobra.textclean.batch;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.CollectSentencesSNLP;
import cobra.textclean.RepeatingSentenceModel;

/**
 * <p>Creates a repeating sentence model from a  long list of text documents. 
 * Intended to be run after the regex cleaner.</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */
public class CreateRepeatingSentenceModel {
	final static Logger logger = LoggerFactory.getLogger(CreateRepeatingSentenceModel.class);
	final static int INDEX_LIMIT = Integer.MAX_VALUE;

	public static void main(String[] args) throws IOException {
		File srcF = new File(args[0]);
		CollectSentencesSNLP sentCollector = new CollectSentencesSNLP();

		File[] listOfFiles = srcF.listFiles((File file) -> file.getName().endsWith(".txt"));
		for (int i = 0; i < Math.min(INDEX_LIMIT, listOfFiles.length); i++) {
			if (listOfFiles[i].isFile()) {
//				logger.debug("File " + listOfFiles[i].getName());
				String text = FileUtils.readFileToString(listOfFiles[i]);
				if (text != null && text.length() > 250) {
					sentCollector.parseSentences(text);
				}
			}
			if (i%1000==0)
				logger.info("{}",i);
				
		}
		RepeatingSentenceModel.exportRepeatingSentenceModel(sentCollector.getSentenceCache(), srcF.getParentFile(),50);
		logger.info("End of Job...");
	}
}