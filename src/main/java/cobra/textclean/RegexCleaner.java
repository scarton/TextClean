package cobra.textclean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Cleanses text by applying a list of regular expressions. Precompiles the regex expressions into patterns.
 * </p>
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Dec 22, 2015
 *
 */
public class RegexCleaner implements Serializable {
	private static final long serialVersionUID = 3719662831635489335L;
	final static Logger logger = LoggerFactory.getLogger(RegexCleaner.class);
	public List<Pattern> regexes;
	private String cleanedText;

	public RegexCleaner() throws IOException {
		loadRegexes();
	}

	/**
	 * <p>
	 * Loads a list of regular expressions from a classpath resource (file).
	 * Parses each into a List.
	 * Pattern.compile(regex).matcher(str).replaceAll(repl)
	 * </p>
	 * 
	 * @throws IOException
	 */
	private void loadRegexes() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("/cleaning.regexes.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		regexes = new ArrayList<>();
		String line = reader.readLine();
		while (line != null) {
			String rx = line.trim();
			if (!line.startsWith("#")) {
				regexes.add(Pattern.compile(rx, Pattern.CASE_INSENSITIVE));
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
		for (Pattern regex : regexes) {
			this.cleanedText = regex.matcher(this.cleanedText).replaceAll("");
		}
	}

	public void cleanTextByRegexStream(String raw) {
//		this.cleanedText = regexes.stream().map(r -> r.matcher(raw).replaceAll(""));
	}

	public String getCleanedText() {
		return cleanedText;
	}

}