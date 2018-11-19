package net.jrtechs.www.webCrawler;

import com.google.gson.Gson;
import net.jrtechs.www.server.Player;


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


    /** Object  used to convert objects to json strings */
    private final Gson gson;

    /**
     * Initalizes the base directory
     * @param basePath
     */
    public FileIO(String basePath)
    {
        this.baseFilaPath = basePath;
        this.gson = new Gson();
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
        return false;
    }


    /**
     * Writes the player to the file.
     *
     * @param player
     */
    public void writeToFile(Player player)
    {
        String data = gson.toJson(player);

        String fileName = baseFilaPath + player.getId() + ".json";

        SteamdFileWriter.writeToFile(data, fileName);
    }
}
