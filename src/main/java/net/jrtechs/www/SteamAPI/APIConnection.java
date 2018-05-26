package net.jrtechs.www.SteamAPI;

import net.jrtechs.www.utils.ConfigLoader;

import net.jrtechs.www.utils.WebScraper;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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


    /**
     * Returns a list of the UIDs of all the players friends
     *
     * @param steamid
     * @return
     */
    public List<String> getFriends(String steamid)
    {
        List<String> friendsId = new ArrayList<>();

        new JSONObject(WebScraper
                .getWebsite(this.baseURL + this.friendListURL +
                        this.apiKey + "&steamid=" + steamid))
                .getJSONObject("friendslist")
                .getJSONArray("friends").toList()
                .forEach(f->
                    friendsId.add(((HashMap<String, String>)(f)).get("steamid"))
        );
        return friendsId;
    }


    /**
     * Returns the name of the player with a specific steam id
     *
     * @param steamid the steam id of player
     * @return
     */
    public String getPlayerName(String steamid)
    {
        return ((HashMap<String, String>) new JSONObject(WebScraper
                .getWebsite(this.baseURL + this.playerInfoURL +
                    this.apiKey + "&steamids=" + steamid))
                .getJSONObject("response")
                .getJSONArray("players")
                .toList().stream().findAny().get()).get("personaname");
    }

    public static void main(String[] args)
    {
        APIConnection con = new APIConnection();

        con.getFriends("76561198188400721").forEach(System.out::println);

        System.out.println(con.getPlayerName("76561198188400721"));
    }
}
