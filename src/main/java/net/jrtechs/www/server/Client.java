package net.jrtechs.www.server;

import net.jrtechs.www.graphDB.SteamGraph;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.List;


/**
 * Client thread which gets graph information from
 * the graphDB and sends it to the client so that
 * they can render it in their web browser.
 *
 * @author Jeffery Russell 5-27-18
 */
public class Client extends Thread
{
    /** Web connection to the client */
    private WebSocket client;

    /** Graph interface to fetch data */
    private SteamGraph graph;

    /** base id to look at */
    private String baseId;

    /** How many layers of friends we are traversing */
    private int debth;


    private int type;


    /**
     * Initializes the client with a steam graph and
     * web socket information.
     * @param client
     */
    public Client(WebSocket client, String id, int type)
    {
        this.client = client;
        this.graph = new SteamGraph();
        this.type = type;
        this.baseId = id;
        this.debth = 1;
    }


    /**
     * returns the web socket object
     *
     * @return
     */
    public WebSocket getSocket()
    {
        return this.client;
    }


    /**
     * Sends the client the request to add a new node to their
     * graph.
     *
     * @param p
     */
    private void sendNodeAdd(Player p, int x, int y)
    {
        JSONObject request = new JSONObject();
        request.put("action", 1);
        request.put("id", p.getId());
        request.put("name", p.getName());

        request.put("x", x);

        request.put("y", y);

        this.sendJSON(request);
    }


    /**
     * Sends the client to request to connect two nodes
     * via an edge.
     *
     * @param p1
     * @param p2
     */
    private void sendEdgeAdd(Player p1, Player p2)
    {
        JSONObject request = new JSONObject();
        request.put("action", 2);
        request.put("id", p1.getId() + p2.getId());
        request.put("p1", p1.getId());
        request.put("p2", p2.getId());

        this.sendJSON(request);
    }


    private void sendJSON(JSONObject request)
    {
        this.client.send(request.toString());
        try
        {
            Thread.sleep(50); //prevents DDOSing the client
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Sends an entire player and all of their friends to the client
     *
     * @param p
     */
    private void sendPlayerToClient(Player p, int x, int y, int gen)
    {
        if(gen == 1)
        {
            sendNodeAdd(p, x, y);
        }

        List<Player> friends = p.fetchFriends();

        double radianStep = Math.PI * 2 / friends.size();

        double currentStep = 0;

        for(Player friend: friends)
        {
            this.sendNodeAdd(friend, (int)(x + Math.cos(currentStep) * (300/gen)), (int)(y + Math.sin(currentStep) * (300/gen)));

            this.sendEdgeAdd(p, friend);

            currentStep += radianStep;
        }
    }


    /**
     * Where the magic happens
     */
    @Override
    public void run()
    {
        Player b = this.graph.getPlayer(this.baseId);

        List<Player> friends = b.fetchFriends();
        this.sendPlayerToClient(b, 300, 243, 1);

        double radianStep = Math.PI * 2 / friends.size();

        double currentStep = 0;


        for(Player f : b.fetchFriends())
        {
            f = this.graph.getPlayer(f.getId());
            this.sendPlayerToClient(f, (int)(300 + Math.cos(currentStep) * 300), (int)(243 + Math.sin(currentStep) * 300) ,2);

            currentStep += radianStep;
        }

        this.client.close();
    }
}