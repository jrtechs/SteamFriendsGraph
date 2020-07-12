package net.jrtechs.www.SteamAPI;

import net.jrtechs.www.model.Game;
import net.jrtechs.www.model.Player;
import net.jrtechs.www.utils.ConfigLoader;

import net.jrtechs.www.utils.WebScraper;
import net.jrtechs.www.webCrawler.APIThrottler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class which is used to pull information from the Steam api
 *
 * Documentation at https://developer.valvesoftware.com/wiki/Steam_Web_API
 *
 * @author Jeffery Russell 5-26-18
 */
public class APIConnection
{
    /** Base url to use for all queries to steam's api  **/
    private final String baseURL = "https://community.steam-api.com";

    /** Path to use when getting info on a player from api **/
    private final String playerInfoURL = "/ISteamUser/GetPlayerSummaries/v0002/";

    private final String friendListURL = "/ISteamUser/GetFriendList/v0001/";

    private final String gamesListURL = "/IPlayerService/GetOwnedGames/v0001/";

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
        System.out.println(url);
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


    public List<Game> getGames(String steamID)
    {
        List<Game> games = new ArrayList<>();
        String apiData = this.querySteamAPI(this.baseURL + this.gamesListURL +
                this.apiKey + "&steamid=" + steamID +
                "&include_appinfo=true&include_played_free_games=true");

        if(apiData.isEmpty())
            return games;

        JSONObject object = new JSONObject(apiData);
        System.out.println(object);

        if(object.has("response"))
        {
            JSONArray gamesJ = object.getJSONObject("response").getJSONArray("games");
            IntStream.range(0, gamesJ.length()).forEach(i ->
                    games.add(new Game(gamesJ.getJSONObject(i))));
        }
        return games;
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
        System.out.println(object);

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
    public List<Player> getPlayers(List<String> ids)
    {
        System.out.println(ids);
        List<Player> players = new ArrayList<>();
        while(!ids.isEmpty())
        {
            StringBuilder queryUrl = new StringBuilder(baseURL + playerInfoURL + apiKey + "&steamids=");

            int remove = Math.min(ids.size(), 100);

            for(int i = 0; i < remove; i++)
            {
                queryUrl.append(",").append(ids.remove(0));
            }

            System.out.println(queryUrl);
            JSONArray names;

            String apiResult = this.querySteamAPI(queryUrl.toString());

            if(apiResult.equals(""))
                return players;

            JSONObject object = new JSONObject(apiResult);

            if(object.has("response"))
            {
                names = object.getJSONObject("response").getJSONArray("players");
            }
            else
            {
                //eh
                return players;
            }

            for(int i = 0; i < names.length(); i++)
            {
                JSONObject player = names.getJSONObject(i);

                if(player.has(Player.KEY_STEAM_ID) && player.has(Player.KEY_USERNAME))
                {
                    players.add(transformToPlayer(player));
                }
            }
        }
        return players;
    }


    private Player transformToPlayer(JSONObject player)
    {
        String avatar = player.has(Player.KEY_AVATAR) ?
                player.getString(Player.KEY_AVATAR) :
                "";
        String realName = player.has(Player.KEY_REAL_NAME) ?
                player.getString(Player.KEY_REAL_NAME) :
                "";
        String id = player.getString(Player.KEY_STEAM_ID);
        Integer timeCreated = player.has(Player.KEY_TIME_CREATED) ?
                player.getInt(Player.KEY_TIME_CREATED) :
                0;
        String username =  player.getString(Player.KEY_USERNAME);
        return new Player(username, id, realName, timeCreated, avatar);
    }



    /**
     * Returns the name of the player with a specific steam id
     *
     * @param steamid the steam id of player
     * @return
     */
    public Player getSingle(String steamid) throws SteamConnectionException
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
                    return transformToPlayer(arr.getJSONObject(0));
                }
            }
        }
        return null;
    }


    public static void main(String[] args) throws SteamConnectionException
    {
        APIConnection con = new APIConnection();

        //steam id of jrtechs
        con.getFriends("76561198188400721").forEach(System.out::println);

        //System.out.println(con.getSingle("76561198188400721"));
        System.out.println(con.getGames("76561198188400721"));
    }
}