This project is a backend for a service that computes the six degrees of Kevin Bacon which is basically the shortest path between Kevin Bacon and a given actor in the database.

The backend model containing the Http server, server context and REST API endpoints were implemented using Java. 

The endpoints for addActor, addMovie, addRelationship, and addReview represent PUT requests.

The endpoints for getMovie, getActor, hasRelationship, computeBaconNumber and computeBaconPath represent GET requests. 

The REST API endpoints were supported by Neo4j graph databases.

The project was tested using the Robot Framework.

Postman was used throughout the project to help evaluate the status of each endpoint. 
