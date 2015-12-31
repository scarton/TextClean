package cobra.textclean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>loads list of stopwords. Provides method to remove word if it is in the list.
 * </p>
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Dec 30, 2015
 *
 */
public class Stopwording {
	private static final int MINWORDLEN = 2;
	private Map<String,Integer> stopwords;

	public Stopwording() throws IOException {
		loadStopwords();
	}
	private void loadStopwords() throws IOException {
		InputStream in = this.getClass().getResourceAsStream("/stopwords_en.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		stopwords = new HashMap<>();
		String line = reader.readLine();
		while (line != null) {
			// System.out.println(line);
			String sw = line.trim();
			if (sw.length() >= MINWORDLEN && !line.startsWith("#")) {
				stopwords.put(sw,null);
			}
			line = reader.readLine();
		}
	}
	public String stopWord(String word) {
		if (!stopwords.containsKey(word))
			return word;
		else
			return "";
	}
}
