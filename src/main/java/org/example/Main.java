package org.example;

import layer2Server.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {

        System.out.println(InetAddress.getLocalHost().getHostName());

        server s=new server(10l);
    }
}
