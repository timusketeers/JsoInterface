package com.howbuy.jso.service.network.thread;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.howbuy.jso.service.CmdEnum;
import com.howbuy.jso.service.network.attachment.Attachment;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.queue.NioQueue;

/**
 * 心跳线程，用于维持链路.
 * @author li.zhang
 * 2014-9-22
 *
 */
public class HeartbeatThread extends Thread
{
    /** key **/
    private SelectionKey key;

    /**
     * 构造方法
     * @param key
     */
    public HeartbeatThread(SelectionKey key)
    {
        this.key = key;
    }

    public void run()
    {
        Attachment attachment = (Attachment)key.attachment();
        NioQueue<ByteBuffer> queue = attachment.getHeartbeatQueue();//心跳消息队列.
        while (true)
        {
            byte[] heartbeat = createHeartBeatStream();
            ByteBuffer buffer = ByteBuffer.wrap(heartbeat);
            
            queue.offer(buffer);
            
            try
            {
                //每隔5秒发送一次心跳消息.
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            
            //如果心跳包大于200个,清空队列，防止内存溢出.
            if (200 < queue.getQueueSize())
            {
                queue.clear();
            }
        }
    }
    
    /**
     * 创建心跳请求流.
     * @return
     */
    private byte[] createHeartBeatStream()
    {
        CmdReq heartbeat = new CmdReq();
        heartbeat.setCmd(CmdEnum.HEART_BEAT.getCommand());
        
        return heartbeat.toByteStream();
    }
}
