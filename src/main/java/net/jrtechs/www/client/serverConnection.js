var connection = new WebSocket('ws://127.0.0.1:4444');

connection.onopen = function ()
{
    console.log('Connected!');
    connection.send('Ping'); // Send the message 'Ping' to the server
};

// Log errors
connection.onerror = function (error)
{
    console.log('WebSocket Error ' + error);
};

function addNodeToGraph(request)
{
    s.graph.addNode({
        id: request.id,
        label: request.name,
        x: Math.random(),
        y: Math.random(),
        size: Math.random(),
        color: '#666'
    });
    s.refresh();
}


function addEdgeToGraph(request)
{
    s.graph.addEdge({
        id: request.id,
        source: request.p1,
        target: request.p2,
        size: Math.random(),
        color: '#000'
    });
    s.refresh();
}


// Log messages from the server
connection.onmessage = function (e)
{
    var request = JSON.parse(e.data);

    if(request.action == 1)
    {
        addNodeToGraph(request);
    }
    else if(request.action == 2)
    {
        addEdgeToGraph(request);
    }
    console.log('Server: ' + e.data);
};