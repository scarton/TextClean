package cobra.textclean.util;

import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Entended Hashmap with String keys and integer values. Adds methods to
 * increment a count value.
 * </p>
 * 
 * @author Steve Carton (stephen.e.carton@usdoj.gov) Dec 22, 2015
 *
 */
public class CountingCache extends HashMap<String, Integer> {
	private final static Logger logger = LoggerFactory.getLogger(CountingCache.class);

	private static final long serialVersionUID = 1L;
	private LinkedList<String> mfulist;
	private int cacheSize = 100000;

	public CountingCache() {
		super();
		mfulist = new LinkedList<String>();
	}

	public CountingCache(int size) {
		super();
		mfulist = new LinkedList<String>();
		this.cacheSize = size;
	}

	public Integer get1(String key) {
		if (!containsKey(key))
			put(key, 0);
		// move the key to the front and return the object
		if (this.cacheSize > 0) {
			synchronized (this) {
				if (this.containsKey(key)) {
					mfulist.remove(key);
					mfulist.addFirst(key);
					return super.get(key);
				}
			}
		}
		return null;
	}

	public Integer get(String key) {
		if (!containsKey(key))
			put(key, 0);
		return super.get(key);
	}

	public void incr(String key, Integer val) {
		put(key, this.get(key) + val);
	}

	public void incr(String key) {
		put(key, this.get(key) + 1);
	}

	public final Integer put1(String key, Integer val) {
		if (this.cacheSize > 0) {
			synchronized (this) {
				// is the cache full? pop off the bottom
				if (mfulist.size() == this.cacheSize) {
					remove(mfulist.getLast());
					mfulist.removeLast();
				}
				// Add the object and the key at the front
				super.put(key, val);
				mfulist.addFirst(key);
			}
		}
		return val;
	}

	public final String toString() {
		String ts = "Cache Size: " + this.cacheSize + " mfuList Size: "
				+ mfulist.size() + "\n";
		if (this.cacheSize > 0 && mfulist != null) {
			for (int i = 0; i < mfulist.size(); i++) {
				ts += "item " + i + "="
						+ (mfulist.get(i) == null ? "null" : mfulist.get(i))
						+ " Class: " + get(mfulist.get(i)).getClass() + "\n";
			}
		}
		return ts;
	}

	public void setCachesize(int cachesize) {
		this.cacheSize = cachesize;
	}
}