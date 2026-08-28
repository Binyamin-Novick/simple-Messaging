package layer2Server.shndlr;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import layer2Server.Serverlogic;
import org.example.MacroDef;
import org.example.MacroDef.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class getGroups extends handler{
    public getGroups(Gson gson, HttpServer server, Serverlogic sl) {
        super(gson, server, http.getMyGroups, sl);
        this.requerdFields.add(http.Reqfield.token);
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
        List<String> groups=new ArrayList<>();
        List<Long>gids=new ArrayList<>();
        try {
            long[]res= sl.getGroups(body.get(http.Reqfield.token),groups,gids);
            if(res[0]== MacroDef.ok){
                HashMap<Long,String>gms=new HashMap<>();
                for(int i=0;i<gids.size();i++){
                    gms.put(gids.get(i),groups.get(i));
                }
                String gmsJ=gson.toJson(gms);
                exchange.sendResponseHeaders(200, gmsJ.length());
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(gmsJ.getBytes());
                    os.flush();
                }
            }else if(res[0]==MacroDef.fail){
                if(res[1]==MacroDef.invalidtoken){

                exchange.sendResponseHeaders(401, 0);
                exchange.close();}
                else exchange.sendResponseHeaders(200, 0);
            }else if(res[0]==MacroDef.timeout)exchange.sendResponseHeaders(408, 0);
            else {
                exchange.sendResponseHeaders(500, 0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
