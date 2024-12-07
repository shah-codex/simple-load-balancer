package com.example.handler;

import com.example.iterator.ServerIterator;
import com.example.model.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {
    private final AsynchronousServerSocketChannel serverSocketChannel;
    private final ServerIterator serverIterator;

    public AcceptCompletionHandler(AsynchronousServerSocketChannel serverSocketChannel, ServerIterator serverIterator) {
        this.serverSocketChannel = serverSocketChannel;
        this.serverIterator = serverIterator;
    }

    @Override
    public void completed(AsynchronousSocketChannel asynchronousSocketChannel, Void unused) {
        serverSocketChannel.accept(null, this);
        Server server = serverIterator.getNextServer();
        ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);
        ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024);
        try {
            AsynchronousSocketChannel backendSocketChannel = AsynchronousSocketChannel.open();
            backendSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            backendSocketChannel.connect(new InetSocketAddress(server.getAddress(), server.getPort()), asynchronousSocketChannel,
                    new CompletionHandler<Void, AsynchronousSocketChannel>() {
                        @Override
                        public void completed(Void unused, AsynchronousSocketChannel connectedClientChannel) {
                            ReadCompletionHandler readCompletionHandler
                                    = new ReadCompletionHandler(readBuffer, asynchronousSocketChannel);
                            asynchronousSocketChannel.read(readBuffer, backendSocketChannel, readCompletionHandler);

                            WriteCompletionHandler writeCompletionHandler
                                    = new WriteCompletionHandler(writeBuffer, asynchronousSocketChannel);
                            asynchronousSocketChannel.write(writeBuffer, backendSocketChannel, writeCompletionHandler);
                        }

                        @Override
                        public void failed(Throwable throwable, AsynchronousSocketChannel connectedClientChannel) {
                            Logger logger = Logger.getAnonymousLogger();
                            logger.log(Level.WARNING, "Failed to connect to backend server.");
                            try {
                                connectedClientChannel.close();
                            } catch (IOException e) {
                                logger.log(Level.WARNING, "Failed to disconnect the client");
                            }
                        }
                    });
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Failed to open Backend Socket Channel");
        }
    }

    @Override
    public void failed(Throwable throwable, Void unused) {
        Logger.getAnonymousLogger().log(Level.SEVERE, "Failed to start the server");
    }
}
