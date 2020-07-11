package net.jrtechs.www.graphDB;

import net.jrtechs.www.SteamAPI.SteamConnectionException;
import net.jrtechs.www.server.Player;
import net.jrtechs.www.SteamAPI.APIConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Does graph based operations with {@link Player}
 * and
 *
 * @author Jeffery Russell 5-26-17
 */
public class SteamGraph
{
    public static String KEY_PLAYER = "player";
    public static String KEY_CRAWLED_STATUS = "crawled";


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
                .V().hasLabel(SteamGraph.KEY_PLAYER)
                .has(Player.KEY_STEAM_ID, id)
                .toList().isEmpty();
    }



    /**
     * Inserts a player vertex into the graph
     *
     * @param
     */
    private void insertPlayerIntoGraph(Player p, boolean check)
    {
        try
        {
            if(!check || !this.alreadyInGraph(p.getId()))
            {
                System.out.println("inserting " + p.getName() + " into graph");
                this.con.getTraversal()
                        .addV(SteamGraph.KEY_PLAYER)
                        .property(Player.KEY_USERNAME, p.getName())
                        .property(SteamGraph.KEY_CRAWLED_STATUS, 0)
                        .property(Player.KEY_STEAM_ID, p.getId())
                        .property(Player.KEY_AVATAR, p.getAvatar())
                        .property(Player.KEY_REAL_NAME, p.getRealName())
                        .property(Player.KEY_TIME_CREATED, p.getTimeCreated())
                        .id().next();
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
                    .V().hasLabel(SteamGraph.KEY_PLAYER)
                    .has(Player.KEY_STEAM_ID, p1)
                    .both()
                    .has(Player.KEY_STEAM_ID, p2)
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
                        .hasLabel(SteamGraph.KEY_PLAYER)
                        .has(Player.KEY_STEAM_ID, p1)
                        .as("p1")
                        .V().hasLabel(SteamGraph.KEY_PLAYER)
                        .has(Player.KEY_STEAM_ID, p2)
                        .as("p2")
                        .addE(Player.KEY_FRIENDS)
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
                    .V().hasLabel(SteamGraph.KEY_PLAYER)
                    .has(Player.KEY_STEAM_ID, id)
                    .has(SteamGraph.KEY_CRAWLED_STATUS, 0)
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
                    .hasLabel(SteamGraph.KEY_PLAYER)
                    .has(Player.KEY_STEAM_ID, id)
                    .property(SteamGraph.KEY_CRAWLED_STATUS, 1).id().next();
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
    private Player getPlayerFromGraph(String id)
    {
        return new Player(this.con.getTraversal().V()
                .hasLabel(SteamGraph.KEY_PLAYER)
                .has(Player.KEY_STEAM_ID, id)
                .valueMap()
                .toStream().findFirst().get());
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
        return new ArrayList<Player>()
        {{
            con.getTraversal().V()
                .hasLabel(SteamGraph.KEY_PLAYER)
                .has(Player.KEY_STEAM_ID, id)
                .both().valueMap().toStream().forEach(r ->
                    add(new Player(r)));
        }};
    }


    /**
     * tells api to get this dude's friends list
     *
     * @param id
     */
    private void indexPersonFriends(String id)
    {
        System.out.println("indexing " + id);

        List<String> friendsIds = this.api.getFriends(id);

        //find ones not in database
        List<String> notInDatabase = friendsIds
                .stream()
                .filter(p -> !alreadyInGraph(p))
                .collect(Collectors.toList());

        this.api.getPlayers(notInDatabase)
                .forEach(p ->
                        insertPlayerIntoGraph(p, false));

        friendsIds.forEach(s->
                this.insertEdgeIntoGraph(id, s));

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
            p = this.getPlayerFromGraph(id);
            if(!this.playerAlreadyIndexed(id)) //must index the person
            {
                this.indexPersonFriends(id);
            }

            p.setFriends(this.getFriendsFromGraph(id));
        }
        else //smh, shouldn't happen frequently
        {
            System.out.println("brand spanking new request " + id);
            try
            {
                p = this.api.getSingle(id);
                this.insertPlayerIntoGraph(p, false);
            }
            catch (SteamConnectionException e)
            {
                e.printStackTrace();
                return null;
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
        graph.getPlayer("76561198068098265").getFriends().stream().forEach(System.out::println);
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