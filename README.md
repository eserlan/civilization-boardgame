Civlization boardgame (Fantasy Flight Games)
=================================

##First version

This will hopefully be a web application where you can use to randomize items for the Civilization boardgame. The first version is targeted for Play By Forums.

This project will consist of server and client, where the server will create Play By Forum games and shuffle cards and hand out items to players.
The client will be a web application that can create games, let players join these games and make draws.

The first version will be pretty basic, where you can only draw items.

##Coming soon
Future version will be a fully playable web client with drag and drop play

##Requires

###civilizaton-rest	
* Maven
* Java 8
* MongoDB
* Lombok (www.projectlombok.org)

###civilization-web
* NodeJS (https://www.npmjs.org/)
* bower (http://bower.io/
* Grunt (http://gruntjs.com/)

##Installation
Install what is required.
To run the rest server, run mvn install and mvn exec:java on civilization-rest
and to run the frontend server, run bower install, and then grunt serve on civilization-web
