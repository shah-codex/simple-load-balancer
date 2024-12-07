package com.example;

import com.example.handler.AcceptCompletionHandler;
import com.example.iterator.ServerIterator;
import com.example.model.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

public class LoadBalancer {
    private ServerIterator serverIterator;
    private final List<Server> servers;

    private AsynchronousServerSocketChannel listener;

    public LoadBalancer(ServerIterator serverIterator) {
        this.serverIterator = serverIterator;
        this.servers = Collections.synchronizedList(new ArrayList<>());
    }

    public void init(int port) throws IOException {
        listener = AsynchronousChannelProvider.provider().openAsynchronousServerSocketChannel(
                AsynchronousChannelGroup.withThreadPool(
                        Executors.newFixedThreadPool(2)
                )
        );

        listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        listener.bind(new InetSocketAddress(port));
    }

    public void start() throws IOException {
        AcceptCompletionHandler acceptCompletionHandler = new AcceptCompletionHandler(listener, serverIterator);
        listener.accept(null, acceptCompletionHandler);

        System.in.read();
    }

    public boolean addServerToPool(Server server) {
        return servers.add(server);
    }

    public boolean removeServerFromPool(int index) {
        try {
            servers.remove(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }

        return true;
    }

    public void updateServerIterator(ServerIterator iterator) {
        this.serverIterator = iterator;
    }
}
