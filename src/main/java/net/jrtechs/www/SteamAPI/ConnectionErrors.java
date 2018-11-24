package net.jrtechs.www.SteamAPI;

/**
 * Errors thrown by the steam API at times.
 *
 * @see  <a href="https://partner.steamgames.com/doc/webapi_overview/responses">Steam API Docs</a>
 *
 * @author Jeffery Russell 11-19-18
 */
public enum ConnectionErrors
{
    /** No errors? */
    VALID,

    /** 500 connection error with the server */
    CONNECTION,

    /** The profile is private 401 */
    RESTRICTED,

    /** Invalid Steam api  403 */
    FORBIDDEN,

    /** 429 you are being rate limited */
    RATE_LIMITED,

    /** The steam api threw a 404 not found error */
    NOT_FOUND,

    /** The request being made is in the wrong format */
    BAD_REQUEST
}
