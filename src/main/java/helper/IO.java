package helper;


import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * Created by Arne on 07.09.2015.
 */
public class IO {
    //Reads a text file line by line. Use this when testing API with examples from /test/resources/
    public static String readFile(String file) throws IOException {
        StringBuilder bldr = new StringBuilder();
        /*for (String line: Files.readAllLines(Paths.get(file), Charset.forName("UTF-8"))) {
            //System.out.println(line);
            bldr.append(line).append('\n');
        }
        return bldr.toString();
        */

        try {
            File fileDir = new File(file);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(fileDir), "UTF8"));

            String str;

            while ((str = in.readLine()) != null) {
                //System.out.println(str);
                bldr.append(str).append('\n');
            }

            in.close();
        }
        catch (UnsupportedEncodingException e)
        {
            System.out.println(e.getMessage());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return bldr.toString();
    }

    public static String readFile(InputStream inputStream) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, "UTF-8");
        String theString = writer.toString();
        IOUtils.closeQuietly(inputStream);
        return theString;
        /*java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
        */
    }

    public static void writeFile(String filename, String content) throws IOException{
        //Files.write(Paths.get(filename), content.getBytes());
        Writer out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "UTF-8"));
        try {
            out.write(content);
        } finally {
            out.close();
        }

    }
}
