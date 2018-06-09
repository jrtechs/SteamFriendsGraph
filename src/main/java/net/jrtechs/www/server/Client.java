package net.jrtechs.www.server;

import net.jrtechs.www.graphDB.SteamGraph;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.util.ArrayList;
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

    /** JSONObjects to send the client */
    private List<JSONObject> queue;


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
        this.queue = new ArrayList<>();
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
    private void sendNodeAdd(Player p, int x, int y, int size)
    {
        JSONObject request = new JSONObject();
        request.put("action", 1);
        request.put("id", p.getId());
        request.put("name", p.getName());
        request.put("size", size);
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

    /**
     * Tells the tells the js on the client side to start
     * the force applied to the graph
     */
    private void sendFinished()
    {
        JSONObject request = new JSONObject();
        request.put("action", 3);
        this.sendJSON(request);
    }


    /**
     * sends a json object to the client
     *
     * @param request
     */
    private void sendJSON(JSONObject request)
    {
        this.queue.add(request);
    }


    /**
     * Sends the next object to the client
     */
    public void sendNextRequest()
    {
        while(this.queue.isEmpty())
        {
            try
            {
                Thread.sleep(500);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        JSONObject send =queue.remove(0);
        this.client.send(send.toString());
        if(send.getInt("action") == 3)
        {
            this.client.close();
        }
    }


    /**
     * Sends an entire player and all of their friends to the client
     *
     * @param p
     */
    private void sendPlayerToClient(Player p, int x, int y,
                                    int gen, int multiplier)
    {
        if(gen == 1)
        {
            sendNodeAdd(p, x, y, 150);
        }

        List<Player> friends = p.fetchFriends();

        double radianStep = Math.PI * 2 / friends.size();

        double currentStep = 0;

        for(Player friend: friends)
        {
            if(gen == 1)
            {
                this.sendNodeAdd(friend, (int)(x + Math.cos(currentStep) *
                        (multiplier/gen)), (int)(y + Math.sin(currentStep) *
                        (multiplier/gen)), 150);
            }
            else
            {
                this.sendNodeAdd(friend, (int)(x + Math.cos(currentStep) *
                        (multiplier/gen)), (int)(y + Math.sin(currentStep) *
                        (multiplier/gen)), 30);
            }


            this.sendEdgeAdd(p, friend);

            currentStep += radianStep;
        }
    }


    /**
     * Generates a friends of friends graph for the client
     */
    private void friendsOfFriends()
    {
        Player b = this.graph.getPlayer(this.baseId);

        List<Player> friends = b.fetchFriends();
        this.sendPlayerToClient(b, 300, 243, 1, 300);

        double radianStep = Math.PI * 2 / friends.size();

        double currentStep = 0;


        for(Player f : b.fetchFriends())
        {
            f = this.graph.getPlayer(f.getId());
            this.sendPlayerToClient(f, (int)(300 + Math.cos(currentStep) * 300),
                    (int)(243 + Math.sin(currentStep) * 300) ,2, 300);

            currentStep += radianStep;
        }
        this.sendFinished();
    }


    /**
     * Generates the friends with friends graph for the client
     *
     * Displays all of the requested ids friends, then it only adds edges
     * between players if they are both friends of yours.
     */
    private void friendsWithFriends()
    {
        Player b = this.graph.getPlayer(this.baseId);

        this.sendPlayerToClient(b, 600, 440, 1, 600);

        for(Player f : b.fetchFriends()) //all my friends
        {
            f = this.graph.getPlayer(f.getId());
            for(Player ff : f.fetchFriends()) // all my friends friends
            {
                for(Player f2 : b.fetchFriends()) // all my friends
                {
                    if(f2.getId().equals(ff.getId()))
                    {
                        this.sendEdgeAdd(f, ff);
                    }
                }
            }
        }
        this.sendFinished();
    }


    /**
     * Where the magic happens
     */
    @Override
    public void run()
    {
        if(this.type == 1)
        {
            friendsOfFriends();
        }
        else
        {
            friendsWithFriends();
        }
    }
}