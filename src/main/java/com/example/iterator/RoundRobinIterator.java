package com.example.iterator;

import com.example.model.Server;

import java.util.List;

public class RoundRobinIterator extends ServerIterator {
    private int currentServer;

    public RoundRobinIterator(List<Server> servers) {
        super(servers);
        this.currentServer = 0;
    }

    private Server getNextServerRoundRobbin() {
        currentServer = (currentServer + 1) % servers.size();
        return servers.get(currentServer);
    }

    @Override
    public Server getNextServer() {
        return getNextServerRoundRobbin();
    }
}
