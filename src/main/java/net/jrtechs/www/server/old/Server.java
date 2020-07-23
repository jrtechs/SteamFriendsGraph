package net.jrtechs.www.server.old;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;


/**
 * Socket server which listens for clients
 *
 * @author Jeffery Russell 5-26-18
 */
public class Server extends WebSocketServer
{
    /** port to listen on **/
    private static int TCP_PORT = 4444;

    /** clients connected to the server **/
    private Set<Client> clients;


    /**
     * Initializes the server and creates an empty set of clients
     */
    public Server()
    {
        super(new InetSocketAddress(TCP_PORT));

        System.out.println(super.getAddress());
        clients = new HashSet<>();
    }


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake)
    {
        System.out.println("New connection from " +
                conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote)
    {
        this.removeClient(conn);
        System.out.println("Closed connection to " +
                conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message)
    {
        JSONObject object = new JSONObject(message);

        if(object.has("graph"))
        {
            for (Client client : clients)
            {
                if(client.getSocket() == conn)
                {
                    return;
                }
            }

            Client newClient = new Client(conn, object.getString("id"),
                    object.getInt("graph"));
            clients.add(newClient);

            newClient.start();
        }
        else if(object.has("go"))
        {
            for(Client client: clients)
            {
                if(client.getSocket() == conn)
                {
                    client.sendNextRequest();
                }
            }
        }


    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        ex.printStackTrace();
        if (conn != null)
        {
            clients.remove(conn);
            // do some thing if required
        }
        System.out.println("ERROR from " + conn.getRemoteSocketAddress()
                .getAddress().getHostAddress());
    }

    @Override
    public void onStart() {

    }


    /**
     * Removes a client from the main list of clients
     * based on the websocket that needs to be removed.
     *
     * @param conn
     */
    public void removeClient(WebSocket conn)
    {
        for(Client c: clients)
        {
            if(c.getSocket() == conn)
            {
                this.clients.remove(c);
                c.stop();
            }
        }
    }


    /**
     * Starts the web socket server
     *
     * @param args
     */
    public static void main(String[] args)
    {
        new Server().start();
    }

}