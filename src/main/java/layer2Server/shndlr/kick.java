package layer2Server.shndlr;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import layer2Server.Serverlogic;
import org.example.MacroDef;
import org.example.MacroDef.http;
import org.example.MacroDef.http.Reqfield;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class kick extends handler{
    public kick(Gson gson, HttpServer server,Serverlogic sl) {
        super(gson, server, http.removeUserFromGroup, sl);
        this.requerdFields.add(Reqfield.token);
        this.requerdFields.add(Reqfield.gid);
        this.requerdFields.add(Reqfield.uid);
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
            long[]res= sl.removeUserFromGroup(body.get(Reqfield.token),
                    Long.parseLong(body.get(Reqfield.gid)), Long.parseLong(body.get(Reqfield.uid)));

            if(res[0]== MacroDef.ok){
                exchange.sendResponseHeaders(200, 0);
            }else if(res[0]==MacroDef.fail){
                if(res[1]==MacroDef.invalidtoken)exchange.sendResponseHeaders(401, 0);
                else if(res[1]==MacroDef.permssionsFail)exchange.sendResponseHeaders(403, 0);
                else exchange.sendResponseHeaders(400, 0);
            }else if(res[0]==MacroDef.timeout)exchange.sendResponseHeaders(408, 0);
            else exchange.sendResponseHeaders(500, 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
