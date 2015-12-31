package cobra.textclean.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p></p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 22, 2015
 *
 */
public class MD5 {
	private static MessageDigest hasher;
	private static void makeHasher() {
		if (hasher==null) {
			try {
				hasher = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
	}
	public static String hash(String k) {
		return hash(k, 16);
	}
	public static String hash(String k, int size) {
		makeHasher();
		byte[] kb =k.getBytes();
		byte[] kbh = hasher.digest(kb);
		String bs = new BigInteger(1,kbh).toString(size);
		return bs;
	}

}
