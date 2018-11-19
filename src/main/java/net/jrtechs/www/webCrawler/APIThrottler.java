package net.jrtechs.www.webCrawler;

import java.util.Calendar;

/**
 * Class which is used to throttle your
 * program to not query an API too fast.
 *
 * @author Jeffery Russell
 */
public class APIThrottler
{
    /** The total amount of queries ran */
    public int totalqueries;

    /** Time in MS that the last query ran */
    private long lastQuery;

    /** The number of MS that we have to wait
     * between each query to not get our account
     * banned from steam*/
    private int waitTimePerQuerie;


    /**
     * initializes start parameters
     */
    public APIThrottler()
    {
        lastQuery = getCurrentTimeInMS();
        waitTimePerQuerie = 864;
    }


    /**
     * Determines if it a certain amount
     * of time has passed since the last
     * query
     *
     * @param waitTime
     * @return
     */
    private boolean queryAvailable(int waitTime)
    {
        long currTime = getCurrentTimeInMS();

        return currTime > lastQuery + waitTime;

    }


    /**
     * Pauses untill the wait time out has been met
     * @param numofQueries
     */
    public void wait(int numofQueries)
    {
        int totalWaitTime = numofQueries * waitTimePerQuerie;

        while(!queryAvailable(totalWaitTime))
        {

        }
        lastQuery = getCurrentTimeInMS();
        totalqueries++;
        if(totalqueries % 1000 == 0)
            System.out.println("Queries ran: " + totalqueries);
    }


    /**
     * Fetches the current time in milliseconds
     **/
    public long getCurrentTimeInMS()
    {
        Calendar calendar = Calendar.getInstance();
        //Returns current time in millis
        long timeMilli2 = calendar.getTimeInMillis();
        return timeMilli2;
    }
}
