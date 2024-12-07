package com.example.iterator;

import com.example.model.Server;

import java.util.List;

public abstract class ServerIterator {
    protected List<Server> servers;

    public ServerIterator(List<Server> servers) {
        this.servers = servers;
    }

    public abstract Server getNextServer();
}