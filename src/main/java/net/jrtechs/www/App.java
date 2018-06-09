package net.jrtechs.www;

import net.jrtechs.www.server.Server;

/**
 * Launcher for the server
 *
 * @author Jeffery Russell 6-9-18
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println("Starting Server");
        new Server().start();
    }
}