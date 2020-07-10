package net.jrtechs.www.graphDB;


import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.core.JanusGraphFactory;

/**
 * @author Jeffery Russell 7-10-2020
 */
public class GraphConnection
{
    private Graph graph;

    private GraphTraversalSource t;

    public GraphConnection()
    {
        this.graph = JanusGraphFactory.open("conf/graph.properties");
        t = graph.traversal();
    }


    public void closeConnection() throws Exception
    {
        this.commit();
        this.graph.close();
    }

    public void commit()
    {
        this.t.tx().commit();
    }


    public GraphTraversalSource getTraversal()
    {
        return this.t;
    }
}
