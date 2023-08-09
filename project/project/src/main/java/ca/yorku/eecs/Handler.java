package ca.yorku.eecs;
import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class Handler implements HttpHandler{


    @Override
    public void handle(HttpExchange request) throws IOException {

        try {
            if (request.getRequestMethod().equals("GET")) {

            }else{
                Utils.sendString(request, "Unimplemented method\n", 501 );
            }
        }catch (Exception e){
            e.printStackTrace();
            Utils.sendString(request, "Server error\n", 500);
        }
    }
}
