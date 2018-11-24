package net.jrtechs.www.SteamAPI;


/**
 * Exception to represent the types of HTTP exceptions
 * that the steam api throws.
 *
 * @author Jeffery Russell 11-19-18
 */
public class SteamConnectionException extends Exception
{
    /**
     * Type of error which the Steam API threw
     */
    private ConnectionErrors error;


    /**
     * Creates a new error with a connection type
     *
     * @param error
     */
    public SteamConnectionException(ConnectionErrors error)
    {
        this.error = error;
    }


    /**
     * returns the type of error the scrapper encountered
     *
     * @return
     */
    public ConnectionErrors getError()
    {
        return error;
    }
}
