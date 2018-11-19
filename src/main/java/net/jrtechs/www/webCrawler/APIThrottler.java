package net.jrtechs.www.webCrawler;

import java.util.Calendar;

/**
 * @author Jeffery Russell
 */
public class APIThrottler
{
    public int totalqueries;

    private long lastQuery;


    boolean queryAvailable()
    {
        return true;
    }


    public long getCurrentTimeInMS()
    {
        Calendar calendar = Calendar.getInstance();
        //Returns current time in millis
        long timeMilli2 = calendar.getTimeInMillis();
        return timeMilli2;
    }
}
