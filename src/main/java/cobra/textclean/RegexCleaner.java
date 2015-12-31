package cobra.textclean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Cleanses text by applying a list of regular expressions
 * </p>
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Dec 22, 2015
 *
 */
public class RegexCleaner {
	final static Logger logger = LoggerFactory.getLogger(RegexCleaner.class);
	private List<String> regexes;
	private String cleanedText;

	public RegexCleaner() throws IOException {
		loadRegexes();
	}

	/**
	 * <p>
	 * Loads a list of regular expressions from a classpath resource (file).
	 * Parses each into a List.
	 * </p>
	 * 
	 * @throws IOException
	 */
	private void loadRegexes() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("/cleaning.regexes.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		regexes = new ArrayList<String>();
		String line = reader.readLine();
		while (line != null) {
			String rx = line.trim();
			if (!line.startsWith("#")) {
				regexes.add(rx);
			}
			line = reader.readLine();
		}
	}

	/**
	 * <p>
	 * Cleanses lines of text. Runs each regex over the entire line and replaces
	 * each match with an empty string.
	 * </p>
	 * 
	 * @param raw
	 */
	public void cleanTextByRegex(String raw) {
		this.cleanedText = raw != null ? raw : "";
		for (String regex : regexes) {
			this.cleanedText = this.cleanedText.replaceAll(regex, "");
		}
	}

	public String getCleanedText() {
		return cleanedText;
	}

}