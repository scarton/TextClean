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
	final static int INDEX_LIMIT = 100; //Integer.MAX_VALUE;
	static int c=0;
	
	public void processFiles(File dir, File outP) throws IOException {
		logger.info("Processing directory {}",dir.getPath());
		File[] listOfFiles = dir.listFiles();
		RegexCleaner cleaner = new RegexCleaner();
		for (int i = 0; c < INDEX_LIMIT && i<listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if (listOfFiles[i].getName().endsWith(".txt")) {
//					logger.debug("File " + listOfFiles[i].getName());
					String text = FileUtils.readFileToString(listOfFiles[i]);
					if (text != null && text.length() > 250) {
						String fn = FilenameUtils.getBaseName(listOfFiles[i].getName()).trim();
						cleaner.cleanTextByRegex(text);
						File outF = new File(outP.getPath()+'/'+fn+".txt");
						IO.putContent(outF, cleaner.getCleanedText());
					}
				}
				if (c%1000==0)
					logger.info("{}",c);
				c++;
			} else {
				processFiles(listOfFiles[i], outP);
			}
		}
			
	}

	public static void main(String[] args) throws IOException {
		logger.info("Starting REGEX cleanups - up to {} Files",INDEX_LIMIT);
		logger.info("  From {}",args[0]);
		logger.info("  To {}",args[1]);
		File srcF = new File(args[0]);
		File outP = new File(args[1]);
		if(!outP.exists() && !outP.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + args[1]);
		}
		RegexFiles processer = new RegexFiles();
		processer.processFiles(srcF, outP);
		logger.info("End of Job...");
	}
}