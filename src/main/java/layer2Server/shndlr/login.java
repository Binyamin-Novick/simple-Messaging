package layer2Server.shndlr;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import layer2Server.Serverlogic;
import org.example.MacroDef;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;

public class login extends handler{
    public login(Gson gson, HttpServer server, Serverlogic sl) {
        super(gson, server, MacroDef.http.login, sl);
        this.requerdFields.add("username");
        this.requerdFields.add("password");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException  {
        boolean[] good=new boolean[1];
        HashMap<String,String>body =super.getGson(exchange,good);
         if(!good[0]){
             String response = "Bad Request";
             exchange.sendResponseHeaders(400, response.length());
             try (OutputStream os = exchange.getResponseBody()) {
                 os.write(response.getBytes(StandardCharsets.UTF_8));
                 os.flush();
                 os.close();
             }catch (Exception e){}

             return;

         }else {
             String[]token =new String[1];
             try {
                 long[]out =sl.login(body.get(MacroDef.http.Reqfield.username),
                         body.get(MacroDef.http.Reqfield.password),token);
                 if(out[0]==MacroDef.ok){

                     exchange.sendResponseHeaders(200, token[0].length());
                     try (OutputStream os = exchange.getResponseBody()) {
                         os.write(token[0].getBytes(StandardCharsets.UTF_8));
                         os.flush();
                         os.close();
                     }
                 }if(out[0]==MacroDef.fail) {
                     exchange.sendResponseHeaders(401, 0);
                     exchange.close();
                 }else if(out[0]==MacroDef.timeout)exchange.sendResponseHeaders(408, 0);
                 else exchange.sendResponseHeaders(500, 0);
                 exchange.close();


             } catch (SQLException e) {
                 throw new RuntimeException(e);
             }
         }
    }
}
