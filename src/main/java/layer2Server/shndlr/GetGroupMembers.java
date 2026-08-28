package layer2Server.shndlr;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.MacroDef;
import org.example.MacroDef.*;
import layer2Server.Serverlogic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GetGroupMembers extends handler{

    public GetGroupMembers(Gson gson, HttpServer server , Serverlogic sl) {
        super(gson, server, http.getGroupMembers, sl);
        this.requerdFields.add(http.Reqfield.token);
        this.requerdFields.add(http.Reqfield.gid);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
       boolean[] good=new boolean[1];
        HashMap<String,String>body = getGson(exchange,good);
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
        List<Long>mid=new ArrayList<>();
        List<String>usernames=new ArrayList<>();
        try {
            long[]res= sl.getMyGusers(body.get(http.Reqfield.token), Long.valueOf(body.get(http.Reqfield.gid)),mid,usernames);
            if(res[0]==MacroDef.ok){
                HashMap<Long,String>gms=new HashMap<>();
                for(int i=0;i<mid.size();i++){
                    gms.put(mid.get(i),usernames.get(i));
                }
                String gmsJ=gson.toJson(gms);
                exchange.sendResponseHeaders(200, gmsJ.length());
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(gmsJ.getBytes());
                    os.flush();
                    os.close();
                    return;
                }catch (Exception e){

                    throw new RuntimeException(e);
                }

            }else if(res[0]==MacroDef.fail){
                if(res[1]==MacroDef.invalidtoken)exchange.sendResponseHeaders(401, 0);
                else exchange.sendResponseHeaders(200, 0);
            }else if(res[0]==MacroDef.timeout)exchange.sendResponseHeaders(408, 0);
            else exchange.sendResponseHeaders(500, 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
}
