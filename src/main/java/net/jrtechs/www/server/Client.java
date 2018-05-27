package net.jrtechs.www.server;

import org.java_websocket.WebSocket;

public class Client extends Thread
{

    private WebSocket client;



    public Client(WebSocket client)
    {
        this.client = client;
    }

    public void recievedMessage(String message)
    {

    }

    public WebSocket getSocket()
    {
        return this.client;
    }

    @Override
    public void run() {

    }
}
