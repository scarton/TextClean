package cobra.textclean.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class IO {
    /**
     * <p>Writes the string to the specified file.</p>
     * @param p
     * @param s
     * @throws IOException
     */
    public static void putContent(String p, String s) throws IOException {
            Writer writer = new BufferedWriter(
                            new OutputStreamWriter(new FileOutputStream(p)));
            writer.write(s);
            writer.close();
    }
    /**
     * <p>Writes the string to the specified file.</p>
     * @param p
     * @param s
     * @throws IOException
     */
    public static void putContent(File p, String s) throws IOException {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(p)));
            writer.write(s);
            writer.close();
    }

}
