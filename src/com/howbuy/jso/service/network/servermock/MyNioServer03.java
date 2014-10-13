package com.howbuy.jso.service.network.servermock;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import com.howbuy.jso.service.CmdEnum;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.network.protocol.CmdResp;
import com.howbuy.jso.service.network.protocol.Param;

public class MyNioServer03
{
    private int BUFFERSIZE = 1024 * 1;
    
    private Selector sel;

    public MyNioServer03(int port) throws IOException
    {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), port));
        sel = Selector.open();
        ssc.register(sel, SelectionKey.OP_ACCEPT);
    }

    public void startup()
    {
        System.out.println("Server start...");
        try
        {
            while (!Thread.interrupted())
            {
                int keysCount = sel.select();
                if (keysCount < 1)
                {
                    continue;
                }
                Set<SelectionKey> set = sel.selectedKeys();
                Iterator<SelectionKey> it = set.iterator();
                while (it.hasNext())
                {
                    SelectionKey key = it.next();
                    if (key.isAcceptable())
                    {
                        doAccept(key);
                    }
                    if (key.isValid() && key.isReadable())
                    {
                        doRead(key);
                    }
                    if (key.isValid() && key.isWritable())
                    {
                        doWrite(key);
                    }
                }
                set.clear();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        System.out.println("Server stop...");
        shutdown();
    }

    public void shutdown()
    {
        Set<SelectionKey> keys = sel.keys();
        for (SelectionKey key : keys)
        {
            try
            {
                key.channel().close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            sel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void doAccept(SelectionKey key)
    {
        try
        {
            System.out.println("doAccept...");
            SocketChannel sc = ((ServerSocketChannel)key.channel()).accept();
            sc.configureBlocking(false);
            SelectionKey newkey = sc.register(sel, SelectionKey.OP_READ);
            newkey.attach(new LinkedList<ByteBuffer>());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void doRead(SelectionKey key)
    {
        try
        {
            System.out.println("doRead...");
            SocketChannel sc = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFERSIZE);
            while ((sc.read(buffer)) > 0);
            
            buffer.flip();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data, 0, buffer.limit());
            
            //服务端可能一下子接收到客户端发送过来的多个包.所以下面这么处理.
            ByteBuffer stream = ByteBuffer.wrap(data);
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
                
                CmdReq req = CmdReq.fromStream(packData);
                switch (CmdEnum.forCommand(req.getCmd()))
                {
                    case HEART_BEAT:
                        onHeartBeatRsp(key, req.getSeqId());
                        break;
                    case CLEAR_ACK:
                        onClearAck(key, req.getSeqId());
                        break;
                        
                    case CLEAR_DIS_ACK:
                        onClearDisAck(key, req.getSeqId());
                        break;
                        
                    case CLEAR_DIS_ACK_DTL:
                        onClearDisAckDtl(key, req.getSeqId());
                        break;  
                }
            }
            
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
        catch (IOException e)
        {
            disconnect(key);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void doWrite(SelectionKey key)
    {
        SocketChannel sc = (SocketChannel)key.channel();
        LinkedList<ByteBuffer> outseq = (LinkedList<ByteBuffer>)key.attachment();
        ByteBuffer bb = outseq.poll();
        if (bb == null)
        {
            return;
        }
        try
        {
            while (bb.hasRemaining())
            {
                sc.write(bb);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void disconnect(SelectionKey key)
    {
        try
        {
            key.channel().close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void printStream(byte[] data)
    {
        //if client disconnected, read return -1
        StringBuilder builder = new StringBuilder();
        builder.delete(0, builder.length());
        builder.append("[");
        for (int i = 0; i < data.length; i++)
        {
            builder.append(data[i]).append(" ");
        }
        builder.append("]");
        
        System.out.println("[client]:"+ builder.toString());
    }
    
    @SuppressWarnings("unchecked")
    private void onHeartBeatRsp(SelectionKey key, int seqId)
    {
        LinkedList<ByteBuffer> queue = (LinkedList<ByteBuffer>)key.attachment();
        CmdResp resp = new CmdResp();
        resp.setCmd(CmdEnum.HEART_BEAT.getCommand());
        resp.setSeqId(seqId);
        resp.setRetCode(0);
        
        queue.offer(ByteBuffer.wrap(resp.toByteStream()));
    }

    @SuppressWarnings("unchecked")
    private void onClearAck(SelectionKey key, int seqId)
    {
        LinkedList<ByteBuffer> queue = (LinkedList<ByteBuffer>)key.attachment();
        CmdResp resp = new CmdResp();
        resp.setCmd(CmdEnum.CLEAR_ACK.getCommand());
        resp.setSeqId(seqId);
        resp.setRetCode(0);
        Param[] params = new Param[1];
        params[0] = new Param();
        params[0].setParamstr("success");
        resp.setParams(params);
        
        queue.offer(ByteBuffer.wrap(resp.toByteStream()));
    }

    @SuppressWarnings("unchecked")
    private void onClearDisAck(SelectionKey key, int seqId)
    {
        LinkedList<ByteBuffer> queue = (LinkedList<ByteBuffer>)key.attachment();
        CmdResp resp = new CmdResp();
        resp.setCmd(CmdEnum.CLEAR_DIS_ACK.getCommand());
        resp.setSeqId(seqId);
        resp.setRetCode(0);
        Param[] params = new Param[1];
        params[0] = new Param();
        params[0].setParamstr("success");
        resp.setParams(params);
        
        queue.offer(ByteBuffer.wrap(resp.toByteStream()));
    }

    @SuppressWarnings("unchecked")
    private void onClearDisAckDtl(SelectionKey key, int seqId)
    {
        LinkedList<ByteBuffer> queue = (LinkedList<ByteBuffer>)key.attachment();
        CmdResp resp = new CmdResp();
        resp.setCmd(CmdEnum.CLEAR_DIS_ACK_DTL.getCommand());
        resp.setSeqId(seqId);
        resp.setRetCode(0);
        Param[] params = new Param[1];
        params[0] = new Param();
        params[0].setParamstr("success");
        resp.setParams(params);
        
        queue.offer(ByteBuffer.wrap(resp.toByteStream()));
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            MyNioServer03 server = new MyNioServer03(30001);
            server.startup();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Exception caught, program exiting…");
        }
    }
}

