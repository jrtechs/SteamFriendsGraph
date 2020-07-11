package net.jrtechs.www.graphDB;

import net.jrtechs.www.server.Player;
import net.jrtechs.www.SteamAPI.APIConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Does graph based operations with {@link Player}
 * and
 *
 * @author Jeffery Russell 5-26-17
 */
public class SteamGraph
{
    /** Connection to the graph server */
    private GraphConnection con;

    /** Connection to steam api */
    private APIConnection api;


    /**
     * Constructs object with a graph connection
     * and a steam api connection
     */
    public SteamGraph()
    {
        this.con = new GraphConnection();
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
        System.out.println("Checking id:" + id);
        return !con.getTraversal()
                .V().hasLabel("player")
                .has("id", id)
                .toList().isEmpty();
    }



    /**
     * Inserts a player vertex into the graph
     *
     * @param
     */
    private void insertPlayerIntoGraph(String id, String name, boolean check)
    {
        try
        {
            if(!check || !this.alreadyInGraph(id))
            {
                System.out.println("inserting " + name + " into graph");
                this.con.getTraversal()
                        .addV("player")
                        .property("name", name)
                        .property("crawled", 0)
                        .property("id", id).id().next();
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
            return !this.con.getTraversal()
                    .V().hasLabel("player")
                    .has("id", p1)
                    .both()
                    .has("id", p2)
                    .toList().isEmpty();
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
                System.out.println("Inserting edge: " + p1 + ":" + p2);
                this.con.getTraversal()
                        .V()
                        .hasLabel("player")
                        .has("id", p1)
                        .as("p1")
                        .V().hasLabel("player")
                        .has("id", p2)
                        .as("p2")
                        .addE("friends")
                        .from("p1").to("p2").id().next();
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
            return this.con.getTraversal()
                    .V().hasLabel("player")
                    .has("id", id)
                    .has("crawled", 0)
                    .toList().isEmpty();
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
            this.con.getTraversal().V()
                    .hasLabel("player")
                    .has("id", id)
                    .property("crawled", 1).id().next();
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
        return this.con.getTraversal().V()
                .hasLabel("player")
                .has("id", id)
                .values("name")
                .toStream().findFirst().get().toString();
    }


    /**
     * Fetches a list of friends from the graph database
     *
     * @param id steam id
     * @return list of friends
     */
    private List<Player> getFriendsFromGraph(String id)
    {
        System.out.println("fetching friends from graph");
        List<Player> friends = new ArrayList<>();
        try
        {
            this.con.getTraversal().V()
                    .hasLabel("player")
                    .has("id", id)
                    .both().valueMap().toStream().forEach(r ->
                        friends.add(
                                new Player(r.get("name").toString(),
                                        r.get("id").toString()
                                )
                        )
            );
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
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
            this.insertPlayerIntoGraph(key, names.get(key), false);
        }

        friendsIds.forEach(s-> this.insertEdgeIntoGraph(id, s));

        this.updateCrawledStatus(id);
        this.con.commit();
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
            if(name == null)
            {
                return null;
            }
            else
            {
                this.insertPlayerIntoGraph(id, name, false);
                return this.getPlayer(id);
            }
        }
        return p;
    }

    public void close()
    {
        try
        {
            this.con.closeConnection();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param args
     */
    public static void main(String[] args)
    {
        SteamGraph graph = new SteamGraph();
        graph.getPlayer("76561198013779806").getFriends().stream().forEach(System.out::println);
//        graph.indexPersonFriends("76561198188400721");
        graph.close();
//
//        Player base = graph.getPlayer(args[0]);
//
//        int debth = Integer.valueOf(args[1]);
//
//        graph.insertIntoGraph(base, debth);
    }
}