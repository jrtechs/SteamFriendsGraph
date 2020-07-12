package net.jrtechs.www;

import net.jrtechs.www.server.old.Server;

/**
 * Launcher for the server
 *
 * @author Jeffery Russell 6-9-18
 */
@Deprecated
public class App 
{
    public static void main( String[] args )
    {
        System.out.println("Starting Server");
        new Server().start();
    }
}