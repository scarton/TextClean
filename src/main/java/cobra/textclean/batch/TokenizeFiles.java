package cobra.textclean.batch;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.TokenizeText;
import cobra.textclean.util.FileName;
import cobra.textclean.util.IO;

/**
 * <p></p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */
public class TokenizeFiles {
	final static Logger logger = LoggerFactory.getLogger(TokenizeFiles.class);
	final static int INDEX_LIMIT = Integer.MAX_VALUE;

	public static void main(String[] args) throws IOException {
		File srcF = new File(args[0]);
		File outP = new File(args[1]);

		File[] listOfFiles = srcF.listFiles((File file) -> file.getName().startsWith("ENR") && file.getName().endsWith(".txt"));
		TokenizeText tokenizer = new TokenizeText();
		tokenizer.loadSpelling();
		tokenizer.loadStopWording();
		for (int i = 0; i < Math.min(INDEX_LIMIT, listOfFiles.length); i++) {
			if (listOfFiles[i].isFile()) {
//				logger.debug("File " + listOfFiles[i].getName());
				String text = FileUtils.readFileToString(listOfFiles[i]);
				if (text != null) {
//					logger.debug("Cleaned text\n{}",text);
					String fn = FileName.baseName(listOfFiles[i].getName()).trim();
					tokenizer.extractWords(text);
					File outF = new File(outP.getPath()+'/'+fn+".txt");
					IO.putContent(outF, tokenizer.toString());
				}
			}
			if (i%1000==0)
				logger.info("{}",i);
		}
		logger.info("End of Job...");
	}
}