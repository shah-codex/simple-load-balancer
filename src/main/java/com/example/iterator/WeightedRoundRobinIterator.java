package com.example.iterator;

import com.example.model.Server;

import java.util.List;

public class WeightedRoundRobinIterator extends ServerIterator {
    private final List<Double> weights;
    private int currentRequests;
    private int currentServer;

    public WeightedRoundRobinIterator(List<Server> servers, List<Double> weights) {
        super(servers);
        double totalSum = weights.stream()
                                 .mapToDouble(w -> w)
                                 .sum();

        if (totalSum != 1) {
            throw new IllegalArgumentException("Sum of weights should be equal to 1");
        }

        this.weights = weights;
        this.currentServer = 0;
        this.currentRequests = 0;
    }

    @Override
    public Server getNextServer() {
        if (currentRequests >= weights.get(currentServer) * 10) {
            currentRequests = 0;
            currentServer = (currentServer + 1) % servers.size();
        }

        currentRequests++;
        Server server = servers.get(currentServer);
        server.incrementConnections();

        return server;
    }
}
