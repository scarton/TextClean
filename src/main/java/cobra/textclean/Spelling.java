package cobra.textclean;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * <p>Spelling Checker/Corrector. Probablistic approach based on Norvic: http://norvig.com/spell-correct.html
 * Expects a previously built model with correct words and their frequency of use.
 * Assumes word is in lower case
 * 
 * Expects a "model" in the form of a .gz of a binary serialization from SpellingModelBuilder. It can also handle a two models
 * where the first is used and then the second only if a correction was not found in the first. 
 * This is handy of having a model that is more industry-specific used first, followed by a second model that is more general.
 * It comes from this kind of use case: 
 * 
 * The word "Ankle" spelled incorrectly as Ankel is corrected to Angel (as this is more probably).
 * 
 * But having a medical industry model available to check first would correct it to ankle.
 * </p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */

/**
 * <p>
 * </p>
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Jan 6, 2016
 *
 */
class Spelling {
	private String MODEL_NAME = "spelling.model";
	private String INDUSTRY_MODEL_NAME = null;

	private List<Map<String, Integer>> models;

	/**
	 * Loads and returns the spelling model from a .gz resource on the classpath
	 * 
	 * @return
	 * @throws IOException
	 */
	public void loadModels() throws IOException {
		models = new ArrayList<>();
		if (INDUSTRY_MODEL_NAME!=null) {
			InputStream in = Spelling.class.getResourceAsStream('/' + INDUSTRY_MODEL_NAME + ".gz");
			this.models.add(loadModel(in));
		}
		{
			InputStream in = Spelling.class.getResourceAsStream('/' + MODEL_NAME + ".gz");
			this.models.add(loadModel(in));
		}
		
	}

	/**
	 * Loads and returns the spelling model - a map of words and counts
	 * 
	 * @return
	 * @throws IOException
	 */
	public Map<String, Integer> loadModel(InputStream in) throws IOException {
		Map<String, Integer> model = new HashMap<String, Integer>();
		GZIPInputStream zis = new GZIPInputStream(in);
		DataInputStream dis = new DataInputStream(zis);
		int l = dis.readInt();
		for (int i = 0; i < l; i++) {
			String k = dis.readUTF();
			Integer v = dis.readInt();
			model.put(k, v);
		}

		return model;
	}

	/**
	 * <p>
	 * Creates a list of alternate word possibilities for this word. These are
	 * not necessarily correctly spelled - just alternative forms. Assumes word
	 * is in lower case There are 4 possible alternates created for the word:
	 * <ul>
	 * <li>Deletions - where one letter is removed, producing a set of words l
	 * in size. (word would create ord, wrd, wod, wor)</li>
	 * <li>Transpositions - where adjacent letters are swapped, creating word
	 * set l-1 in size (word->owrd, wrod, wodr)</li>
	 * <li>Replacements - where each letter is replaced by another a-z letter
	 * creating a word set of 26(l+1) new words. (word->aord, bord, cord...)</li>
	 * <li>Insertions - where each character has added immediately following,
	 * each of a-z, creating a word set of 54n+25.</li>
	 * </ul>
	 * Wondering if the replacements would work better if we chose from
	 * characters near the source char on the keyboard? Also wondering if we
	 * should also include numbers in the various options.
	 * </p>
	 * 
	 * @param word
	 * @return
	 */
	private final ArrayList<String> edits(String word) {
		ArrayList<String> result = new ArrayList<String>();
		// create word variations with 1 character deleted.
		for (int i = 0; i < word.length(); ++i)
			result.add(word.substring(0, i) + word.substring(i + 1));
		// Create word variations with 1 character swapped
		for (int i = 0; i < word.length() - 1; ++i)
			result.add(word.substring(0, i) + word.substring(i + 1, i + 2)
					+ word.substring(i, i + 1) + word.substring(i + 2));
		// create word variations with each character replaced by [a-z].
		for (int i = 0; i < word.length(); ++i)
			for (char c = 'a'; c <= 'z'; ++c)
				result.add(word.substring(0, i) + String.valueOf(c)
						+ word.substring(i + 1));
		// create word variations with [a-z] added after each character.
		for (int i = 0; i <= word.length(); ++i)
			for (char c = 'a'; c <= 'z'; ++c)
				result.add(word.substring(0, i) + String.valueOf(c)
						+ word.substring(i));
		return result;
	}

	/**
	 * <p>
	 * Performs spelling correction on a word. Builds a Map of candidates by
	 * creating the word variants and then removing those that are not real
	 * words. Returns the word most probably the correction. Norvik postulates
	 * running each word in the variants list through the edits function to
	 * account for character errors that are 2 apart.
	 * </p>
	 * 
	 * @param word
	 * @return
	 */
	public final FixedWords correct(String word, Map<String, Integer> lmodel) {
		if (lmodel.containsKey(word))
			return new FixedWords(false,word);
		// get a list of all variants
		ArrayList<String> list = edits(word);
		// this will hold a subset of the model - words in the variants that are
		// in the model (are actual words) and their frequency score.
		HashMap<Integer, String> candidates = new HashMap<Integer, String>();
		for (String s : list)
			if (lmodel.containsKey(s))
				candidates.put(lmodel.get(s), s);
		// If we found candidates, we return the one with the highest
		// score/frequency
		if (candidates.size() > 0)
			return new FixedWords(true,candidates.get(Collections.max(candidates.keySet())));
		// Still no joy, go through the candidates again and create variants on
		// the variants (Norviks postulate above).
		// Add any real words and their frequency to the Map
		for (String s : list)
			for (String w : edits(s))
				if (lmodel.containsKey(w))
					candidates.put(lmodel.get(w), w);
		// If we have something n the map, return the word with the highest
		// score. Otherwise return the unchanged word.
		return candidates.size() > 0 ? 
				new FixedWords(true,candidates.get(Collections.max(candidates.keySet()))) : 
					new FixedWords(false,word);
	}
	class FixedWords {
		public boolean fixed=false;
		public String word="";
		public FixedWords(boolean fixed, String word) {
			this.fixed=fixed;
			this.word=word;
		}
	}

	/**
	 * <p>
	 * Corrects spelling of an array of words. Assumes the words are in lower case.
	 * 
	 * Tries the "industry" model first. If that dowsn't cause a fix to the word, tries the general model.
	 * If there is only one model, that's the only one used.
	 * case.
	 * </p>
	 * 
	 * @param words
	 * @return
	 */
	public final String[] correct(String... words) {
		ArrayList<String> fixed = new ArrayList<String>();
		for (String word : words) {
			FixedWords fw = correct(word, models.get(0));
			if (!fw.fixed && models.size()>1) {
				fw = correct(word, models.get(1));
			}
			fixed.add(fw.word);
		}
		return fixed.toArray(new String[0]);
	}

	/**
	 * <p>
	 * To test with... supply words to check as args to the 
	 * </p>
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		Spelling spelling = new Spelling();
		spelling.loadModels();
		if (args.length > 0) {
			String[] words = args;
			String[] cwds = spelling.correct(words);
			for (int i = 0; i < words.length; i++)
				System.out.println("'" + words[i] + "' - '" + cwds[i] + "'");
		}
	}

	public void setMODEL_NAME(String m) {
		MODEL_NAME = m;
	}

	public void setINDUSTRY_MODEL_NAME(String im) {
		INDUSTRY_MODEL_NAME = im;
	}
}
