package layer2Server.shndlr;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import layer2Server.PosgressCumuncater.*;
import layer2Server.Serverlogic;
import org.example.MacroDef;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;

public class sendMessege extends handler{
    public sendMessege(Gson gson, HttpServer server,  Serverlogic sl) {
        super(gson, server, MacroDef.http.sendMessege, sl);
        this.requerdFields.add(MacroDef.http.Reqfield.token);
        this.requerdFields.add(MacroDef.http.Reqfield.messege);
        this.requerdFields.add(MacroDef.http.Reqfield.gid);
        this.requerdFields.add(MacroDef.http.Reqfield.username);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("messege sent");
        boolean[] good=new boolean[1];
        HashMap<String,String> body =super.getGson(exchange,good);
        if(!good[0]){
            String response = "Bad Request";
            exchange.sendResponseHeaders(400, response.length());
            try (java.io.OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
                os.flush();
                os.close();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        Messege m = new Messege(body.get(MacroDef.http.Reqfield.messege),
                Timestamp.valueOf(LocalDateTime.now()),body.get(MacroDef.http.Reqfield.gid)
                ,body.get(MacroDef.http.Reqfield.username));

        try {
            System.out.println("messege sent a");
            long []res= sl.insertMessage(body.get(MacroDef.http.Reqfield.token),m);
            System.out.println("messege sent b");
            if(res[0]==MacroDef.ok){
                exchange.sendResponseHeaders(200, 0);

                System.out.println("messege sent c");
                exchange.getResponseBody().close();

            }if(res[0]==MacroDef.fail){
                if(res[1]==MacroDef.invalidtoken)exchange.sendResponseHeaders(401, 0);
                else if(res[1]==MacroDef.permssionsFail)exchange.sendResponseHeaders(403, 0);
                else exchange.sendResponseHeaders(400, 0);
            }
            if(res[0]==MacroDef.timeout)exchange.sendResponseHeaders(408, 0);
            else exchange.sendResponseHeaders(500, 0);
            exchange.getResponseBody().close();
        } catch (SQLException e) {
            System.out.println("messege sent error");
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
