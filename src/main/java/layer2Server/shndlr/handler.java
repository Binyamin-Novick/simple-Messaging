package layer2Server.shndlr;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import layer2Server.Serverlogic;
import org.example.MacroDef;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import static org.example.MacroDef.http.Reqfield.*;

public abstract class handler implements HttpHandler {
    Gson gson;
    public List<String> requerdFields = new ArrayList<>();


    Serverlogic sl;
    public handler(Gson gson, HttpServer server, String path, Serverlogic sl){
        server.createContext(path,this);
        this.gson=gson;
        this.sl=sl;
    }
    public handler(Gson gson, HttpsServer server, String path, Serverlogic sl){
        server.createContext(path,this);
        this.gson=gson;
        this.sl=sl;
    }

    public HashMap<String,String> getGson(HttpExchange exchange,boolean[] good) {
        good[0]=true;
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        try {


        HashMap<String,String>body =gson.fromJson(new String( exchange.getRequestBody().readAllBytes()), new TypeToken<HashMap<String,String>>(){}.getType());
        good[0]=true;
        for(String field:requerdFields){
            if(!isFieldValid(body,field))good[0]=false;
        }
        return body;
        }catch (Exception e){
            good[0]=false;
            return null;
        }
    }


    public boolean isFieldValid(HashMap<String,String> body,String field){
        if(!body.containsKey(field))return false;
        if( body.get(field) == null)return false ;
        try {


            switch (requerdFields.get(requerdFields.indexOf(field))) {
                case messege:
                    return body.get(field).length() > 0;
                case groupName:
                    return body.get(field).length() > 0;


                case username:
                    return body.get(field).length() > 0;
                case password:
                    return body.get(field).length() > 0;
                case newPassword:
                    return body.get(field).length() > 0;
                case token:
                    return body.get(field).length() > 0;
                case timestamp:
                    if (body.get(field).length() == 0) return false;
                    Timestamp.valueOf(body.get(field));
                    return true;
                case gid:
                    if (body.get(field).length() > 0)
                        return Long.parseLong(body.get(field)) > 0;
                    return false;
                case oldPassword:
                    return body.get(field).length() > 0;
                case uid:
                    if (body.get(field).length() > 0)
                        return Long.parseLong(body.get(field)) > 0;
                    return false;
                case perm:
                    if (body.get(field).length() > 0)
                        if(Long.parseLong(body.get(field)) > 0)
                            return Long.parseLong(body.get(field))<=MacroDef.admin;
                    return false;
                default:
                    return true;
            }
        }catch (Exception e){
            return false;
        }
    }
}
