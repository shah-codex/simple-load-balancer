package com.example.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ReadCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private final ByteBuffer buffer;
    private final AsynchronousSocketChannel asynchronousSocketChannel;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public ReadCompletionHandler(ByteBuffer buffer, AsynchronousSocketChannel asynchronousSocketChannel) {
        this.buffer = buffer;
        this.asynchronousSocketChannel = asynchronousSocketChannel;
    }

    @Override
    public void completed(Integer bytesRead, AsynchronousSocketChannel backendServerChannel) {
        if (bytesRead == -1) {
            closeConnection(asynchronousSocketChannel);
//            closeConnection(backendServerChannel);
        }

        buffer.flip();

        backendServerChannel.write(buffer.duplicate());
        buffer.clear();

        asynchronousSocketChannel.read(buffer, backendServerChannel, this);
    }

    @Override
    public void failed(Throwable throwable, AsynchronousSocketChannel backendServerChannel) {
        logger.log(Level.SEVERE, "Failed to read from clientChannel");
        closeConnection(asynchronousSocketChannel);
    }

    private void closeConnection(AsynchronousSocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to terminate the connection");
        }
    }
}
