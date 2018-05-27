package net.jrtechs.www.graphDB;

import net.jrtechs.www.Player;
import net.jrtechs.www.SteamAPI.APIConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Does graph based operations with {@link Player}
 * and {@link RemoteConnection}
 *
 * @author Jeffery Russell 5-26-17
 */
public class SteamGraph
{
    /** Connection to the graph server */
    private RemoteConnection con;

    /** Connection to steam api */
    private APIConnection api;


    /**
     * Constructs object with a graph connection
     * and a steam api connection
     */
    public SteamGraph()
    {
        this.con = new RemoteConnection();
        this.api = new APIConnection();
    }


    /**
     * Checks if a player is already in the graph
     *
     * @param id steam id of player
     * @return
     */
    private boolean alreadyInGraph(String id)
    {
        String query = "g.V().hasLabel('player')" +
                        ".has('id', '" + id + "')";
        System.out.println(query);
        return (1 == con.queryGraph(query).stream().count());
    }


    /**
     * Checks if a friend-friend edge is already in the
     * graph
     *
     * @param p1
     * @param p2
     * @return
     */
    private boolean edgeAlreadyInGraph(Player p1, Player p2)
    {
        String query = "g.V().hasLabel('player')" +
                ".has('id', '" + p1.getId() + "')" +
                ".both()" +
                ".has('id', '" + p2.getId() + "')";
        System.out.println(query);
        return (1 == con.queryGraph(query).stream().count());
    }

    /**
     * Inserts a player vertex into the graph
     *
     * @param player
     */
    private void insertSinglePlayer(Player player)
    {
        String queryInsertPlayer = "g.addV('player')" +
                ".property('name', '" + player.getName() + "')" +
                ".property('id', '" + player.getId() + "')";
        System.out.println(queryInsertPlayer);
        this.con.queryGraph(queryInsertPlayer);
    }


    /**
     * Inserts a edge between two players into the graph
     *
     * @param p1
     * @param p2
     */
    private void insertEdge(Player p1, Player p2)
    {
        String query = "g.V().hasLabel('player')" +
                ".has('id', '" + p1.getId() + "')" +
                ".as('p1')" +
                "V().hasLabel('player')" +
                ".has('id', '" + p2.getId() + "')" +
                ".as('p2')" +
                ".addE('friends')" +
                ".from('p1').to('p2')";
        System.out.println(query);
        this.con.queryGraph(query);
    }


    /**
     * Inserts a player and all of it's friends into
     * the graph.
     *
     * @param player
     */
    public void insertIntoGraph(Player player)
    {
        System.out.println(player);
        if(!this.alreadyInGraph(player.getId()))
        {
            this.insertSinglePlayer(player);
        }

        for(Player friend : player.fetchFriends(api))
        {
            if(!alreadyInGraph(friend.getId()))
            {
                insertSinglePlayer(friend);
            }

            if(!edgeAlreadyInGraph(player, friend))
            {
                insertEdge(player, friend);
            }
        }
    }


    /**
     * Recursive function for scraping the steam api
     *
     * @param player
     * @param debth
     */
    public void insertIntoGraph(Player player, int debth)
    {
        insertIntoGraph(player);

        if(debth > 0)
        {
            player.fetchFriends(this.api)
                    .forEach(f -> insertIntoGraph(f, debth -1));
        }
    }


    /**
     * Fetches the name of the player from the graph database
     *
     * @param id
     * @return
     */
    private String getNameFromGraph(String id)
    {
        String query = "g.V().hasLabel('player')" +
                ".has('id', '" + id + "')" +
                ".values('name')";
        return this.con.queryGraph(query).stream()
                .findFirst().get().getObject().toString();
    }


    /**
     * Fetches a list of friends from the graph database
     *
     * @param id
     * @return
     */
    private List<Player> getFriendsFromGraph(String id)
    {
        List<Player> friends = new ArrayList<>();

        String query = "g.V().hasLabel('player')" +
                ".has('id', '" + id + "')" +
                ".both().valueMap()";

        this.con.queryGraph(query).stream().forEach(r->
            friends.add(new Player(
                ((ArrayList) (((HashMap<String, Object>)(r.getObject()))
                    .get("name"))).get(0).toString(),
                ((ArrayList)(((HashMap<String, Object>)(r.getObject()))
                        .get("id"))).get(0).toString()))
        );

        return friends;
    }



    /**
     * Fetches a player from the graph
     *
     * @param id
     * @return
     */
    public Player getPlayerInformation(String id)
    {
        Player p;
        if(!this.alreadyInGraph(id))
        {
            p = new Player(id);
            this.insertIntoGraph(p);
        }
        else
        {
            p = new Player(this.getNameFromGraph(id), id);
            System.out.println(p.getName());
        }

        p.setFriends(this.getFriendsFromGraph(id));

        return p;
    }


    /**
     *
     * @param args
     */
    public static void main(String[] args)
    {
        SteamGraph graph = new SteamGraph();

        Player base = new Player(args[0]);

        int debth = Integer.valueOf(args[1]);

        graph.insertIntoGraph(base, debth);
    }
}
