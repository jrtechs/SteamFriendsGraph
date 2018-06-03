# Steam Graph Analysis

This is a project that I threw together during the weekend to play around with
gremlin graph database. Currently this project scrapes the steam API for friends
and their friends which can be used to generate a graph. This information is stored
locally in a gremlin server and is then sent to the client via a web socket. 

![Diagram](website/Diagram.svg)


[Video Of Friends of Friends Graph](https://www.youtube.com/watch?v=WJfo9bU0nH8)


This project is in the VERY early stages of development and is far from finished.
If you are lucky, you will find it live at [http://jrtechs.student.rit.edu/friendsOfFriends.html](http://jrtechs.student.rit.edu/friendsOfFriends.html).
It is still being actively developed and does not have permanent hosting so there is a %60
chance at any time that you will be able to access it. 

![Graph](website/exampleGraph.png)


# Bugs
* Tends to crash w/o telling user if you provide an invalid steam id


# TODO
* Include a steam name to steam id lookup
* Dockerize this entire environment
* Connect the gremlin/janus server to a HBase server for persistent storage
* Make the graphs look better -- possibly switch from sigma.js to d3
* Get the java web socket to work with ssh -- currently does not work with wss
* Make more graphs to provide more insights
    * Friends with friends -- shows which of your friends are friends with each other
    * Most common friends friends -- will show you people you may know
    * Graph of a larger chunk of the steam community
    * ...
* Write more documentation on how the system as a whole works.
* Write blog post on what I learned during this project.    