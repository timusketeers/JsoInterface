package com.howbuy.jso.service.network.receiver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 消息接收者
 * 
 * @author li.zhang 2014-9-18
 * 
 */
public class ResponseReceiver
{
    private Selector selector;

    public ResponseReceiver()
    {
        init();
    }

    private void init()
    {
        try
        {
            selector = Selector.open();

            int port = 9090;
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ServerSocket ss = serverChannel.socket();
            InetSocketAddress address = new InetSocketAddress(port);
            ss.bind(address);
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        new RespReceiveThread(selector).start();
    }
    
    public static void main(String[] args)
    {
        new ResponseReceiver();
        while (true);
    }
}


/**
 * 响应接收线程.
 * 
 * @author li.zhang 2014-9-18
 * 
 */
class RespReceiveThread extends Thread
{
    private Selector selector;

    /**
     * 构造方法
     * 
     * @param selector
     */
    RespReceiveThread(Selector selector)
    {
        this.selector = selector;
    }

    public void run()
    {
        while (true)
        {
            try
            {
                if (selector.select() <= 0)
                {
                    continue;
                }
                
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext())
                {
                    SelectionKey key = iter.next();
                    iter.remove();
                    
                    if (key.isAcceptable())
                    {
                        ServerSocketChannel server = (ServerSocketChannel)key.channel();
                        SocketChannel client = server.accept();
                        System.out.println("Accept connection from " + client);
                        client.configureBlocking(false);
                        
                        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                    }
                    //这里是服务端正式向socket写数据..
                    else if (key.isWritable())
                    {
                        SocketChannel client = (SocketChannel)key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1000);
                        
                        buffer.put("server say hello world".getBytes());
                        buffer.flip();
                        client.write(buffer);
                    }
                    else if (key.isReadable())
                    {
                        SocketChannel client = (SocketChannel)key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        while ((client.read(buffer)) != 0);
                        buffer.flip();
                        byte[] dst = new byte[buffer.limit()];
                        buffer.get(dst);
                        
                        System.out.println(new String(dst));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
