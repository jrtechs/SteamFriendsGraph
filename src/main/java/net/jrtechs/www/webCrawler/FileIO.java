package net.jrtechs.www.webCrawler;

import net.jrtechs.www.server.Player;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * File which handles the file IO for storing
 * all the players on the HHD
 *
 * @author Jeffery Russell 11-18-18
 */
public class FileIO
{
    /** Base directory to store all the data */
    private String baseFilaPath;

    /**
     * Initalizes the base directory
     * @param basePath
     */
    public FileIO(String basePath)
    {
        this.baseFilaPath = basePath;
    }


    /**
     * Determines if we already have the player
     * on disk.
     *
     * @param id
     * @return
     */
    public boolean playerExists(String id)
    {
        String fileName = baseFilaPath + id + ".json";

        return new File(fileName).isFile();
    }


    /**
     * Returns the date in a form which is easy to read and write
     * to from a file.
     *
     * @return
     */
    private String getDate()
    {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        return simpleDateFormat.format(new Date());
    }


    /**
     * Writes the player to the file.
     *
     * @param player
     */
    public void writeToFile(Player player, List<String> friendIDS)
    {
        JSONObject object = new JSONObject();
        object.put("name", player.getName());
        object.put("date", getDate());
        object.put("friends", friendIDS);

        String fileName = baseFilaPath + player.getId() + ".json";

        SteamdFileWriter.writeToFile(object.toString(4), fileName);
    }
}
