package net.jrtechs.www.webCrawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class SteamdFileWriter
{
    public static void writeToFile(String data, String fileName)
    {
        BufferedWriter writer;
        try
        {
            File file = new File(fileName);
            file.createNewFile();
            writer = new BufferedWriter(new FileWriter(file));
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
