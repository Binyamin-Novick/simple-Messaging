package layer2Server.shndlr;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import layer2Server.Serverlogic;
import layer2Server.PosgressCumuncater.Messege;
import org.example.MacroDef;
import org.example.MacroDef.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;

import static org.example.MacroDef.invalidtoken;

public class getMessges extends handler{
    public getMessges(Gson gson, HttpServer server, Serverlogic sl) {
        super(gson, server, http.getMesseges, sl);
        this.requerdFields.add(http.Reqfield.token);
        this.requerdFields.add(http.Reqfield.timestamp);
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

        final Timestamp timestamp;
        try {
            timestamp = Timestamp.valueOf(body.get(http.Reqfield.timestamp));
        } catch (IllegalArgumentException e) {
            String response = "Bad Request";
            exchange.sendResponseHeaders(400, response.length());
            try (java.io.OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        long[] feedback = new long[2];
        try {
            Messege message = sl.Getmessge(body.get(http.Reqfield.token), timestamp, feedback);
            if (feedback[0] == MacroDef.ok) {
                String response = gson.toJson(message);
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (java.io.OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }
            if (feedback[1] == invalidtoken) {
                exchange.sendResponseHeaders(401, -1);
            } else {
                exchange.sendResponseHeaders(500, -1);
            }
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, -1);
        } finally {
            exchange.close();
        }
    }
}
