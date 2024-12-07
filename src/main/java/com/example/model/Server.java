package com.example.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final String address;
    private final int port;
    private boolean isHealthy;
    private AtomicInteger numConnections;

    public Server(String address, int port) {
        this.address = address;
        this.port = port;
        this.isHealthy = true;
        this.numConnections = new AtomicInteger(0);
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isHealthy() {
        return isHealthy;
    }

    public boolean setIsHealthy(boolean value) {
        isHealthy = value;

        return isHealthy;
    }

    public int getConnections() {
        return numConnections.get();
    }

    public int incrementConnections() {
        return numConnections.incrementAndGet();
    }

    public int decrementConnections() {
        return numConnections.decrementAndGet();
    }
}