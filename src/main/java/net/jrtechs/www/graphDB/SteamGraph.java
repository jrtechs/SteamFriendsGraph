package net.jrtechs.www.graphDB;

import net.jrtechs.www.server.Player;
import net.jrtechs.www.SteamAPI.APIConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //System.out.println(query);
        return (1 <= con.queryGraph(query).stream().count());
    }



    /**
     * Inserts a player vertex into the graph
     *
     * @param
     */
    private void insertPlayerIntoGraph(String id, String name)
    {
        try
        {
            if(!this.alreadyInGraph(id))
            {
                String queryInsertPlayer = "g.addV('player')" +
                        ".property('name', '" + name + "')" +
                        ".property('crawled', '0')" +
                        ".property('id', '" + id + "')";
                System.out.println("inserting " + name + " into graph");
                this.con.queryGraph(queryInsertPlayer);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Checks if a friend-friend edge is already in the
     * graph
     *
     * @param p1
     * @param p2
     * @return
     */
    private boolean edgeAlreadyInGraph(String p1, String p2)
    {
        try
        {
            String query = "g.V().hasLabel('player')" +
                    ".has('id', '" + p1 + "')" +
                    ".both()" +
                    ".has('id', '" + p2 + "')";
            //System.out.println(query);
            return (1 <= con.queryGraph(query).stream().count());
        }
        catch(Exception e)
        {
            return false;
        }

    }


    /**
     * Inserts a edge between two players into the graph
     *
     * @param p1
     * @param p2
     */
    private void insertEdgeIntoGraph(String p1, String p2)
    {
        try
        {
            if(!this.edgeAlreadyInGraph(p1, p2))
            {
                String query = "g.V().hasLabel('player')" +
                        ".has('id', '" + p1 + "')" +
                        ".as('p1')" +
                        "V().hasLabel('player')" +
                        ".has('id', '" + p2 + "')" +
                        ".as('p2')" +
                        ".addE('friends')" +
                        ".from('p1').to('p2')";
                //System.out.println(query);
                this.con.queryGraph(query);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


    /**
     * determines if a player has been indexed for friends yet
     *
     * @param id
     * @return
     */
    private boolean playerAlreadyIndexed(String id)
    {
        try
        {
            String query = "g.V().hasLabel('player')" +
                    ".has('id', '" + id + "')" +
                    ".has('crawled', '0')";

            return (1 != con.queryGraph(query).stream().count());
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return true;
    }


    private void updateCrawledStatus(String id)
    {
        try
        {
            String query = "g.V().hasLabel('player')" +
                    ".has('id', '" + id + "')" +
                    ".property('crawled', '1')";

            this.con.queryGraph(query);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
     * tells api to get this dude's friends list
     *
     * @param id
     */
    private void indexPersonFriends(String id)
    {
        System.out.println("indexing " + this.getNameFromGraph(id));

        List<String> friendsIds = this.api.getFriends(id);

        //find ones not in database
        List<String> notInDatabase = new ArrayList<>();
        for(String fid : friendsIds)
        {
            if(!this.alreadyInGraph(fid))
            {
                notInDatabase.add(fid);
            }
        }
        Map<String, String> names = this.api.getNames(notInDatabase);

        for(String key: names.keySet())
        {
            this.insertPlayerIntoGraph(key, names.get(key));
        }

        friendsIds.forEach(s-> this.insertEdgeIntoGraph(id, s));

        this.updateCrawledStatus(id);
    }


    /**
     * Fetches a player from the graph with all of its friends
     *
     * @param id
     * @return
     */
    public Player getPlayer(String id)
    {
        Player p;
        if(this.alreadyInGraph(id)) // yay
        {
            p = new Player(this.getNameFromGraph(id), id);
            if(!this.playerAlreadyIndexed(id)) //must index the person
            {
                this.indexPersonFriends(id);
            }

            p.setFriends(this.getFriendsFromGraph(id));
        }
        else //smh, shouldn't happen frequently
        {
            System.out.println("brand spanking new request " + id);
            String name = this.api.getPlayerName(id);
            this.insertPlayerIntoGraph(id, name);
            return this.getPlayer(id);
        }
        return p;
    }


    /**
     *
     * @param args
     */
    public static void main(String[] args)
    {
//        SteamGraph graph = new SteamGraph();
//
//        Player base = graph.getPlayer(args[0]);
//
//        int debth = Integer.valueOf(args[1]);
//
//        graph.insertIntoGraph(base, debth);
    }
}