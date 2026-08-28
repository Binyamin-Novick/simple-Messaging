package layer2Server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import layer2Server.shndlr.*;
import org.example.MacroDef;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    private static final int PORT = 8080;

    private final HttpServer httpServer;
    private final ExecutorService executor;

    public server(Long connectionCount) throws IOException, SQLException {
        if (connectionCount == null || connectionCount < 1 || connectionCount > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Connection count must be between 1 and " + Integer.MAX_VALUE);
        }

        int threadCount = connectionCount.intValue();
        Serverlogic serverlogic = new Serverlogic(connectionCount);
        Gson gson = new Gson();

        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
        executor = Executors.newFixedThreadPool(threadCount);
        httpServer.setExecutor(executor);

        new login(gson, httpServer, serverlogic);
        new signeUP(gson, httpServer, serverlogic);
        new sendMessege(gson, httpServer, serverlogic);
        new getMessges(gson, httpServer, serverlogic);
        new makegroup(gson, httpServer, MacroDef.http.createGroup, serverlogic);
        new addmember(gson, httpServer, MacroDef.http.addUserToGroup, serverlogic);
        new kick(gson, httpServer, serverlogic);
        new leaveGroup(gson, httpServer, MacroDef.http.leaveGroup, serverlogic);
        new deleteGroup(gson, httpServer, serverlogic);
        new changPerms(gson, httpServer, MacroDef.http.chngPermissions, serverlogic);
        new changPassword(gson, httpServer, serverlogic);
        new getGroups(gson, httpServer, serverlogic);
        new GetGroupMembers(gson, httpServer, serverlogic);
        new getUId(gson, httpServer, serverlogic);
        new delUser(gson, httpServer, serverlogic);

        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
        executor.shutdown();
    }
}
