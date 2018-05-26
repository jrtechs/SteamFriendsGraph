package net.jrtechs.www.SteamAPI;

import net.jrtechs.www.utils.ConfigLoader;

/**
 * Class which is used to pull information from the Steam api
 *
 * @author Jeffery Russell 5-26-18
 */
public class APIConnection
{
    /** Base url to use for all queries to steam's api  **/
    private final String baseURL = "http://api.steampowered.com";

    /** Path to use when getting info on a player from api **/
    private final String playerInfoURL = "/ISteamUser/GetPlayerSummaries/v0002/";

    private final String friendListURL = "/ISteamUser/GetFriendList/v0001/";

    /** Path to conf file(from within the conf folder) **/
    private final String confPath = "SteamAPIKey.json";

    /** API key for steam's api - loaded from json conf file **/
    private String apiKey;


    /**
     * Constructor for APIConnection which loads a config file
     * and sets the api key to your Steam api key.
     */
    public APIConnection()
    {
        ConfigLoader conf = new ConfigLoader(confPath);

        apiKey = "?key=" + conf.getValue("api");
    }
}
