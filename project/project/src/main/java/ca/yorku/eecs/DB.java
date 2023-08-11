package ca.yorku.eecs;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;

import static ca.yorku.eecs.Utils.splitQuery;
import static org.neo4j.driver.v1.Values.parameters;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

public class DB {

    private Driver driver;
    private String uriDb;

    public DB(){
        uriDb = "bolt://localhost:7687";
        Config config = Config.builder().withoutEncryption().build();
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j", "12345678"), config);
    }
    public void handleGet(HttpExchange request) throws IOException{

            URI uri = request.getRequestURI();
            String path = uri.getPath();
            String query = uri.getQuery();
            Map<String, String> queryParam = Utils.splitQuery(query);

            if(path.contains("getActor")){
                getActor(queryParam);
            }else if (path.contains(("getMovie"))){
                getMovie(queryParam);
            }else {
                getHasRelationship(queryParam);
            }
    }

    private void getActor(Map<String, String> queryParam) {

        String id = queryParam.get("actorId");
        try(Session session = driver.session()){
            session.writeTransaction(tx -> tx.run("MATCH (a: actor)"));
            session.close();
        }
    }

    private void getMovie(Map<String, String> queryParam){

        String id = queryParam.get("movieId");
    }

    private void getHasRelationship(Map<String, String> queryParam){
        String actorId = queryParam.get("actorId");
        String movieId = queryParam.get("movieId");
    }

    public void close(){
        this.driver.close();
    }
}
