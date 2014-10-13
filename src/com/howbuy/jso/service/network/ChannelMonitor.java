package com.howbuy.jso.service.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.howbuy.jso.service.CmdEnum;
import com.howbuy.jso.service.network.attachment.Attachment;
import com.howbuy.jso.service.network.host.Host;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.network.protocol.CmdResp;
import com.howbuy.jso.service.network.thread.HeartbeatThread;
import com.howbuy.jso.service.queue.NioQueue;

/**
 * 渠道监控线程.
 * @author li.zhang
 * 2014-9-23
 *
 */
public class ChannelMonitor extends Thread
{
    /** 渠道选择器. **/
    private final Selector selector;
    
    /** 主机选择器. **/
    private final HostSelector hostSelector;
    
    /** 试连次数. **/
    private int tryConnectCount;
    
    /**
     * 构造方法
     * @param selector
     */
    public ChannelMonitor(HostSelector selector)
    {
        this.hostSelector = selector;
        this.selector = selector.getSelector();
    }
    
    @Override
    public void run()
    {
        while (true)
        {
                int selectedCount = 0;
                
                try
                {
                    selectedCount = selector.selectNow();
                }
                catch (IOException e)
                {
                    continue;
                }
                
                //非阻塞.
                if (0 >= selectedCount)
                {
                    continue;
                }
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext())
                {
                    SelectionKey key = iterator.next();
                    
                    //连接就绪
                    if (key.isValid() && key.isConnectable())
                    {
                        doConnect(key);
                    }
                    
                    //渠道读就绪.
                    if (key.isValid() && key.isReadable())
                    {
                        doRead(key);
                    }
                    
                    //渠道写就绪.
                    if (key.isValid() && key.isWritable())
                    {
                        doWrite(key);
                    }
                }
                
                selectedKeys.clear();
        }
    }

    //连接就绪
    private void doConnect(SelectionKey key)
    {
        System.out.println("doConnect...");
        SocketChannel channel = (SocketChannel)key.channel();
        
        try
        {
            boolean connected = channel.finishConnect();
            
            if (connected)
            {
                //重连成功后, 从撤销列表中删除.
                removeFromCancelledSet(key);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            
            closeChannel(channel);
            return;
        }
        
        //关联附件
        key.attach(new Attachment());
        
        //启动心跳服务线程.
        HeartbeatThread hearbeat = new HeartbeatThread(key);
        hearbeat.start();
        
        //如果试连次数大于或者等于主机个数,则说明链路初始化已经完成.
        ++tryConnectCount;
        
        //一个渠道channel和一个选择器selector只会有一个注册关系,即只有一个selectionKey.
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);//重新设置注册关系中感兴趣的就绪事件.
    }

    //渠道读就绪
    private void doRead(SelectionKey key)
    {
        System.out.println("doRead...");
        Attachment attachment = (Attachment)key.attachment();
        SocketChannel channel = (SocketChannel)key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8 * 1024);
        
        try
        {
            while (channel.read(buffer) > 0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            removeFromSelectedSet(channel);
            return;
        }
        
        buffer.flip();
        byte[] rspData = new byte[buffer.limit()];
        buffer.get(rspData);
        
        //客户端可能一下子接收到服务端发送过来的多个包.所以下面这么处理.
        ByteBuffer stream = ByteBuffer.wrap(rspData);
        while (stream.hasRemaining())
        {
            int packageLen = stream.getInt() - Integer.SIZE / 8;
            byte[] packageData = new byte[packageLen];
            stream.get(packageData);
            
            //这是一个数据包.
            ByteBuffer pack = ByteBuffer.allocate(512);
            pack.putInt(packageLen);
            pack.put(packageData);
            pack.flip();
            byte[] packData = new byte[pack.limit()];
            pack.get(packData);
            
            //打印客户端发来的请求流..
            printStream(packData);
            
            CmdResp resp = CmdResp.fromByteStream(packData);
            switch (CmdEnum.forCommand(resp.getCmd()))
            {
                case HEART_BEAT:
                    System.out.println("成功收到心跳响应from:[" + channel.socket().getPort() + "]");
                    onHeartBeatRsp(key);
                    
                    break;
                    
                case CLEAR_ACK:
                    System.out.println("成功收到clearAck响应from:[" + channel.socket().getPort() + "]");
                    onClearAckRsp(resp, attachment);
                    break;
                    
                case CLEAR_DIS_ACK:
                    System.out.println("成功收到clearDisAck响应from:[" + channel.socket().getPort() + "]");
                    onClearDisAckRsp(resp, attachment);
                    break;
                    
                case CLEAR_DIS_ACK_DTL:
                    System.out.println("成功收到clearDisAckDtl响应from:[" + channel.socket().getPort() + "]");
                    onClearAckDtlRsp(resp, attachment);
                    break;
            }
        }
    }
    
    //渠道写就绪.
    private void doWrite(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel)key.channel();
        Attachment attachment = (Attachment)key.attachment();
        NioQueue<ByteBuffer> heartbeatQueue = attachment.getHeartbeatQueue();
        NioQueue<CmdReq> reqQueue = attachment.getReqQueue();
        
        if (reqQueue.isEmpty() && heartbeatQueue.isEmpty())
        {
            return;
        }
        
        try
        {
            if (!heartbeatQueue.isEmpty())
            {
                channel.write(heartbeatQueue.poll());
            }
            
            if (!reqQueue.isEmpty())
            {
                CmdReq req = reqQueue.poll();
                channel.write(ByteBuffer.wrap(req.toByteStream()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            removeFromSelectedSet(channel);
        }
    }
    
    private void removeFromCancelledSet(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel)key.channel();
        Socket socket = channel.socket();
        InetSocketAddress host = (InetSocketAddress)socket.getRemoteSocketAddress();
        String ip = host.getAddress().getHostAddress();
        int port = socket.getPort();
        Host hostinfo = new Host();
        hostinfo.setIp(ip);
        hostinfo.setPort(port);
        
        hostSelector.removeFromCancelledHost(hostinfo);
        hostSelector.addSelectedHost(hostinfo);    
        
        //在注册列表中注册连接连接.
        hostSelector.getRegistry().put(ip + "_" + host.getPort(), key);
    }

    private void removeFromSelectedSet(SocketChannel channel)
    {
        Socket socket = channel.socket();
        InetSocketAddress host = (InetSocketAddress)socket.getRemoteSocketAddress();
        String ip = host.getAddress().getHostAddress();
        int port = socket.getPort();
        Host hostinfo = new Host();
        hostinfo.setIp(ip);
        hostinfo.setPort(port);
        
        hostSelector.removeSelectedHost(hostinfo);
        hostSelector.addCancelledHost(hostinfo);
        
        //从注册表中删除服务主机host对应的客户端连接.
        hostSelector.getRegistry().remove(ip + "_" + port);
        
        closeChannel(channel);
    }
    
    private void closeChannel(SocketChannel channel)
    {
        try
        {
            channel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 心跳响应处理逻辑.
     * @param key
     */
    private void onHeartBeatRsp(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel)key.channel();
        Socket socket = channel.socket();
        InetSocketAddress host = (InetSocketAddress)socket.getRemoteSocketAddress();
        String ip = host.getAddress().getHostAddress();
        int port = socket.getPort();
        Host hostinfo = new Host();
        hostinfo.setIp(ip);
        hostinfo.setPort(port);
        
        if (channel.isConnected())
        {
            hostSelector.addSelectedHost(hostinfo);
            hostSelector.removeFromCancelledHost(hostinfo);
        }
        else
        {
            hostSelector.removeSelectedHost(hostinfo);
            hostSelector.addCancelledHost(hostinfo);
        }
        
        //如果试连次数大于或者等于主机个数,则说明链路初始化已经完成.
        if (tryConnectCount >= hostSelector.getHostCount())
        {
            //此时每个服务器都有一个连接到该服务器的客户端连接,如果服务器可达.
            hostSelector.setChannelInitEnd(true);
        }
    }
    
    private void onClearAckRsp(CmdResp resp, Attachment attachment)
    {
        NioQueue<CmdResp> respQueue = attachment.getRespQueue();
        respQueue.offer(resp);
    }

    private void onClearDisAckRsp(CmdResp resp, Attachment attachment)
    {
        NioQueue<CmdResp> respQueue = attachment.getRespQueue();
        respQueue.offer(resp);
    }

    private void onClearAckDtlRsp(CmdResp resp, Attachment attachment)
    {
        NioQueue<CmdResp> respQueue = attachment.getRespQueue();
        respQueue.offer(resp);
    }
    
    private void printStream(byte[] data)
    {
        StringBuilder builder = new StringBuilder();
        builder.delete(0, builder.length());
        builder.append("[");
        for (int i = 0; i < data.length; i++)
        {
            builder.append(data[i]).append(" ");
        }
        builder.append("]");
        
        System.out.println("[Server]:"+ builder.toString());
    }
}
