package net.jrtechs.www.server;

import net.jrtechs.www.Player;
import net.jrtechs.www.graphDB.SteamGraph;
import org.java_websocket.WebSocket;
import org.json.JSONObject;


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


    /**
     * Initializes the client with a steam graph and
     * web socket information.
     * @param client
     */
    public Client(WebSocket client)
    {
        this.client = client;
        this.graph = new SteamGraph();

        //temp stuff
        this.baseId = "76561198188400721";
        this.debth = 1;
    }


    /**
     * Method which is called when the client sends a message
     * to the server.
     *
     * @param message
     */
    public void receivedMessage(String message)
    {

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
    private void sendNodeAdd(Player p)
    {
        JSONObject request = new JSONObject();
        request.put("action", 1);
        request.put("id", p.getId());
        request.put("name", p.getName());

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
        System.out.println("sending " + request.toString());
        this.client.send(request.toString());
    }


    /**
     * Sends an entire player and all of their friends to the client
     *
     * @param p
     */
    private void sendPlayerToClient(Player p)
    {
        sendNodeAdd(p);

        for(Player friend: p.fetchFriends())
        {
            this.sendNodeAdd(friend);

            this.sendEdgeAdd(p, friend);
        }
    }


    /**
     * Where the magic happens
     */
    @Override
    public void run()
    {
        Player b = this.graph.getPlayerInformation(this.baseId);

        this.sendPlayerToClient(b);
    }
}
