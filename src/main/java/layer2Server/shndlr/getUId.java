package layer2Server.shndlr;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import layer2Server.Serverlogic;
import org.example.MacroDef;
import org.example.MacroDef.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class getUId extends handler{

    public getUId(Gson gson, HttpServer server, Serverlogic sl) {
        super(gson, server, http.getUsers, sl);
        this.requerdFields.add(http.Reqfield.username);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        boolean[] good=new boolean[1];
        HashMap<String,String> body =super.getGson(exchange,good);
        if(!good[0]){
            String response = "Bad Request";
            exchange.sendResponseHeaders(400, response.length());
            try (java.io.OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
                os.flush();
                os.close();
                return;
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        try {
            long[]res=sl.getUID(body.get(http.Reqfield.username));
            if(res[0]== MacroDef.ok){
                String value = String.valueOf(res[1]);
                exchange.sendResponseHeaders(200, value.length());
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(value.getBytes());
                    os.flush();
                    os.close();
                }
            }else if(res[0]==MacroDef.fail){
                exchange.sendResponseHeaders(200, 0);

            }else if(res[0]==MacroDef.timeout)exchange.sendResponseHeaders(408, 0);
            else exchange.sendResponseHeaders(500, 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
