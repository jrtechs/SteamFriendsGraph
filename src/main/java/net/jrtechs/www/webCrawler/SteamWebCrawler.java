package net.jrtechs.www.webCrawler;

import net.jrtechs.www.SteamAPI.APIConnection;
import net.jrtechs.www.server.Player;

import java.util.*;


/**
 * Main class for digging up the entire
 * steam network.
 *
 * @author Jeffery Russell 11-18-18
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
    private LinkedList<Player> downlaodQueue;

    /** Players which have been detected by
     * our search and currently in a queue
     * or has already been processed*/
    private HashSet<String> visited;

    /** List of players which we have accessed
     * in the steam network, but, have no clue what
     * their name is*/
    private LinkedList<String> namelessQueue;


    /**
     * Initializes the steam crawler's objects
     */
    public SteamWebCrawler()
    {
        throttler = new APIThrottler();

        this.connection = new APIConnection();

        this.fileIO = new FileIO("/media/jeff/A4BA9239BA920846/steamData/");

        this.downlaodQueue = new LinkedList<>();

        visited = new HashSet<>();

        namelessQueue = new LinkedList<>();
    }


    /**
     * If the download queue is empty, this will
     * look up the names of the first 100 players in the
     * nameless queue and add them to download queue.
     */
    private void shiftNamelessToDownload()
    {
        //this is a while instead of if because the getfull players query fails
        //once in a blue moon
        while(this.downlaodQueue.isEmpty() && !this.namelessQueue.isEmpty())
        {
            List<String> winners = new ArrayList<>();
            for(int i = 0; i < (100 < namelessQueue.size()? 100: namelessQueue.size()); i++)
            {
                winners.add(this.namelessQueue.remove());
            }
            List<Player> namedPlayers = connection.getFullPlayers(winners);
            this.throttler.wait(1);
            downlaodQueue.addAll(namedPlayers);
        }
    }


    /**
     * Does one of the following three actions for each
     * of the steam members in the list:
     * 1: Ignore- already has been queued by program
     * 2: Add to nameless queue -- doesn't have name yet
     * 3: Add to download queue -- already on HHD but needed for
     * the search algo to work.
     *
     * @param ids list of steam ids
     */
    private void queueUpPlayers(List<String> ids)
    {
        for(String s: ids)
        {
            if(!visited.contains(s))
            {
                if(fileIO.playerExists(s))
                {
                    downlaodQueue.add(new Player("dummy", s));
                }
                else
                {
                    namelessQueue.add(s);
                }
                visited.add(s);
            }
        }
        System.out.println("Download Queue: " + downlaodQueue.size());
        System.out.println("Nameless Queue: " + namelessQueue.size());
    }




    /**
     * Runs a BFS search of the steam network
     */
    private void runCrawler()
    {
        while(!downlaodQueue.isEmpty())
        {
            Player current = downlaodQueue.remove();

            List<String> currentFriends;
            if(!fileIO.playerExists(current.getId()))
            {
                this.throttler.wait(1);
                currentFriends = connection.getFriends(current.getId());
                fileIO.writeToFile(current, currentFriends);
            }
            else
            {
                currentFriends = fileIO.readFriends(current.getId());
            }

            queueUpPlayers(currentFriends);

            shiftNamelessToDownload();
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
