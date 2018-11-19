package net.jrtechs.www.webCrawler;

import net.jrtechs.www.SteamAPI.APIConnection;
import net.jrtechs.www.server.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Main class for digging up the entire
 * steam network.
 *
 * @author Jeffery Russell
 */
public class SteamWebCrawler
{
    /** Object used to limit the speed at which I access the steam
     * network */
    private APIThrottler throttler;

    /** Connection to the steam network */
    private APIConnection connection;

    /** Saves players to the disk */
    private FileIO fileIO;

    /** Queue used for a BFS search */
    private Queue<Player> downlaodQueue;


    /**
     * Initializes the steam crawler's objects
     */
    public SteamWebCrawler()
    {
        throttler = new APIThrottler();

        this.connection = new APIConnection();

        this.fileIO = new FileIO("/media/jeff/A4BA9239BA920846/steamData/");

        this.downlaodQueue = new LinkedList<>();
    }


    /**
     * Runs a BFS search of the steam network
     */
    private void runCrawler()
    {
        while(!downlaodQueue.isEmpty())
        {
            Player current = downlaodQueue.remove();

            List<String> currentFriends = connection.getFriends(current.getId());

            List<String> neededFriends = new ArrayList<>();

            currentFriends.forEach(s ->
            {
                if(!fileIO.playerExists(s))
                    neededFriends.add(s);
            });

            connection.getFullPlayers(neededFriends).forEach(f->
            {
                downlaodQueue.add(f);
            });


            int queriesRan = neededFriends.size()/100 + 2;
            this.throttler.wait(queriesRan);

            if(!fileIO.playerExists(current.getId()))
            {
                fileIO.writeToFile(current, currentFriends);
            }
        }
    }


    /**
     * pop first fiend on the queue
     * and release the beast
     *
     * @param baseID
     */
    public void runSteamCrawlerBase(String baseID)
    {
        downlaodQueue.add(new Player("jrtechs", baseID));
        runCrawler();
    }



    public static void main(String args[])
    {
        new SteamWebCrawler().runSteamCrawlerBase("76561198188400721");
    }
}
