package net.jrtechs.www.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Class to store information on a player
 *
 * @author Jeffery Russell 5-26-18
 */
public class Player
{
    public static String KEY_STEAM_ID = "steamid";
    public static String KEY_REAL_NAME = "realname";
    public static String KEY_TIME_CREATED = "timecreated";
    public static String KEY_AVATAR = "avatarfull";
    public static String KEY_USERNAME = "personaname";

    public static String KEY_FRIENDS = "friends";


    /** Name of the player **/
    private String name;

    /** Steam id of the player **/
    private String id;

    /** List of friends the player has */
    private List<Player> friends;

    private String realName;

    private String avatar;

    private Integer timeCreated;


    /**
     * Sets the name and id of the player
     *
     * @param name
     * @param id
     */
    public Player(String name, String id,
                  String realName, Integer timeCreated,
                  String avatar)
    {
        this.name = name;
        this.id = id;
        this.realName = realName;
        this.timeCreated = timeCreated;
        this.avatar = avatar;
        this.friends = new ArrayList<>();
    }


    public Player(Map<String, Object> apiInfo)
    {
        this.id = ((List<Object>) apiInfo.get(Player.KEY_STEAM_ID)).get(0).toString();
        this.name = ((List<Object>) apiInfo.get(Player.KEY_USERNAME)).get(0).toString();
        this.realName = ((List<Object>) apiInfo.getOrDefault(Player.KEY_REAL_NAME, "")).get(0).toString();
        this.avatar = ((List<Object>) apiInfo.getOrDefault(Player.KEY_AVATAR, "")).get(0).toString();
        this.timeCreated = (Integer)((List<Object>)apiInfo.get(KEY_TIME_CREATED)).get(0);
        this.friends = new ArrayList<>();
    }

    public List<Player> getFriends()
    {
        return friends;
    }


    /**
     * Getter for display name of player
     *
     * @return
     */
    public String getName()
    {
        return this.name.replace("'", "");
    }


    /**
     * Getter for id of player
     *
     * @return
     */
    public String getId()
    {
        return this.id;
    }

    public void setFriends(List<Player> friends)
    {
        this.friends = friends;
    }

    public String getRealName()
    {
        return this.realName;
    }


    public String getAvatar()
    {
        return this.avatar;
    }


    public Integer getTimeCreated()
    {
        return this.timeCreated;
    }


    @Override
    public String toString()
    {
        return "Name: " + this.name +
                " id: " + this.id +
                " friend count " + friends.size() +
                " avatar:" + this.avatar +
                " real name " + this.realName +
                " created " + this.timeCreated;
    }
}