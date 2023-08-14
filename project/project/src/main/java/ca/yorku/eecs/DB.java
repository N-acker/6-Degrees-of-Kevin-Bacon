package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static ca.yorku.eecs.Utils.splitQuery;
import static org.neo4j.driver.v1.Values.parameters;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.*;

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
            if (path.equals("/api/v1/addActor")) {
                String requestBody = Utils.getBody(request);
                JSONObject json = new JSONObject(requestBody);
                String name = json.getString("name");
                String actorId = json.getString("actorId");
                addActor(name, actorId);
                Utils.sendString(request, "Actor added successfully\n", 200);
            } else if (path.equals("/api/v1/addMovie")) {
                String requestBody = Utils.getBody(request);
                JSONObject json = new JSONObject(requestBody);
                String name = json.getString("name");
                String movieId = json.getString("movieId");
                addMovie(name, movieId);
                Utils.sendString(request, "Movie added successfully\n", 200);
            } else if (path.equals("/api/v1/addRelationship")) {
                String requestBody = Utils.getBody(request);
                JSONObject json = new JSONObject(requestBody);
                String actorId = json.getString("actorId");
                String movieId = json.getString("movieId");
                addRelationship(actorId, movieId);
                Utils.sendString(request, "Relationship added successfully\n", 200);
            } else {
                Utils.sendString(request, "Bad request\n", 400);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.sendString(request, "Bad request\n", 400);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendString(request, "Server error\n", 500);
        }
    }


    private void addActor(String name, String actorId) {
        try (Session session = driver.session()) {
            String query = "CREATE (a:Actor {name: $name, actorId: $actorId})";
            session.writeTransaction(tx -> tx.run(query, parameters("name", name, "actorId", actorId)));
        }
    }

    private void addMovie(String name, String movieId) {
        try (Session session = driver.session()) {
            String query = "CREATE (m:Movie {name: $name, movieId: $movieId})";
            session.writeTransaction(tx -> tx.run(query, parameters("name", name, "movieId", movieId)));
        }
    }

    private void addRelationship(String actorId, String movieId) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:Actor {actorId: $actorId}), (m:Movie {movieId: $movieId}) " +
                    "CREATE (a)-[:ACTED_IN]->(m)";
            session.writeTransaction(tx -> tx.run(query, parameters("actorId", actorId, "movieId", movieId)));
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
                getcomputeBaconNumber(queryParam, request);
            }else if (path.contains("/api/v1/computeBaconPath")) {
                getcomputeBaconPath(queryParam, request);
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
                    StatementResult result3 = tx.run( "MATCH (a:Actor {actorId: $actorId})-[r:ACTED_IN]->(m:Movie {movieId: $movieId}) RETURN r",
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
                        node.put("hasRelationship:", result3.hasNext());
                        JSONObject jsonNode = new JSONObject(node);
                        Utils.sendString(request, jsonNode.toString(), 200);
                    }

                }
            }


        }
    }



    private void getcomputeBaconPath(Map<String, String> queryParam, HttpExchange request) {
        try(Session session = driver.session()){
            if(!queryParam.containsKey("actorId") || queryParam.get("actorId").length()==0){
                Utils.sendString(request, "Bad request: Improper formatting.\n", 400);
            }else {
                try (Transaction tx = session.beginTransaction()) {
                    String actorId = queryParam.get("actorId");
                    StatementResult result = tx.run("MATCH p=shortestPath(\n" +
                            "    (a:actor{actorId:\"$actorId\"})-[*]-(b:actor{actorId:\"nm0000102\"})\n" +
                            ")\n" +
                            "RETURN [node IN nodes(p) | node.actorId] AS BaconPath", parameters("actorId", actorId));
                    if (result.hasNext()) {
                        Map<String, Object> node = result.next().get("[node IN nodes(p) | node.actorId]").asMap();
                        node.put("baconPath:", result);
                        JSONObject jsonNode = new JSONObject(node);
                        Utils.sendString(request, jsonNode.toString(), 200);
                    } else {
                        Utils.sendString(request, "Actor not found!", 404);
                    }
                }
            }
        }
    }

    private void getcomputeBaconNumber(Map<String, String> queryParam, HttpExchange request) {
        try(Session session = driver.session()){
            if(!queryParam.containsKey("actorId") || queryParam.get("actorId").length()==0){
                Utils.sendString(request, "Bad request: Improper formatting.\n", 400);
            }else {
                try (Transaction tx = session.beginTransaction()) {
                    String actorId = queryParam.get("actorId");
                    StatementResult result = tx.run("MATCH(a:actor{actorId:\"$actorId\"})-[:ACTED_IN]->(m:movie)<-[:ACTED_IN]-(q:actor{actorId:\"nm0000102\"})\n" +
                            "RETURN count(r);", parameters("actorId", actorId));
                    if (result.hasNext()) {
                        Map<String, Object> node = result.next().get("count(r)").asMap();
                        node.put("baconNumber:", result);
                        JSONObject jsonNode = new JSONObject(node);
                        Utils.sendString(request, jsonNode.toString(), 200);
                    } else {
                        Utils.sendString(request, "Actor not found!", 404);
                    }
                }
            }
        }
    }

}
