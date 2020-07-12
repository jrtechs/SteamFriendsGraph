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
            console.log("adding edge dynamic")
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
    updateProgress();
    getPersonAPI(node.id,
        (data)=>
        {
            for(var i = 0; i < data.friends.length; i++)
            {
                console.log("adding new con");
                addConnection(node.id, data.friends[i].id)
            }
        }, (error)=>
        {
            console.log(error);
        });
}


/**
 * Creates connections between all the nodes in
 * the graph.
 *
 * @returns {Promise<any>}
 */
function createConnections()
{
    return new Promise((resolve, reject)=>
    {
        var prom = [];
        for(var i = 0; i < nodes.length; i++)
        {
            processUserConnections(nodes[i]);
        }

    });
}


/**
 * Updates progress bar for loading the JS graph
 */
function updateProgress()
{
    indexed++;
    const percent = parseInt((indexed/total)*100);

    $("#" + progressID).html("<div class=\"progress\">\n" +
        "  <div class=\"progress-bar progress-bar-striped progress-bar-animated\" role=\"progressbar\" style=\"width: " + percent + "%\" aria-valuenow=\"" + percent + "\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div>\n" +
        "</div>");
}


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
            baseID = data.id;
            total = data.friends.length;
            addPersonToGraph(data);
            resolve();
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
    for(var i = 0; i < nodes.length; i++)
    {
        if(nodes[i].id === uname)
        {
            profileGen(nodes[i].id, "profileGen");
        }
    }
}

var network;

/**
 * Creates a graph
 * @param username
 * @param containerName
 * @param progressBarID
 */
function createFriendsGraph(username, containerName, progressBarID)
{
    progressID = progressBarID;

    nodes = [];
    edges = [];
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


        createConnections().then(()=>
        {

            network.on("click", function (params)
            {
                if(Number(this.getNodeAt(params.pointer.DOM)) !== NaN)
                {
                    bringUpProfileView(this.getNodeAt(params.pointer.DOM));
                }
            });
        })
    }).catch((error)=>
    {
        //$("#" + graphsTitle).html("Error Fetching Data From API");
        alert("Invalid User");
    });
}