package com.howbuy.jso.service.network.protocol;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.howbuy.jso.service.network.sequence.SeqIdGenerator;
import com.howbuy.jso.service.network.stream.BinaryReadStream2;
import com.howbuy.jso.service.network.stream.BinaryWriteStream3;

/**
 * 指令请求对应的实体.
 * @author li.zhang
 * 2014-9-22
 *
 */
public class CmdReq
{
    /** 命令的标识.  **/
    private short cmd;
    
    /** 表示序列. **/
    private int seqId = SeqIdGenerator.nextSeqId();
    
    /** 参数列表.**/
    private Param[] params;
    
    /**
     * 根据流构造cmdReq对象.
     * @param data
     * @return
     */
    public static CmdReq fromStream(byte[] data)
    {
        CmdReq req = null;
       
        if (null == data || 0 == data.length)
        {
            return req;
        }
        
        req = new CmdReq();
        
        BinaryReadStream2 reader = new BinaryReadStream2(data);
        
        //read cmd
        AtomicReference<Short> cmd = new AtomicReference<Short>();
        reader.ReadShort(cmd);
        req.setCmd(cmd.get());
        
        //read seqId
        AtomicInteger seq = new AtomicInteger();
        reader.ReadInt(seq);
        req.setSeqId(seq.get());
            
        Param param = null;
        List<Param> params = new ArrayList<Param>();
        while (!reader.IsEnd())
        {
            param = new Param();
            AtomicReference<String> paramValue = new AtomicReference<String>();
            AtomicLong valuelen = new AtomicLong();
            try
            {
                reader.ReadString(paramValue, 0, valuelen);
                
                param.setHeader(valuelen.get());
                param.setParamstr(paramValue.get());
                
                params.add(param);
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
        
        Param[] paramArray = new Param[params.size()];
        params.toArray(paramArray);
        
        req.setParams(paramArray);
        return req;
    }
    
    /**
     * 将对应的实体转换成对应的字节流.
     * @return
     */
    public byte[] toByteStream()
    {
        BinaryWriteStream3 writer = new BinaryWriteStream3();
        
        writer.Write(cmd);
        writer.Write(seqId);
        
        if (null == params || 0 == params.length)
        {
            writer.Flush();
            return writer.getData();
        }
        
        for (int i = 0; i < params.length; i++)
        {
            String param = params[i].getParamstr();
            writer.Write(param, param.length());
        }
        
        writer.Flush();
        return writer.getData();
    }

    public short getCmd()
    {
        return cmd;
    }

    public void setCmd(short cmd)
    {
        this.cmd = cmd;
    }

    public int getSeqId()
    {
        return seqId;
    }

    public void setSeqId(int seqId)
    {
        this.seqId = seqId;
    }

    public Param[] getParams()
    {
        return params;
    }

    public void setParams(Param[] params)
    {
        this.params = params;
    }
}
