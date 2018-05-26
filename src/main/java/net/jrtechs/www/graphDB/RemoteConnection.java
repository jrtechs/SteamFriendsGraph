package net.jrtechs.www.graphDB;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.ResultSet;

import net.jrtechs.www.utils.ConfigLoader;

/**
 * Simple helper class which allows us to remotely connect to a
 * remote graph
 *
 * @author Jeffery Russell 5-24-18
 */
public class RemoteConnection
{

    /** Stores/manages client connections **/
    private Cluster cluster;


    /**
     * Connection to the graph db
     */
    private Client client;


    /**
     * Connects to a remote Graph database
     *
     * Check this link out to learn about Cluster.Builder()
     * http://tinkerpop.apache.org/javadocs/3.3.3/core/org/apache/tinkerpop/
     * gremlin/driver/Cluster.Builder.html
     *
     */
    public RemoteConnection()
    {
        ConfigLoader conf = new ConfigLoader("GremlinServerConnection.json");

        Cluster.Builder b = Cluster.build();
        b.addContactPoint(conf.getValue("host"));
        b.port(conf.getInt("port"));

        b.credentials(conf.getValue("username"), conf.getValue("password"));

        this.cluster = b.create();

        this.client = cluster.connect();
    }


    /**
     * Queries the graph and return the results which can be iterated over
     *
     * ex:
     *  ResultSet results = remote.queryGraph("g.V().values('name')");
     *
     *  results.stream().forEach(result ->
     *  {
     *       String s = result.getString();
     *       System.out.println("name: " + s);
     *  });
     *
     * @param q
     * @return
     */
    public ResultSet queryGraph(String q)
    {
        return this.client.submit(q);
    }


    /**
     * Closes connection with remote database
     */
    public void closeConnection()
    {
        this.cluster.close();
    }


    /**
     * testing method which will be removed soon
     * @param args
     */
    public static void main(String args[])
    {
        RemoteConnection remote = new RemoteConnection();
        ResultSet results = remote.queryGraph("g.V().values('name')");

        //results.stream().forEach(System.out::println);

        results.stream().forEach(result ->
        {
            String s = result.getString();
            System.out.println("name: " + s);
        });

        remote.closeConnection();
    }
}