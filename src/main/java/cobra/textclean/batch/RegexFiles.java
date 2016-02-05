package cobra.textclean.batch;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cobra.textclean.RegexCleaner;
import cobra.textclean.util.IO;
import cobra.textclean.util.ProcessLimit;

/**
 * <p>Performs regex cleanup of text in files in the specified directory. Writes to the specified output directory</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */
public class RegexFiles {
	final static Logger logger = LoggerFactory.getLogger(RegexFiles.class);
	
	public int processFiles(File dir, File outP, int cur, int limit) throws IOException {
		logger.info("Processing directory {}",dir.getPath());
		File[] listOfFiles = dir.listFiles();
		RegexCleaner cleaner = new RegexCleaner();
		for (int i = 0; cur < ProcessLimit.get() && i<listOfFiles.length; i++) {
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
				if (cur%1000==0)
					logger.info("{}",cur);
				cur++;
			} else {
				cur = processFiles(listOfFiles[i], outP, cur, limit);
			}
		}
		return cur;
	}

	public static void main(String[] args) throws IOException {
		int l = ProcessLimit.get();
		int c=0;
		logger.info("Starting REGEX cleanups - up to {} Files",l);
		logger.info("  From {}",args[0]);
		logger.info("  To {}",args[1]);
		File srcF = new File(args[0]);
		File outP = new File(args[1]);
		if(!outP.exists() && !outP.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + args[1]);
		}
		RegexFiles processer = new RegexFiles();
		processer.processFiles(srcF, outP,c,l);
		logger.info("End of Job...");
	}
}