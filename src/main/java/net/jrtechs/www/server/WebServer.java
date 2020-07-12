package net.jrtechs.www.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.jrtechs.www.graphDB.SteamGraph;
import net.jrtechs.www.model.Game;
import net.jrtechs.www.model.Player;

import java.lang.reflect.Type;
import java.util.List;

import static spark.Spark.*;

/**
 * Quick and dirty web server to serve as an API backend to
 * the graph interface.
 *
 * @author Jeffery Russell 7-12-20
 */
public class WebServer
{
    private SteamGraph graph;

    private Gson gson;

    public static String GET_PLAYER = "/player";
    public static String GET_GAMES = "/games";

    public WebServer()
    {
        this.graph = new SteamGraph();
        this.gson = new Gson();

        Type typePlayer = new TypeToken<Player>(){}.getType();
        Type typeGames = new TypeToken<List<Game>>(){}.getType();
        staticFileLocation("/website");

        get("/player/:id", (req, res) ->
                gson.toJson(
                        graph.getPlayer(req.params(":id")), typePlayer));

        get("/games/:id", (req, res) ->
                gson.toJson(
                        graph.getGameList(req.params(":id")), typeGames));
        System.out.println("Finished starting web server");
    }


    public static void main(String[] arguments)
    {
        new WebServer();
    }
}
