package layer2Server.shndlr;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import layer2Server.Serverlogic;
import org.example.MacroDef;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class signeUP extends handler{
    public signeUP(Gson gson, HttpServer server, Serverlogic sl) {
        super(gson, server, MacroDef.http.register, sl);
        this.requerdFields.add("username");
        this.requerdFields.add("password");
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
            }catch (Exception e){}
            return;
        }
        String []token =new String[1];
        try {
           long[]res= sl.nwUser(body.get(MacroDef.http.Reqfield.username),body.get(MacroDef.http.Reqfield.password),token);
           if(res[0]==MacroDef.ok){
               exchange.sendResponseHeaders(200, token[0].length());
               try (java.io.OutputStream os = exchange.getResponseBody()) {
                   os.write(token[0].getBytes());
                   os.flush();
                   os.close();
               }
           }if(res[0]==MacroDef.fail){
               exchange.sendResponseHeaders(409, 0);
               exchange.close();

            }
            else if(res[0]==MacroDef.timeout)exchange.sendResponseHeaders(408, 0);
            else exchange.sendResponseHeaders(500, 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
