/**
 * Simple file which uses jQuery's ajax
 * calls to make it easier to get data
 * from the steam api.
 *
 * @author Jeffery Russell 2-16-19, 7-12-20
 */


const APIROOT = "";

const API_USER_PATH = "/player/";

const API_GAMES_PATH = "/games/"

/**
 * Fetches a list of fiends for a user.
 * 
 * @param {*} userName 
 * @param {*} suc 
 * @param {*} err 
 */
function getPersonAPI(userID, suc, err)
{
    // api/friends/jrtechs
    const urlpath = APIROOT + API_USER_PATH + userID;
    runAjax(urlpath, suc, err);
}


function getUserGames(userID, suc, err)
{
    //ex: http://localhost:7000/api/repositories/jwflory
    const urlpath = APIROOT + "/games/" + userID;
    runAjax(urlpath, suc, err);
}


/**
 * Queries github API end points with the backend
 * proxy server for github graphs.
 * 
 * @param {*} url 
 * @param {*} successCallBack 
 * @param {*} errorCallBack 
 */
function queryUrl(url, successCallBack, errorCallBack)
{
    url = url.split("https://api.github.com/").join("api/");
    runAjax(url, successCallBack, errorCallBack);
}


/**
 * Wrapper for AJAX calls so we can unify
 * all of our settings.
 * 
 * @param {*} url -- url to query
 * @param {*} successCallBack  -- callback with data retrieved
 * @param {*} errorCallBack  -- callback with error message
 */
function runAjax(url, successCallBack, errorCallBack)
{
    console.log(url);
    $.ajax({
        type:'GET',
        url: url,
        crossDomain: true,
        dataType: "json",
        success: successCallBack,
        error:errorCallBack,
        timeout: 300000
    });
}