package net.jrtechs.www.SteamAPI;

import net.jrtechs.www.server.Player;
import net.jrtechs.www.utils.ConfigLoader;

import net.jrtechs.www.utils.WebScraper;
import net.jrtechs.www.webCrawler.APIThrottler;
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
     * Makes a call to the steam api using the requested url and does
     * some error handling where it will re-request data from the steam
     * api if it simply throws an internal error.
     *
     * @param url address to download data with
     * @return string of the data returned
     */
    public String querySteamAPI(String url)
    {
        boolean downloaded = false;
        String apiData = "";
        while(!downloaded)
        {
            try
            {
                apiData =  WebScraper.getWebsite(url);
                downloaded = true;
            }
            catch (SteamConnectionException e)
            {
                switch (e.getError())
                {
                    case RESTRICTED:
                    {
                        //This is fine
                        System.out.println("Private profile: ");
                        System.out.println(url);
                        return "";
                    }
                    case CONNECTION:
                    {
                        //spooky 500 error :(
                        //I don't know why but, steam throws 1-3 of these per day
                        System.out.println("Spooky steam API error");
                        new APIThrottler().wait(30);
                        break;
                    }
                    case RATE_LIMITED:
                    {
                        //hasn't happened yet
                        System.out.println("Oof, we are being throttled");
                        new APIThrottler().wait(300);
                        break;
                    }
                    case FORBIDDEN:
                    {
                        System.out.println("Check your API key.");
                        System.exit(-1);
                    }
                    case BAD_REQUEST:
                    {
                        System.out.println("BAD REQUEST:");
                        System.out.println(url);
                        System.out.println("Please modify your query.");
                        return "";
                    }
                }
            }
        }
        return apiData;
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

        String apiData = this.querySteamAPI(this.baseURL + this.friendListURL +
                this.apiKey + "&steamid=" + steamid);

        if(apiData.equals(""))
            return friendsId; //private url

        JSONObject object = new JSONObject(apiData);

        if(object.has("friendslist"))
        {
            object.getJSONObject("friendslist")
                .getJSONArray("friends").toList()
                .forEach(f->
                        friendsId.add(((HashMap<String, String>)(f)).get("steamid"))
                );
        }
        else
        {
            return friendsId;
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
            JSONArray names;

            String apiResult = this.querySteamAPI(queryUrl);

            if(apiResult.equals(""))
                return map;

            JSONObject object = new JSONObject(apiResult);

            if(object.has("response"))
            {
                names = object.getJSONObject("response").getJSONArray("players");
            }
            else
            {
                //eh
                return map;
            }

            for(int i = 0; i < names.length(); i++)
            {
                JSONObject player = names.getJSONObject(i);

                if(player.has("steamid") && player.has("personaname"))
                {
                    map.put(player.getString("steamid"),
                            player.getString("personaname"));
                }
            }
        }
        return map;
    }


    /**
     * Wrapper for getNames which returns a list of players instead
     * of a map from id's to names
     *
     * @param ids
     * @return
     */
    public List<Player> getFullPlayers(List<String> ids)
    {
        Map<String, String> map = this.getNames(ids);

        List<Player> players = new ArrayList<>();

        for(String id: map.keySet())
        {
            players.add(new Player(map.get(id),id));
        }

        return players;
    }



    /**
     * Returns the name of the player with a specific steam id
     *
     * @param steamid the steam id of player
     * @return
     */
    public String getPlayerName(String steamid)
    {
        JSONObject response;
        try
        {
            response = new JSONObject(WebScraper
                    .getWebsite(this.baseURL + this.playerInfoURL +
                            this.apiKey + "&steamids=" + steamid));
        }
        catch (SteamConnectionException ex)
        {
            return "";
        }


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

        //steam id of jrtechs
        con.getFriends("76561198188400721").forEach(System.out::println);

        System.out.println(con.getPlayerName("76561198188400721"));
    }
}