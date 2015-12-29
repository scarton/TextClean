package cobra.textclean;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
 
/**
 * <p></p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */

class Spelling {
	private static final String MODEL_NAME = "spelling.model";

    private Map<String, Integer> model;
    
    public Spelling() throws IOException {
    	this.model = loadModel();
    }

	/**
	 * Loads and returns the spelling model from a .gz resource on the classpath
	 * 
	 * @return
	 * @throws IOException
	 */
	public Map<String, Integer> loadModel() throws IOException {
		InputStream in = Spelling.class.getResourceAsStream('/' + MODEL_NAME + ".gz");
		return loadModel(in);
	}

	/**
	 * Loads and returns the spelling model - a map of words and counts
	 * 
	 * @return
	 * @throws IOException
	 */
	public Map<String, Integer> loadModel(InputStream in)
			throws IOException {
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

    private final ArrayList<String> edits(String word) {
        ArrayList<String> result = new ArrayList<String>();
        for(int i=0; i < word.length(); ++i) result.add(word.substring(0, i) + word.substring(i+1));
        for(int i=0; i < word.length()-1; ++i) result.add(word.substring(0, i) + word.substring(i+1, i+2) + word.substring(i, i+1) + word.substring(i+2));
        for(int i=0; i < word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i+1));
        for(int i=0; i <= word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
        return result;
    }
 
    public final String correct(String word) {
        if(model.containsKey(word)) return word;
        ArrayList<String> list = edits(word);
        HashMap<Integer, String> candidates = new HashMap<Integer, String>();
        for(String s : list) if(model.containsKey(s)) candidates.put(model.get(s),s);
        if(candidates.size() > 0) return candidates.get(Collections.max(candidates.keySet()));
        for(String s : list) for(String w : edits(s)) if(model.containsKey(w)) candidates.put(model.get(w),w);
        return candidates.size() > 0 ? candidates.get(Collections.max(candidates.keySet())) : word;
    }
 
    public final String[] correct(String ... words) {
     ArrayList<String> fixed = new ArrayList<String>();
     for (String word : words) {
      fixed.add(correct(word));
     }
     return fixed.toArray(new String[0]);
    }
 
    public static void main(String args[]) throws IOException {
     Spelling spelling = new Spelling();
        if(args.length > 0) {
         String[] words = args;
         String[] cwds = spelling.correct(words);
         for (int i=0; i<words.length; i++)
          System.out.println("'"+words[i]+"' - '"+cwds[i]+"'");
        }
    }
 
	public  void setModel(Map<String, Integer> model) {
		this.model = model;
	}
}
