package com.howbuy.jso.service.network.future;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.howbuy.jso.service.network.attachment.Attachment;
import com.howbuy.network.entity.Result;

/**
 * 表示seqId对应的异步结果封装.
 * @author li.zhang
 * 2014-9-24
 *
 */
public class ResultFuture implements Future<Result>
{   
    /** key **/
    private final SelectionKey key;
    
    /** attachment **/
    private final Attachment attachment;
    
    /** seqId **/
    private final int seqId;
    
    /**
     * 构造方法
     * @param key
     * @param seqId
     */
    public ResultFuture(SelectionKey key, int seqId)
    {
        this.key = key;
        this.attachment = (Attachment)key.attachment();
        this.seqId = seqId;
    }
    
    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return false;
    }

    @Override
    public boolean isCancelled()
    {
        return false;
    }

    @Override
    public boolean isDone()
    {
        boolean done = false;
        if (null == attachment)
        {
            return done;
        }
        
        SocketChannel channel = (SocketChannel)key.channel();
        if (!channel.isConnected())
        {
            done = true;
        }
        else
        {
            if (null != attachment.fromRespQueue(seqId))
            {
                done = true;
            }
        }
        
        return done;
    }

    @Override
    public Result get() throws InterruptedException, ExecutionException
    {
        //如果没有完成,这里将一直阻塞.
        while (!isDone());
        
        Result result = null;
        
        SocketChannel channel = (SocketChannel)key.channel();
        
        //如果selectionKey已经失效.
        if (!key.isValid())
        {
            result = new ExceptionResultFuture("0999", "Selection key is not valid...").get();
            return result;
        }
        
        //如果断连..
        if (!channel.isConnected())
        {
            result = new ExceptionResultFuture("0999", "This server abruptly encounter network interruption.").get();
            return result;
        }
        
        result = attachment.getResult(seqId);
        
        //将对应的响应从响应队列中删除,防止内存溢出.
        attachment.removeRspQueue(seqId);
        
        return result;
    }

    @Override
    public Result get(long timeout, TimeUnit unit) throws InterruptedException,
        ExecutionException, TimeoutException
    {
        Result result = null;
        
        long current = System.currentTimeMillis();
        long miseconds = unit.toMillis(timeout);
        
        //如果任务没有完成并且没有超时就一直阻塞在这里.
        boolean done = false;
        while (true)
        {
            if (isDone())
            {
                done = true;
                break;
            }
            
            if ((current + miseconds) < System.currentTimeMillis())
            {
                break;
            }
        }
            
        if (done)
        {
            result = attachment.getResult(seqId);
        }
        
        //将对应的响应从响应队列中删除,防止内存溢出.
        attachment.removeRspQueue(seqId);
        
        return result;
    }
}
