package net.jrtechs.www.webCrawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple utility class for reading a file in as a
 * {@link List} of strings
 *
 * @author Jeffery Russell 11-19-18
 */
public class FileReader
{
    /**
     * Reads a file and return's its contents in a array list of strings
     *
     * @return contents of file as a list of Strings
     */
    public static String readFile(String filePath)
    {
        String result = "";
        try
        {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filePath)));
            String line;

            while ((line = br.readLine()) != null)
            {
                result = result.concat(line);
            }
            br.close();
        }
        catch (IOException e)
        {
            System.out.println("ERROR: unable to read file " + filePath);
        }
        return result;
    }
}