package net.jrtechs.www.SteamAPI;


/**
 * @author Jeffery Russell 11-19-18
 */
public class SteamConnectionException extends Exception
{
    private ConnectionErrors error;

    public SteamConnectionException(ConnectionErrors error)
    {
        this.error = error;
    }


    public ConnectionErrors getError() {
        return error;
    }
}
