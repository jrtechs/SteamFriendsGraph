package net.jrtechs.www.utils;

import java.io.BufferedWriter;
import java.io.File;


/**
 * Simple utility class to write the contents of a string to a
 * file.
 *
 * @author Jeffery Russell 11-24-18
 */
public class WrappedFileWriter
{
    /**
     * Writes the contents of a string to a file.
     *
     * @param data data to be included in the file
     * @param fileName name of the file to write to.
     */
    public static void writeToFile(String data, String fileName)
    {
        BufferedWriter writer;
        try
        {
            File file = new File(fileName);
            file.createNewFile();
            writer = new BufferedWriter(new java.io.FileWriter(file));
            writer.write(data);
            writer.flush();
            writer.close();
            System.out.println("Wrote to " + fileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
