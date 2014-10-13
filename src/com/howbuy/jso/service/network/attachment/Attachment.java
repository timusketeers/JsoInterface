package com.howbuy.jso.service.network.attachment;

import java.nio.ByteBuffer;

import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.network.protocol.CmdResp;
import com.howbuy.jso.service.network.utils.Utils;
import com.howbuy.jso.service.queue.NioQueue;
import com.howbuy.network.entity.Result;

/**
 * 附件
 * @author li.zhang
 * 2014-9-24
 *
 */
public class Attachment
{
    /** 心跳队列. **/
    private NioQueue<ByteBuffer> heartbeatQueue = new NioQueue<ByteBuffer>();
    
    /** 业务请求指令队列. **/
    private NioQueue<CmdReq> reqQueue = new NioQueue<CmdReq>();
    
    /** 业务指令响应队列. **/
    private NioQueue<CmdResp> respQueue = new NioQueue<CmdResp>();
    
    /**
     * 根据seqId从队列中取出队列的response.
     * @param seqId
     * @return
     */
    public CmdResp fromRespQueue(int seqId)
    {
        CmdResp resp = null;
        if (null == respQueue || respQueue.isEmpty())
        {
            return resp;
        }
        
        for (int i = 0; i < respQueue.getQueueSize(); i++)
        {
            CmdResp item = respQueue.get(i);
            if (seqId == item.getSeqId())
            {
                resp = item;
                break;
            }
        }
        
        return resp;
    }
    
    public Result getResult(int seqId)
    {
        Result result = null;
        CmdResp resp = fromRespQueue(seqId);
        if (null == resp)
        {
            return result;
        }
        
        result = Utils.fromCmdResp(resp);
        
        return result;
    }
    
    public Result removeRspQueue(int seqId)
    {
        Result result = null;
        CmdResp item = fromRespQueue(seqId);
        if (null == item)
        {
            return result;
        }
        
        respQueue.remove(item);
        return result;
    }
    
    public NioQueue<ByteBuffer> getHeartbeatQueue()
    {
        return heartbeatQueue;
    }

    public void setHeartbeatQueue(NioQueue<ByteBuffer> heartbeatQueue)
    {
        this.heartbeatQueue = heartbeatQueue;
    }

    public NioQueue<CmdReq> getReqQueue()
    {
        return reqQueue;
    }

    public NioQueue<CmdResp> getRespQueue()
    {
        return respQueue;
    }
}
