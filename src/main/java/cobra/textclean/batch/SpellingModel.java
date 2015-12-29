package cobra.textclean.batch;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Creates a serialized Map from the "big.txt" file that represents a model -
 * unique words and counts. Also, a static method to load the map from the
 * serialization.
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Dec 22, 2015
 *
 */
class SpellingModel {
	private static final String MODEL_NAME = "spelling.model";

	/**
	 * Reads the file, tokenizes and builds Hashmap
	 * 
	 * @param file
	 * @throws IOException
	 */
	public Map<String, Integer> loadBigText(File file) throws IOException {
		Map<String, Integer> nWords = new HashMap<String, Integer>();
		BufferedReader in = new BufferedReader(new FileReader(file));
		Pattern p = Pattern.compile("\\w+");
		for (String temp = ""; temp != null; temp = in.readLine()) {
			Matcher m = p.matcher(temp.toLowerCase());
			while (m.find())
				nWords.put((temp = m.group()),
						nWords.containsKey(temp) ? nWords.get(temp) + 1 : 1);
		}
		in.close();
		return nWords;
	}

	/**
	 * Creates a simple field-value pair serialization from the HashMap
	 * 
	 * @param out
	 * @throws IOException
	 */
	public void exportModel(Map<String, Integer> nWords, File path)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(path.getPath() + "/"
				+ MODEL_NAME + ".gz");
		GZIPOutputStream zos = new GZIPOutputStream(fos);
		DataOutputStream dos = new DataOutputStream(zos);
		dos.writeInt(nWords.size());
		for (Entry<String, Integer> entry : nWords.entrySet()) {
			dos.writeUTF(entry.getKey());
			dos.writeInt(entry.getValue());
		}
		dos.close();
	}

	public static void main(String args[]) throws IOException {
		if (args.length > 0) {
			File bigTxt = new File(args[0]);
			File path = bigTxt.getParentFile();
			SpellingModel modelBuilder = new SpellingModel();
			Map<String, Integer> nWords = modelBuilder.loadBigText(bigTxt);
			modelBuilder.exportModel(nWords, path);
		} 
	}
}
