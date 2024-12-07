package com.example.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteCompletionHandler implements CompletionHandler<Integer, AsynchronousSocketChannel> {
    private final ByteBuffer buffer;
    private final AsynchronousSocketChannel asynchronousSocketChannel;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public WriteCompletionHandler(ByteBuffer buffer, AsynchronousSocketChannel asynchronousSocketChannel) {
        this.buffer = buffer;
        this.asynchronousSocketChannel = asynchronousSocketChannel;
    }

    @Override
    public void completed(Integer integer, AsynchronousSocketChannel backendServerChannel) {
        buffer.clear();
        backendServerChannel.read(buffer, this, new CompletionHandler<Integer, WriteCompletionHandler>() {
            @Override
            public void completed(Integer bytesRead, WriteCompletionHandler writeCompletionHandler) {
                if (bytesRead == -1) {
                    closeConnection(asynchronousSocketChannel);
//                    closeConnection(backendServerChannel);
                }

                buffer.flip();
                asynchronousSocketChannel.write(buffer, backendServerChannel, writeCompletionHandler);
            }

            @Override
            public void failed(Throwable throwable, WriteCompletionHandler writeCompletionHandler) {
                logger.log(Level.SEVERE, "Failed to write to client Channel");
                closeConnection(asynchronousSocketChannel);
            }
        });
    }

    @Override
    public void failed(Throwable throwable, AsynchronousSocketChannel backendServerChannel) {
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
