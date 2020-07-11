package net.jrtechs.www.server;


import org.json.JSONObject;

import javax.json.JsonObject;
import java.util.List;
import java.util.Map;

/**
 * Example URL: http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key=XXXXXXXXXXXXXXXXX&steamid=76561197960434622&format=json
 */
public class Game
{
    public static String KEY_DB = "game";
    public static String KEY_STEAM_GAME_ID = "appid";
    public static String KEY_GAME_NAME = "name";
    public static String KEY_GAME_ICON = "img_icon_url";
    public static String KEY_GAME_LOGO = "img_logo_url";

    public static String KEY_RELATIONSHIP = "owns";

    //other
    public static String KEY_PLAY_TIME = "playtime_forever";

    private Integer appID;
    private String icon;
    private String logo;
    private String name;

    private Integer timePlayed;

    public Game(JSONObject g)
    {
        this.appID = g.getInt(Game.KEY_STEAM_GAME_ID);
        this.name = g.getString(KEY_GAME_NAME);
        this.icon = g.getString(KEY_GAME_ICON);
        this.logo = g.getString(KEY_GAME_LOGO);
        this.timePlayed = g.getInt(KEY_PLAY_TIME);
    }

    public Game(Map<String, Object> graph)
    {
        System.out.println(graph);
        this.appID= (Integer)((List<Object>) graph.get(KEY_STEAM_GAME_ID)).get(0);
        this.name = (String)((List<Object>) graph.get(KEY_GAME_NAME)).get(0);
        this.icon = (String)((List<Object>) graph.get(KEY_GAME_ICON)).get(0);
        this.logo = (String)((List<Object>) graph.get(KEY_GAME_LOGO)).get(0);
        this.timePlayed = 0;
    }

    public Integer getAppID()
    {
        return appID;
    }

    public String getIcon()
    {
        return icon;
    }

    public String getLogo()
    {
        return logo;
    }

    public String getName()
    {
        return name;
    }

    public Integer getTimePlayed()
    {
        return timePlayed;
    }

    @Override
    public String toString()
    {
        return "Game{" +
                "appID=" + appID +
                ", icon='" + icon + '\'' +
                ", logo='" + logo + '\'' +
                ", name='" + name + '\'' +
                ", timePlayed=" + timePlayed +
                '}';
    }
}
