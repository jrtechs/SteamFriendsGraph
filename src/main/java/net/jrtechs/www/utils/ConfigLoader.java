package net.jrtechs.www.utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;


/**
 * {@link ConfigLoader} Is responsible for abstracting the process
 * of loading a configuration file to make it easier to use for
 * multiple purposes.
 * All configuration files will be stored in JSON format which
 * makes reading and distributing them easier.
 *
 * @author Jeffery Russell 5-22-18
 */
public class ConfigLoader
{
    /** Json object which stores configuration contents **/
    private JSONObject config;


    /**
     * Constructor which reads in a conf file and sets the
     * json element of the class.
     *
     * @param fileName
     */
    public ConfigLoader(String fileName)
    {
        fileName = "conf/" + fileName;

        String file = this.loadFile(fileName);
        this.config = new JSONObject(file);
    }


    /**
     * Loads JSON from a particular file
     *
     * @param file -- file name to open
     * @return - String of file contents
     */
    private String loadFile(String file)
    {
        String jsonString = "";

        try
        {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));
            String line;
            while((line = br.readLine()) != null)
            {
                if(line.length() > 0 && line.charAt(0) != '#')
                {
                    jsonString += line;
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return jsonString;
    }


    /**
     * Returns a single property from a {@link ConfigLoader}
     * based on its key.
     *
     * @param key
     * @return
     */
    public String getValue(String key)
    {
        if(this.config.has(key))
            return this.config.getString(key);
        else
            return null;
    }


    /**
     * Returns the integer value associated with a key in
     * the configuration file.
     *
     * @param key
     * @return
     */
    public int getInt(String key)
    {
        if(this.config.has(key))
            return this.config.getInt(key);
        else
            return 0;
    }
}