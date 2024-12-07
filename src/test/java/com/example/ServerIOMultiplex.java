package com.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

public class ServerIOMultiplex {
    public static void main(String[] args) throws IOException {
        if (args.length < 1 ) {
            throw new IllegalArgumentException("PORT not specified");
        }

        int port = Integer.parseInt(args[0]);

        Selector selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().setReuseAddress(true);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

        while (true) {
            selector.select();
            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

            while (selectionKeyIterator.hasNext()) {
                SelectionKey key = selectionKeyIterator.next();
                selectionKeyIterator.remove();
                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = serverChannel.accept();
                    clientChannel.configureBlocking(false);
                    key.attach(clientChannel);
                    clientChannel.register(key.selector(), SelectionKey.OP_WRITE);
                } else if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    readBuffer.clear();

                    clientChannel.read(readBuffer);
                    readBuffer.flip();
                    clientChannel.write(readBuffer);

                    clientChannel.register(key.selector(), SelectionKey.OP_READ);
                } else if (key.isWritable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    readBuffer.clear();

                    String message = "Hello from Server " + port + "\n";
                    readBuffer.put(message.getBytes());
                    readBuffer.flip();
                    clientChannel.write(readBuffer);

                    clientChannel.register(key.selector(), SelectionKey.OP_READ);
                }
            }
        }
    }
}
