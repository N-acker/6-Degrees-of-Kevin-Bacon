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
    public void handleGet(HttpExchange request) throws IOException, JSONException {

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

    private void getActor(Map<String, String> queryParam, HttpExchange request) throws IOException, JSONException {

        String id = queryParam.get("actorId");
        try(Session session = driver.session()){
            try(Transaction tx = session.beginTransaction()){
                StatementResult result = tx.run("MATCH (a: actor {actorId: $actorId}) RETURN a", parameters("actorId", id));
                if(result.hasNext()){
                    Map<String, Object> node = result.next().get("a").asMap();
                    JSONObject jsonNode = new JSONObject(node);
                    Utils.sendString(request, jsonNode.toString(4), 200);
                }else{
                    Utils.sendString(request, "Actor not found!", 404);
                }

            }

        }



    }

    private void getMovie(Map<String, String> queryParam, HttpExchange request) throws IOException, JSONException {

        String id = queryParam.get("movieId");
        try(Session session = driver.session()){
            try(Transaction tx = session.beginTransaction()){
                StatementResult result = tx.run("MATCH (m: movie {movieId: $movieId}) RETURN m", parameters("movieId", id));
                if(result.hasNext()){
                    Map<String, Object> node = result.next().get("m").asMap();
                    JSONObject jsonNode = new JSONObject(node);
                    Utils.sendString(request, jsonNode.toString(4), 200);
                }else{
                    Utils.sendString(request, "Actor not found!", 404);
                }
            }

        }
    }

    private void getHasRelationship(Map<String, String> queryParam, HttpExchange request) throws IOException, JSONException {
        String actorId = queryParam.get("actorId");
        String movieId = queryParam.get("movieId");
        try(Session session = driver.session()){
            try(Transaction tx = session.beginTransaction()){
                StatementResult result1 = tx.run("MATCH (a: actor {actorId: $actorId}) RETURN a.actorId", parameters("actorId", actorId));
                StatementResult result2 = tx.run("MATCH (m: movie {movieId: $movieId}) RETURN m.movieId", parameters("movieId", movieId));
                StatementResult result3 = tx.run("RETURN EXISTS ( (a: actor {actorId: $actorId})-[:ACTED_IN*1]-(m: movie {name: $movieId})) AS bool",
                        parameters("actorId", actorId, "movieId", movieId ));

                if(!result1.hasNext()){
                    Utils.sendString(request, "Actor not found!", 404);
                }else if (!result2.hasNext()){
                    Utils.sendString(request, "Movie not found!", 404);
                }else{
                    Map<String, Object> node = new HashMap<>();
                    node.put("actorId:", result1);
                    node.put("movieId:", result2);
                    node.put("hasRelationship:", result3);
                    JSONObject jsonNode = new JSONObject(node);
                    Utils.sendString(request, jsonNode.toString(4), 200);
                }

            }

        }
    }

}
