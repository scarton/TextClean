package cobra.textclean.util;

/**
 * <p>Utilities for file names</p>
 * @author Steve Carton (stephen.e.carton@usdoj.gov)
 * Dec 23, 2015
 *
 */
public class FileName {
	private FileName(){}
	public static String baseName(String f) {
		return f.split("\\.")[0];
	}

}
