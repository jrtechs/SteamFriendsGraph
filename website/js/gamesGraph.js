/** Nodes in the vis js graph */
var nodes;

/** Edges used to make the Vis JS graph*/
var edges;

/** Used for the loading bar */
var total = 1;
var indexed = 0;
var progressID;

/** Github id of the user being indexed */
var baseID;

/**
 * Vis js graph options
 */
var options = {
    nodes: {
        borderWidth:4,
        size:30,
        color: {
            border: '#222222',
            background: '#666666'
        },
        font:{color:'#eeeeee'}
    },
    edges: {
        color: 'lightgray'
    }
};


/**
 * Checks if a user is a node in the graph
 *
 * @param userID
 * @returns {boolean}
 */
function alreadyInGraph(username)
{
    for(var i = 0; i < nodes.length; i++)
    {
        if(nodes[i].id === username)
        {
            return true;
        }
    }
    return false;
}


/**
 * adds a person to the nodes list
 *
 * @param profileData
 */
function addPersonToGraph(profileData)
{
    addManualToGraph(profileData.id, profileData.avatar);
    for(var i = 0; i < profileData.friends.length; i++)
    {
        addManualToGraph(profileData.friends[i].id,
            profileData.friends[i].avatar);
    }
}

function addManualToGraph(id,avatar)
{
    nodes.push(
        {
            id:id,
            shape: 'circularImage',
            image:avatar
        });
}


/**
 * Adds the followers/following of a person
 * to the graph
 *
 * @param username
 * @param apiPath
 * @returns {Promise<any>}
 */
function addFriends(username)
{
    updateProgress();
    return new Promise((resolve, reject)=>
    {
        getPersonAPI(username, (data)=>
            {
                for(var i = 0; i < data.length; i++)
                {
                    if(!alreadyInGraph(data[i].login))
                    {
                        addPersonToGraph(data[i]);
                    }
                }
                resolve();
            },
            (error)=>
            {
                reject(error);
            })
    });
}


/**
 * Greedy function which checks to see if a edge is in the graphs
 *
 * @param id1
 * @param id2
 * @returns {boolean}
 */
function edgeInGraph(id1, id2)
{
    for(var i = 0;i < edges.length; i++)
    {
        if((edges[i].to === id1 && edges[i].from === id2) ||
            (edges[i].from === id1 && edges[i].to === id2))
        {
            return true;
        }
    }
    return false;
}


/**
 * Adds a connection to the graph
 *
 * @param person1
 * @param person2
 */
function addConnection(id1, id2)
{
    if(id1 !== id2)
    {
        if(alreadyInGraph(id2) && !edgeInGraph(id1, id2))
        {
            network.body.data.edges.add([{
                from: id1,
                to: id2
            }]);
        }
    }
}


/**
 * Processes all the connections of a user and adds them to the graph
 *
 * @param user has .id and .name
 * @returns {Promise<any>}
 */
function processUserConnections(node)
{
    return new Promise((resolve, reject)=>
    {
        getPersonAPI(node.id,
            (data) => {
                updateProgress();
                for (var i = 0; i < data.friends.length; i++) {
                    addConnection(node.id, data.friends[i].id)
                }
                resolve();
            }, (error) => {
                console.log(error);
                resolve();
            });
    });
}


function processUserGames(node)
{
    return new Promise((resolve, reject)=>
    {
        getUserGames(node.id,
            (data) => {
                for (var i = 0; i < data.length; i++) {
                    addConnection(node.id, "" +data[i].appID)
                }
                resolve();
            }, (error) => {
                console.log(error);
                resolve();
            });
    });
}


/**
 * Creates connections between all the nodes in
 * the graph.
 *
 * @returns {Promise<any>}
 */
async function createConnections() {
    for (var i = 0; i < nodes.length; i++)
    {
        await processUserConnections(nodes[i]);
        await processUserGames(nodes[i]);
    }
}


/**
 * Updates progress bar for loading the JS graph
 */
function updateProgress()
{
    indexed++;
    if(indexed >= total)
    {
        $("#" + progressID).html("");
    }
    else
    {
        const percent = parseInt((indexed/total)*100);
        $("#" + progressID).html("<div class=\"progress\">\n" +
            "  <div class=\"progress-bar progress-bar-striped progress-bar-animated\" role=\"progressbar\" style=\"width: " + percent + "%\" aria-valuenow=\"" + percent + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div>\n" +
            "</div>");
    }
}




function getSteamImgURL(game)
{
    return "http://media.steampowered.com/steamcommunity/public/images/apps/" + game.appID + "/" + game.icon + ".jpg"
}


function addGamesToGraph(games)
{
    console.log("adding base player games to graph");

    for(var i = 0; i < games.length; i++)
    {
        var gameLogo = getSteamImgURL(games[i]);
        console.log(gameLogo);
        nodes.push(
            {
                id:"" +games[i].appID,
                image:gameLogo,
                shape: 'image',
                size: 30,
            });
    }
}


var selfData;

/**
 * Adds the base  person to the graph.
 *
 * @param username
 * @returns {Promise<any>}
 */
function addSelfToGraph(id)
{
    return new Promise((resolve, reject)=>
    {
        getPersonAPI(id, (data)=>
            {
                selfData = data;
                baseID = data.id;
                total = data.friends.length;
                addPersonToGraph(data);


                getUserGames(id, (games)=>
                {
                    console.log(games);
                    addGamesToGraph(games);
                    resolve();
                },
                (error2)=>
                {
                    console.log(error2);
                    reject();
                });
            },
            (error)=>
            {
                reject(error);
            });
    });
}


/**
 * Used for the on graph click event
 *
 * @param github id
 */
function bringUpProfileView(uname)
{
    console.log(uname);
    if(uname === selfData.id)
    {
        profileGen(selfData, "profileGen");
    }
    else
    {
        for(var i = 0; i < selfData.friends.length; i++)
        {
            if(selfData.friends[i].id === uname)
            {
                profileGen(selfData.friends[i], "profileGen");
            }
        }
    }
}

var network;
nodes = [];
edges = [];
/**
 * Creates a graph
 * @param username
 * @param containerName
 * @param progressBarID
 */
function createGameGraphs(username, containerName, progressBarID)
{
    progressID = progressBarID;

    addSelfToGraph(username).then(()=>
    {
        $("#" + progressID).html("");

        var container = document.getElementById(containerName);
        var data =
            {
                nodes: nodes,
                edges: edges
            };
        network = new vis.Network(container, data, options);
        bringUpProfileView(selfData.id);
        network.on("click", function (params)
        {
            if(Number(this.getNodeAt(params.pointer.DOM)) !== NaN)
            {
                bringUpProfileView(this.getNodeAt(params.pointer.DOM));
            }
        });
        createConnections().then(()=>
        {
            // $("#" + progressID).html("");
            console.log("Finished");
        })
    }).catch((error)=>
    {
        //$("#" + graphsTitle).html("Error Fetching Data From API");
        // alert("Invalid User");
        console.log(error);
    });
}