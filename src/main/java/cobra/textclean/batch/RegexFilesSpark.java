package cobra.textclean.batch;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;
import cobra.textclean.RegexCleaner;
import cobra.textclean.util.IO;

/**
 * <p>
 * </p>
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Jan 8, 2016
 *
 */
public class RegexFilesSpark {
	final static Logger logger = LoggerFactory.getLogger(RegexFiles.class);
	final static int INDEX_LIMIT = 1000; //Integer.MAX_VALUE;
	private JavaSparkContext jsc; 
	private SparkConf sparkConf;
	static int c = 0;
	
	public void processFiles(File dir, File outP) throws IOException {
		processFilesInDir(dir, outP);
		File[] listOfFiles = dir.listFiles();
		for (int i = 0; c < INDEX_LIMIT && i<listOfFiles.length; i++) {
			if (listOfFiles[i].isDirectory()) {
				processFiles(listOfFiles[i], outP);
			}
		}
	}

	private void createSparkContext() {
		sparkConf = new SparkConf().setAppName("RegexCleanup")
				.setMaster("local[*]").set("spark.driver.memory", "4g")
				.set("spark.executor.memory", "4g");
		jsc = new JavaSparkContext(sparkConf);
	}
	
	private void closeSparkContext() {
		jsc.close();
	}


	public void processFilesInDir(File dir, File outP) throws IOException {
		logger.info("Processing directory {}", dir.getPath());
		RegexCleaner cleaner = new RegexCleaner();
		logger.info("Created Cleaner");
		JavaPairRDD<String, String> files = jsc.parallelizePairs(jsc
				.wholeTextFiles(dir.getPath()).filter(f -> {
					logger.info("Considering {}",f._1);
					return f._1.endsWith(".txt");
				}).take(INDEX_LIMIT));
		JavaPairRDD<String, String> processedFiles = files.mapToPair(entry -> {
			cleaner.cleanTextByRegex(entry._2);
			return Tuple2.apply(entry._1, cleaner.getCleanedText());
		});
		processedFiles.foreach(pf -> {
			String fn = FilenameUtils.getBaseName(pf._1).trim();
			File outF = new File(outP.getPath()+'/'+fn+".txt");
			IO.putContent(outF, pf._2);
			c++;
		});
	}

	public static void main(String[] args) throws IOException {
		logger.info("Starting REGEX cleanups - up to {} Files", INDEX_LIMIT);
		logger.info("  From {}", args[0]);
		logger.info("  To {}", args[1]);
		File srcF = new File(args[0]);
		File outP = new File(args[1]);
		if (!outP.exists() && !outP.mkdirs()) {
			throw new IllegalStateException("Couldn't create dir: " + args[1]);
		}
		RegexFilesSpark processer = new RegexFilesSpark();
		processer.createSparkContext();
		processer.processFiles(srcF, outP);
		processer.closeSparkContext();
		logger.info("End of Job...");
	}
}
