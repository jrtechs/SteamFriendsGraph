package net.jrtechs.www.webCrawler;

import net.jrtechs.www.SteamAPI.APIConnection;

import java.io.File;


/**
 * Main class for digging up the entire
 * steam network.
 *
 * @author Jeffery Russell
 */
public class SteamWebCrawler
{

    private APIThrottler throttler;

    private APIConnection connection;

    private FileIO fileIO;

    public void runSteamCrawler(String baseID)
    {

    }



    public static void main(String args[])
    {
        new SteamWebCrawler().runSteamCrawler("76561198188400721");
    }
}
