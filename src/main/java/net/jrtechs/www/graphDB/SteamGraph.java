package net.jrtechs.www.graphDB;

import com.google.gson.Gson;
import net.jrtechs.www.SteamAPI.SteamConnectionException;
import net.jrtechs.www.model.Game;
import net.jrtechs.www.model.Player;
import net.jrtechs.www.SteamAPI.APIConnection;
import net.jrtechs.www.utils.WrappedFileWriter;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Does graph based operations with {@link Player}
 * and {@link Game}
 *
 * @author Jeffery Russell 5-26-17
 */
public class SteamGraph
{
    public static String KEY_PLAYER = "player";
    public static String KEY_CRAWLED_STATUS = "crawled";

    public static String KEY_CRAWLED_GAME_STATUS = "crawled_game";

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
    private void insertPlayerIntoGraph(Player p)
    {
        if(!this.alreadyInGraph(p.getId()))
        {
            System.out.println("inserting " + p.getName() + " into graph");
            this.con.getTraversal()
                    .addV(SteamGraph.KEY_PLAYER)
                    .property(Player.KEY_USERNAME, p.getName())
                    .property(SteamGraph.KEY_CRAWLED_STATUS, 0)
                    .property(SteamGraph.KEY_CRAWLED_GAME_STATUS, 0)
                    .property(Player.KEY_STEAM_ID, p.getId())
                    .property(Player.KEY_AVATAR, p.getAvatar())
                    .property(Player.KEY_REAL_NAME, p.getRealName())
                    .property(Player.KEY_TIME_CREATED, p.getTimeCreated())
                    .id().next();
        }
    }

    private boolean gameAlreadyInGraph(Integer id)
    {
        return !con.getTraversal()
                .V().hasLabel(Game.KEY_DB)
                .has(Game.KEY_STEAM_GAME_ID, id)
                .toList().isEmpty();
    }

    private void insertGameForPlayerToGraph(String id, Game g)
    {
        if(!gameAlreadyInGraph(g.getAppID()))// check if game is already in graph
        {
            //insert game into graph
            this.con.getTraversal()
                    .addV(Game.KEY_DB)
                    .property(Game.KEY_STEAM_GAME_ID, g.getAppID())
                    .property(Game.KEY_GAME_NAME, g.getName())
                    .property(Game.KEY_GAME_ICON, g.getIcon())
                    .property(Game.KEY_GAME_LOGO, g.getLogo())
                    .id().next();
        }

        // insert connection from player to game

        this.con.getTraversal()
                .V()
                .hasLabel(SteamGraph.KEY_PLAYER)
                .has(Player.KEY_STEAM_ID, id)
                .as("p")
                .V().hasLabel(Game.KEY_DB)
                .has(Game.KEY_STEAM_GAME_ID, g.getAppID())
                .as("g")
                .addE(Game.KEY_RELATIONSHIP)
                .from("p").to("g")
                .property(Game.KEY_PLAY_TIME, g.getTimePlayed())
                .id().next();
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
        return !this.con.getTraversal()
                .V().hasLabel(SteamGraph.KEY_PLAYER)
                .has(Player.KEY_STEAM_ID, p1)
                .both()
                .has(Player.KEY_STEAM_ID, p2)
                .toList().isEmpty();
    }


    /**
     * Inserts a edge between two players into the graph
     *
     * @param p1
     * @param p2
     */
    private void insertFriendshipIntoGraph(String p1, String p2)
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


    /**
     * determines if a player has been indexed for friends yet
     *
     * @param id
     * @return
     */
    private boolean playerFriendsAlreadyIndexed(String id)
    {
        return playerPropertyIndexed(id, SteamGraph.KEY_CRAWLED_STATUS);
    }


    private boolean playerGamesAlreadyIndexed(String id)
    {
        return playerPropertyIndexed(id, SteamGraph.KEY_CRAWLED_GAME_STATUS);
    }

    /**
     * determines if a player has been indexed yet
     *
     * @param id
     * @return
     */
    private boolean playerPropertyIndexed(String id, String key)
    {
        return this.con.getTraversal()
                .V().hasLabel(SteamGraph.KEY_PLAYER)
                .has(Player.KEY_STEAM_ID, id)
                .has(key, 0)
                .toList().isEmpty();
    }


    private void updateCrawledStatusFriends(String id)
    {
        updateCrawledStatus(id, SteamGraph.KEY_CRAWLED_STATUS);
    }


    private void updateCrawledStatusGames(String id)
    {
        updateCrawledStatus(id, SteamGraph.KEY_CRAWLED_GAME_STATUS);
    }


    private void updateCrawledStatus(String id, String key)
    {
        try
        {
            this.con.getTraversal().V()
                    .hasLabel(SteamGraph.KEY_PLAYER)
                    .has(Player.KEY_STEAM_ID, id)
                    .property(key, 1).id().next();
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
        return con.getTraversal().V()
                .hasLabel(SteamGraph.KEY_PLAYER)
                .has(Player.KEY_STEAM_ID, id)
                .outE()
                .inV()
                .hasLabel(SteamGraph.KEY_PLAYER)
                .valueMap()
                .toStream()
                .map(Player::new)
                .collect(Collectors.toList());
    }

    private List<Game> getPlayerGamesFromGraph(String id)
    {
        System.out.println("fetching games from graph");
        return con.getTraversal().V()
                .hasLabel(SteamGraph.KEY_PLAYER)
                .has(Player.KEY_STEAM_ID, id)
                .outE()
                .inV()
                .hasLabel(Game.KEY_DB)
                .valueMap()
                .toStream()
                .map(Game::new)
                .collect(Collectors.toList());
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
                        insertPlayerIntoGraph(p));

        friendsIds.forEach(s->
                this.insertFriendshipIntoGraph(id, s));

        this.updateCrawledStatusFriends(id);
        this.con.commit();
    }

    private List<Game> indexPersonsGames(String id)
    {
        System.out.println("indexing  games for " + id);
        List<Game> games = this.api.getGames(id);
        games.forEach(g -> insertGameForPlayerToGraph(id, g));
        this.updateCrawledStatusGames(id);
        this.con.commit();
        return games;
    }

    public synchronized List<Game> getGameList(String id)
    {
        return this.playerGamesAlreadyIndexed(id) ?
                this.getPlayerGamesFromGraph(id) :
                this.indexPersonsGames(id);
    }


    /**
     * Fetches a player from the graph with all of its friends
     *
     * @param id
     * @return
     */
    public synchronized Player getPlayer(String id)
    {
        Player p;
        if(this.alreadyInGraph(id)) // yay
        {
            p = this.getPlayerFromGraph(id);
            if(!this.playerFriendsAlreadyIndexed(id)) //must index the person
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
                this.insertPlayerIntoGraph(p);
                this.indexPersonFriends(id);
                p.setFriends(this.getFriendsFromGraph(id));
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
        System.out.println(
                graph.con.getTraversal().E().hasLabel(Game.KEY_RELATIONSHIP).count().next()
        );
//
        Object o =
                graph.con.getTraversal()
                    .V()
                    .hasLabel(Game.KEY_DB)
                    .match(
                            __.as("c").values(Game.KEY_STEAM_GAME_ID).as("gameID"),
                            __.as("c").values(Game.KEY_GAME_NAME).as("gameName"),
                            __.as("c").inE(Game.KEY_RELATIONSHIP).values(Game.KEY_PLAY_TIME).as("time")
                    ).select("gameID", "time", "gameName").toList();

//        System.out.println(new Gson().toJson(o));
        WrappedFileWriter.writeToFile(new Gson().toJson(o).toLowerCase(), "games.json");

//        System.out.println(
//                graph.con.getTraversal()
//                        .V()
//                        .hasLabel(Game.KEY_DB)
//                        .match(
//                                __.as("c").values(Game.KEY_STEAM_GAME_ID).as("gameID"),
//                                __.as("c").values(Game.KEY_GAME_NAME).as("gameName"),
//                                __.as("c").inE(Game.KEY_RELATIONSHIP).values(Game.KEY_PLAY_TIME).as("averageGameTime")
//                        ).select("gameID", "time", "averageGameTime").toList()
//        );

//        System.out.println(
//                graph.con.getTraversal().V()
//                        .hasLabel(Game.KEY_DB)
//                        .as("g").inE()
//                        .hasLabel(Game.KEY_RELATIONSHIP)
//                        .as("p")
//                        .project(Game.KEY_PLAY_TIME, Game.KEY_STEAM_GAME_ID).value().toList()
//        );

        System.out.println(
                graph.con.getTraversal().E()
                        .hasLabel(Game.KEY_RELATIONSHIP).values(Game.KEY_PLAY_TIME).max().toList()
        );
//
//        graph.getPlayer("76561198188400721")
//            .getFriends().forEach(f->
//            {
//                graph.getGameList(f.getId());
//                graph.getPlayer(f.getId()).getFriends().forEach(f2->
//                {
//                    graph.getGameList(f2.getId());
//                    graph.getPlayer(f2.getId());
//                });
//            });

        System.out.println(
                graph.con.getTraversal().V().hasLabel(SteamGraph.KEY_PLAYER)
                .has(SteamGraph.KEY_CRAWLED_GAME_STATUS, 1)
                .count().next()
        );


//        graph.getPlayer("76561198062300654");
        //graph.getGameList("76561198188400721");
//        //graph.getPlayer("76561198068098265").getFriends().stream().forEach(System.out::println);
////        graph.indexPersonFriends("76561198188400721");
//        graph.indexPersonsGames("76561198188400721");
//        System.out.println(graph.getGameList("76561198188400721"));
//        System.out.println(graph.getPlayer("76561198188400721"));
        graph.close();
//
//        Player base = graph.getPlayer(args[0]);
//
//        int debth = Integer.valueOf(args[1]);
//
//        graph.insertIntoGraph(base, debth);
    }
}