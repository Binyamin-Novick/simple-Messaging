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

public class changPassword extends handler{
    public changPassword(Gson gson, HttpServer server,  Serverlogic sl) {
        super(gson, server, http.chngpswrd, sl);
        requerdFields.add(http.Reqfield.username);
        requerdFields.add(http.Reqfield.oldPassword);
        requerdFields.add(http.Reqfield.newPassword);
        requerdFields.add(http.Reqfield.token);

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        boolean[] good=new boolean[1];
        HashMap<String,String>body=getGson(exchange,good);
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
            long[]res= sl.ChangPassword(body.get(http.Reqfield.token),body.get(http.Reqfield.oldPassword),
                    body.get(http.Reqfield.groupName),http.Reqfield.newPassword);

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
