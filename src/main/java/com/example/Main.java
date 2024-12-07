package com.example;

import com.example.iterator.RoundRobinIterator;
import com.example.iterator.ServerIterator;
import com.example.model.Server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger("Main");

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.log(Level.WARNING, "PORT not specified.");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);

        List<Server> servers = new ArrayList<>();
        servers.add(new Server("localhost", 9001));
        servers.add(new Server("localhost", 9002));

        ServerIterator iterator = new RoundRobinIterator(servers);

        LoadBalancer loadBalancer = new LoadBalancer(iterator);
        try {
            loadBalancer.init(port);
            loadBalancer.start();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to start the server!!");
        }
    }
}