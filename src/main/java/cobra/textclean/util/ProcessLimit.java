package cobra.textclean.util;

/**
 * @author steve
 *
 */
public class ProcessLimit { 
	
	public static int get() {
		String smax = System.getenv("MAX");
		if (smax!=null && smax.length()>0 && smax.matches("^[0-1]*$"))
			return Integer.parseInt(smax);
		else
			return Integer.MAX_VALUE;
	}

}
