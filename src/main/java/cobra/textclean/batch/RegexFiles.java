package cobra.textclean.batch;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.RegexCleaner;
import cobra.textclean.util.IO;

/**
 * <p>Performs regex cleanup of text in files in the specified directory. Writes to the specified output directory</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */
public class RegexFiles {
	final static Logger logger = LoggerFactory.getLogger(RegexFiles.class);
	final static int INDEX_LIMIT = Integer.MAX_VALUE;

	public static void main(String[] args) throws IOException {
		File srcF = new File(args[0]);
		File outP = new File(args[1]);

		File[] listOfFiles = srcF.listFiles((File file) -> file.getName().endsWith(".txt"));
		RegexCleaner cleaner = new RegexCleaner();
		for (int i = 0; i < Math.min(INDEX_LIMIT, listOfFiles.length); i++) {
			if (listOfFiles[i].isFile()) {
//				logger.debug("File " + listOfFiles[i].getName());
				String text = FileUtils.readFileToString(listOfFiles[i]);
				if (text != null && text.length() > 250) {
					String fn = FilenameUtils.getBaseName(listOfFiles[i].getName()).trim();
					cleaner.cleanTextByRegex(text);
					File outF = new File(outP.getPath()+'/'+fn+".txt");
					IO.putContent(outF, cleaner.getCleanedText());
				}
			}
			if (i%1000==0)
				logger.info("{}",i);
				
		}
		logger.info("End of Job...");
	}
}