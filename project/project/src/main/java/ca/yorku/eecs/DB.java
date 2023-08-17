package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.v1.Values.parameters;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

public class DB {

    private Driver driver;
    private String uriDb;

    public DB(){
        uriDb = "bolt://localhost:7687";
        Config config = Config.builder().withoutEncryption().build();
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j", "12345678"), config);
    }

    public void handlePut(HttpExchange request) throws IOException {
        String path = request.getRequestURI().getPath();
        try {
            if (path.contains("/api/v1/addActor")) {
                addActor(request);
            } else if (path.contains("/api/v1/addMovie")) {
                addMovie(request);
            } else if (path.contains("/api/v1/addRelationship")) {
                addRelationship(request);
            }else if (path.contains("/api/v1/addReview")) {
                addReview(request);
            } else{
                Utils.sendString(request, "Bad request: query method not spelled correctly\n", 400);
            }

        } catch (IOException e) {
            e.printStackTrace();
            String r = "Server Error: " + e.getMessage() + "\n";
            Utils.sendString(request, r, 500);
        }

        driver.close();
    }


    private void addActor(HttpExchange request) throws IOException {

        try{
            String requestBody = Utils.getBody(request);
            JSONObject json = new JSONObject(requestBody);
            String name = json.optString("name", null);
            String actorId = json.optString("actorId", null);

            if(name==null || actorId==null || name.isEmpty() || actorId.isEmpty()){
                Utils.sendString(request, "Improper Formatting: missing input", 400);
            }else{
                    try (Session session = driver.session()) {
                        try (Transaction tx = session.beginTransaction()) {
                            StatementResult result = tx.run("MATCH (a: actor {actorId: $actorId}) RETURN a", parameters("actorId", actorId));
                            if(!result.hasNext()){
                                tx.run("CREATE (a: actor {actorId: $actorId, name: $name, movies: []})", parameters("actorId", actorId, "name", name));
                                tx.success();
                                Utils.sendString(request, "Actor Added Successfully", 200);
                            }else{
                                Utils.sendString(request, "Actor Exists Already", 400);
                            }

                        }


                    }
            }
        }catch(Exception e){
            e.printStackTrace();
            String r = "Server Error: " + e.getMessage() + "\n";
            Utils.sendString(request, r, 500);
        }

    }
//here is our feature, it's the inclusion of a rating for a movie
    private void addMovie(HttpExchange request) throws IOException{
        try{
            String requestBody = Utils.getBody(request);
            JSONObject json = new JSONObject(requestBody);
            String name = json.optString("name", null);
            String movieId = json.optString("movieId", null);
            String review = "Movie has not been reviewed";
            if(name==null || movieId==null || name.isEmpty() || movieId.isEmpty()){
                Utils.sendString(request, "Improper Formatting: missing input", 400);
            }else{
                try (Session session = driver.session()) {
                    try (Transaction tx = session.beginTransaction()) {
                        StatementResult result = tx.run("MATCH (m: movie {movieId: $movieId}) RETURN m", parameters("movieId", movieId));
                        if(!result.hasNext()){
                            tx.run("CREATE (m: movie {movieId: $movieId, name: $name, review: $review, actors: []})", parameters("movieId", movieId, "name", name, "review", review));
                            tx.success();
                            Utils.sendString(request, "Movie Added Successfully", 200);
                        }else{
                            Utils.sendString(request, "Movie Exists Already", 400);
                        }

                    }


                }
            }
        }catch(Exception e){
            e.printStackTrace();
            String r = "Server Error: " + e.getMessage() + "\n";
            Utils.sendString(request, r, 500);
        }
    }

//    this is our feature, it adds a review
    private void addReview(HttpExchange request) throws IOException{
        try{
            String requestBody = Utils.getBody(request);
            JSONObject json = new JSONObject(requestBody);
            String rating = json.optString("review");
            String movieId = json.optString("movieId", null);
            if(rating==null || movieId==null || rating.isEmpty() || movieId.isEmpty()){
                Utils.sendString(request, "Improper Formatting: missing input", 400);
            }else{
                try (Session session = driver.session()) {
                    try (Transaction tx = session.beginTransaction()) {
                        StatementResult result = tx.run("MATCH (m: movie {movieId: $movieId}) RETURN m", parameters("movieId", movieId));
                        if(result.hasNext()){
                            tx.run("MATCH (m:movie {movieId: $movieId})" + "SET m.review = $rating", parameters( "movieId", movieId, "rating", rating));
                            tx.success();
                            Utils.sendString(request, "Rating Added Successfully", 200);
                        }else{
                            Utils.sendString(request, "Movie Doesn't Exist", 400);
                        }

                    }


                }
            }
        }catch(Exception e){
            e.printStackTrace();
            String r = "Server Error: " + e.getMessage() + "\n";
            Utils.sendString(request, r, 500);
        }
    }

    private void addRelationship(HttpExchange request) throws IOException{
        try{
            String requestBody = Utils.getBody(request);
            JSONObject json = new JSONObject(requestBody);
            String actorId = json.optString("actorId", null);
            String movieId = json.optString("movieId", null);

            if(actorId==null || movieId==null || actorId.isEmpty() || movieId.isEmpty()){
                Utils.sendString(request, "Improper Formatting: missing input", 400);
            }else{
                try (Session session = driver.session()) {
                    try (Transaction tx = session.beginTransaction()) {
                        StatementResult hasRel = tx.run( "MATCH (a:actor {actorId: $actorId})-[r:ACTED_IN]->(m:movie {movieId: $movieId}) RETURN r", parameters("actorId", actorId, "movieId", movieId));
                        Map<String, Object> node = new HashMap<>();
                        node.put("result",hasRel.hasNext());
                        if(node.get("result").equals(true)){
                            Utils.sendString(request, "Relationship Exists Already", 400);
                            return;
                        }
                        StatementResult actorIdCheck = tx.run("MATCH (a: actor {actorId: $actorId}) RETURN a", parameters("actorId", actorId));
                        StatementResult movieIdCheck = tx.run("MATCH (m: movie {movieId: $movieId}) RETURN m", parameters("movieId", movieId));
                        if(!actorIdCheck.hasNext()){
                            Utils.sendString(request, "Actor not found!\n", 404);
                        }else if(!movieIdCheck.hasNext()){
                            Utils.sendString(request, "Movie not found!\n", 404);
                        }else{
                            tx.run("MATCH (a:actor {actorId: $actorId}), (m:movie {movieId: $movieId})" + "CREATE (a)-[:ACTED_IN]->(m)", parameters("actorId", actorId, "movieId", movieId));
                            tx.run("MATCH (m:movie {movieId: $movieId})" + "SET m.actors = m.actors + $actorId", parameters("actorId", actorId, "movieId", movieId));
                            tx.run("MATCH (a:actor {actorId: $actorId}) " + "SET a.movies = a.movies + $movieId", parameters("actorId", actorId, "movieId", movieId));
                            tx.success();
                            Utils.sendString(request, "Relationship Added Successfully", 200);
                        }

                    }

                }
            }
        }catch(Exception e){
            e.printStackTrace();
            String r = "Server Error: " + e.getMessage() + "\n";
            Utils.sendString(request, r, 500);
        }

    }

    public void handleGet(HttpExchange request) throws IOException{

            URI uri = request.getRequestURI();
            String path = uri.getPath();
            String query = uri.getQuery();
            Map<String, String> queryParam = Utils.splitQuery(query);


            if(path.contains("/api/v1/getActor")){
                getActor(queryParam, request);
            }else if (path.contains(("/api/v1/getMovie"))){
                getMovie(queryParam, request);
            }else if (path.contains("/api/v1/hasRelationship")) {
                getHasRelationship(queryParam, request);
            }else if (path.contains("/api/v1/computeBaconNumber")) {
                computeBaconNumber(queryParam, request);
            }else if (path.contains("/api/v1/computeBaconPath")) {
                computeBaconPath(queryParam, request);
            }else{
                Utils.sendString(request, "Bad request\n", 400);
            }


            driver.close();
    }


    private void getActor(Map<String, String> queryParam, HttpExchange request) throws IOException{

        try(Session session = driver.session()){

            if(!queryParam.containsKey("actorId") || queryParam.get("actorId").length()==0){
                Utils.sendString(request, "Bad request: Improper formatting.\n", 400);
            }else{
                try(Transaction tx = session.beginTransaction()){
                    String id = queryParam.get("actorId");
                    StatementResult result = tx.run("MATCH (a: actor {actorId: $actorId}) RETURN a", parameters("actorId", id));
                    if(result.hasNext()){
                        Map<String, Object> node = result.next().get("a").asMap();
                        JSONObject jsonNode = new JSONObject(node);
                        Utils.sendString(request, jsonNode.toString(), 200);
                    }else{
                        Utils.sendString(request, "Actor not found!\n", 404);
                    }

                }
            }


        }



    }

    private void getMovie(Map<String, String> queryParam, HttpExchange request) throws IOException{


        try(Session session = driver.session()){
            if(!queryParam.containsKey("movieId") || queryParam.get("movieId").length()==0){
                Utils.sendString(request, "Bad request: Improper Formatting\n", 400);
            }else{
                try(Transaction tx = session.beginTransaction()){
                    String id = queryParam.get("movieId");
                    StatementResult result = tx.run("MATCH (m: movie {movieId: $movieId}) RETURN m", parameters("movieId", id));
                    if(result.hasNext()){
                        Map<String, Object> node = result.next().get("m").asMap();
                        JSONObject jsonNode = new JSONObject(node);
                        Utils.sendString(request, jsonNode.toString(), 200);
                    }else{
                        Utils.sendString(request, "Movie not found!\n", 404);
                    }
                }
            }


        }
    }




    private void getHasRelationship(Map<String, String> queryParam, HttpExchange request) throws IOException {

        try(Session session = driver.session()){
            if(!queryParam.containsKey("actorId") || queryParam.get("actorId").length()==0 ||!queryParam.containsKey("movieId") || queryParam.get("movieId").length()==0){
                Utils.sendString(request, "Bad request: Improper Formatting\n", 400);
            }else{
                try(Transaction tx = session.beginTransaction()){
                    String actorId = queryParam.get("actorId");
                    String movieId = queryParam.get("movieId");
                    StatementResult result1 = tx.run("MATCH (a: actor {actorId: $actorId}) RETURN a.actorId", parameters("actorId", actorId));
                    StatementResult result2 = tx.run("MATCH (m: movie {movieId: $movieId}) RETURN m.movieId", parameters("movieId", movieId));
                    StatementResult result3 = tx.run( "MATCH (a:actor {actorId: $actorId})-[r:ACTED_IN]->(m:movie {movieId: $movieId}) RETURN r",
                            parameters("actorId", actorId, "movieId", movieId));


                    if(!result1.hasNext() && !result2.hasNext()) {
                        Utils.sendString(request, "Actor and Movie not found!\n", 404);
                    }else if(!result1.hasNext()){
                        Utils.sendString(request, "Actor not found!\n", 404);
                    }else if (!result2.hasNext()){
                        Utils.sendString(request, "Movie not found!\n", 404);
                    }else{
                        Map<String, Object> node = new HashMap<>();
                        node.put("actorId", result1.next().get("a.actorId").asString());
                        node.put("movieId", result2.next().get("m.movieId").asString());
                        node.put("hasRelationship", result3.hasNext());
                        JSONObject jsonNode = new JSONObject(node);
                        Utils.sendString(request, jsonNode.toString(), 200);
                    }

                }
            }


        }
    }


    private void computeBaconNumber(Map<String, String> queryParam, HttpExchange request) throws IOException {
        try (Session session = driver.session()) {
            if (!queryParam.containsKey("actorId") || queryParam.get("actorId").isEmpty()) {
                Utils.sendString(request, "Bad request: Improper formatting.\n", 400);
            } else {
                try (Transaction tx = session.beginTransaction()) {
                    String actorId = queryParam.get("actorId");
                    if(actorId.equals("nm0000102")){
                        JSONObject responseJson = new JSONObject();
                        responseJson.put("baconNumber", 0);
                        Utils.sendString(request, responseJson.toString(), 200);
                        return;
                    }
                    StatementResult check = tx.run("MATCH (a: actor {actorId: $actorId}) RETURN a", parameters("actorId", actorId));
                    if (check.hasNext()) {
                        String baconId = "nm0000102";
                        StatementResult result = tx.run("MATCH p = shortestPath((a: actor {actorId: $actorId})-[:ACTED_IN*]-(k: actor {actorId: $kevinBaconId})) RETURN length(p)/2 AS kbNum", parameters("actorId", actorId, "kevinBaconId", baconId));
                        if(result.hasNext()){
                            int baconNumber = result.next().get("kbNum").asInt();
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("baconNumber", baconNumber);
                            Utils.sendString(request, responseJson.toString(), 200);
                        }else{
                            Utils.sendString(request, "There is no Bacon path", 404);
                        }
                    } else {
                        Utils.sendString(request, "Actor not found!", 404);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String r = "Server Error: " + e.getMessage() + "\n";
                    Utils.sendString(request, r, 500);
                }
            }
        }
    }

    private void computeBaconPath(Map<String, String> queryParam, HttpExchange request) throws IOException {
        try (Session session = driver.session()) {
            if (!queryParam.containsKey("actorId") || queryParam.get("actorId").isEmpty()) {
                Utils.sendString(request, "Bad request: Improper formatting.\n", 400);
            } else {
                try (Transaction tx = session.beginTransaction()) {
                    String actorId = queryParam.get("actorId");
                    JSONObject responseJson = new JSONObject();
                    if(actorId.equals("nm0000102")){
                        JSONArray emptyPath = new JSONArray();
                        emptyPath.put(actorId);
                        responseJson.put("baconNumber", emptyPath);
                        Utils.sendString(request, responseJson.toString(), 200);
                        return;
                    }
                    StatementResult check = tx.run("MATCH (a: actor {actorId: $actorId}) RETURN a", parameters("actorId", actorId));
                    if (check.hasNext()) {
                        String baconId = "nm0000102";
                        StatementResult result = tx.run("MATCH path=shortestPath((a:actor {actorId: $actorId})-[:ACTED_IN*]-(k:actor {actorId: $kevinBaconId})) RETURN nodes(path) AS nodes", parameters("actorId", actorId, "kevinBaconId", baconId));
                        if(result.hasNext()){
                            JSONArray path = new JSONArray();
                            List<Object> list = result.single().get("nodes").asList();
                            for (Object node : list) {
                                if (node instanceof Node) {
                                    Node n = (Node)node;
                                    if (n.hasLabel("actor")) {
                                        path.put(n.get("actorId").asString());
                                    } else if (n.hasLabel("movie")) {
                                        path.put(n.get("movieId").asString());
                                    }
                                }
                            }
                            responseJson.put("baconPath", path);
                            Utils.sendString(request, responseJson.toString(), 200);
                        }else{
                            Utils.sendString(request, "There is no Bacon path", 404);
                        }
                    } else {
                        Utils.sendString(request, "Actor not found!", 404);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    String r = "Server Error: " + e.getMessage() + "\n";
                    Utils.sendString(request, r, 500);
                }
            }
        }
    }


}
