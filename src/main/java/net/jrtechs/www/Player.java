package net.jrtechs.www;

import net.jrtechs.www.SteamAPI.APIConnection;

import java.util.ArrayList;
import java.util.List;


/**
 * Class to store information on a player
 *
 * @author Jeffery Russell 5-26-18
 */
public class Player
{
    /** Name of the player **/
    private String name;

    /** Steam id of the player **/
    private String id;

    /** List of friends the player has */
    private List<Player> friends;


    /**
     * Sets the name and id of the player
     *
     * @param name
     * @param id
     */
    public Player(String name, String id)
    {
        this.name = name;
        this.id = id;
        this.friends = null;
    }


    /**
     * Sets the fields of the player only based on it's
     * steam id
     *
     * @param id
     */
    public Player(String id)
    {
        this.id = id;
        this.friends = null;
        this.name = new APIConnection().getPlayerName(id);
    }


    /**
     * Returns a list of all the friends of a specific player
     *
     * @param con
     * @return
     */
    public List<Player> fetchFriends(APIConnection con)
    {
        if(this.friends == null)
        {
            this.friends = new ArrayList<>();
            con.getFriends(this.id)
                    .forEach(f-> this.friends.add(
                            new Player(con.getPlayerName(f), f)));
        }
        return friends;
    }


    /**
     * Returns a list of all the friends of a specific player
     *
     * @return
     */
    public List<Player> fetchFriends()
    {
        return this.friends;
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


    @Override
    public String toString()
    {
        return "Name: " + this.name + " id: " + this.id;
    }
}
