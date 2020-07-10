package net.jrtechs.www.graphDB;


import org.apache.tinkerpop.gremlin.process.remote.RemoteConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.IntStream;


/**
 * Class used to test connection with remote database and issue queries while
 * developing our project
 *
 * @author Jeffery Russell 8-25-18
 */
public class GremlinConsole
{
    /** Connection to graph server **/
    private GraphConnection connection;


    /**
     * Instantiates the remote connection
     */
    public GremlinConsole()
    {
        this.connection = new GraphConnection();
    }


    /**
     * Fetches remote connection of the console
     *
     * @return
     */
    public GraphConnection getConnection()
    {
        return this.connection;
    }



    /**
     * Gets input from the user and queries the graph server and prints out
     * the output.
     *
     * There is excessive try catching to prevent a bad query/input from crashing
     * the console
     */
    public void run()
    {
        BufferedReader br = null;

        try
        {
            br = new BufferedReader(new InputStreamReader(System.in));

            while (true)
            {

                System.out.print("Enter Query: ");
                String input = br.readLine();

                if ("q".equals(input))
                {
                    System.out.println("Exit!");
                    System.exit(0);
                }



//                ResultSet set = this.connection.queryGraph(input);
//                try
//                {
//                    set.forEach(System.out::println);
//                }
//                catch (Exception ex)
//                {
//                    ex.printStackTrace();
//                }

            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Makes this script runnable as a stand alone application for use
     *
     * To run queries from command line arguments, surround them with double
     * quotes and add spaces between queries
     *
     * ex usage:
     *  "g.V().hasLabel('Satellite').values('OBJECT_ID')" "g.V().hasLabel
     *  ('Country').has('abr', 'US').valueMap()"
     *
     * @param args
     */
    public static void main(String args[]) throws Exception
    {
//        GremlinConsole console = new GremlinConsole();
//
//        //don't worry about this lambda
//        IntStream.range(0, args.length)
//                .forEach(i-> console.getConnection()
//                        .queryGraph(args[i])
//                        .forEach((System.out::println)));
//
//        console.run();

        GraphConnection con = new GraphConnection();

        System.out.println(con.getTraversal().E().toList()
            );
        System.out.println(con.getTraversal().V().toList()
        );
        con.closeConnection();
    }
}
