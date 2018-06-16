package net.jrtechs.www.SteamAPI;

import net.jrtechs.www.utils.ConfigLoader;

import net.jrtechs.www.utils.WebScraper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        try
        {
            new JSONObject(WebScraper
                    .getWebsite(this.baseURL + this.friendListURL +
                            this.apiKey + "&steamid=" + steamid))
                    .getJSONObject("friendslist")
                    .getJSONArray("friends").toList()
                    .forEach(f->
                            friendsId.add(((HashMap<String, String>)(f)).get("steamid"))
                    );
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return friendsId;
    }


    /**
     * returns a map from the steam id to the players name
     *
     * * tricky because we can only request up to 100 ids
     * in one request
     *
     * @param ids
     * @return
     */
    public Map<String, String> getNames(List<String> ids)
    {
        System.out.println(ids);
        Map<String, String> map = new HashMap<>();

        while(!ids.isEmpty())
        {
            String queryUrl = baseURL + playerInfoURL + apiKey + "&steamids=";

            int remove = (ids.size() > 100) ? 100 : ids.size();

            for(int i = 0; i < remove; i++)
            {
                queryUrl = queryUrl + "," + ids.remove(0);
            }

            System.out.println(queryUrl);
            JSONArray names = new JSONObject(WebScraper.getWebsite(queryUrl))
                    .getJSONObject("response").getJSONArray("players");

            for(int i = 0; i < names.length(); i++)
            {
                JSONObject player = names.getJSONObject(i);
                System.out.println(player);
                map.put(player.getString("steamid"),
                        player.getString("personaname"));
            }
        }
        return map;
    }


    /**
     * Returns the name of the player with a specific steam id
     *
     * @param steamid the steam id of player
     * @return
     */
    public String getPlayerName(String steamid)
    {
        JSONObject response = new JSONObject(WebScraper
                .getWebsite(this.baseURL + this.playerInfoURL +
                        this.apiKey + "&steamids=" + steamid));

        if(response.has("response"))
        {
            response = response.getJSONObject("response");
            if(response.has("players"))
            {
                JSONArray arr = response.getJSONArray("players");
                if(arr.length() > 0)
                {
                    return arr.getJSONObject(0).getString("personaname");
                }
            }
        }
        return null;
    }

    public static void main(String[] args)
    {
        APIConnection con = new APIConnection();

        con.getFriends("76561198188400721").forEach(System.out::println);

        System.out.println(con.getPlayerName("76561198188400721"));
    }
}