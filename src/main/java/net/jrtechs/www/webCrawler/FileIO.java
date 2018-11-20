package net.jrtechs.www.webCrawler;

import net.jrtechs.www.server.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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


    private String getURL(String id)
    {
        return baseFilaPath + id + ".json";
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


    public List<String> readFriends(String id)
    {
        String fileContents = FileReader.readFile(this.getURL(id));

        JSONObject player = new JSONObject(fileContents);

        if(player.has("friends"))
        {
            List<String> list = new ArrayList<>();

            JSONArray jsonArray = player.getJSONArray("friends");

            for(int i = 0 ; i < jsonArray.length();i++)
            {
                list.add(jsonArray.getString(i));
            }
            return list;
        }
        return new ArrayList<>();
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
