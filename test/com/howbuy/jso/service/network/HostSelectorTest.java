package com.howbuy.jso.service.network;

import java.nio.channels.SelectionKey;

import com.howbuy.jso.service.CmdEnum;
import com.howbuy.jso.service.network.attachment.Attachment;
import com.howbuy.jso.service.network.protocol.CmdReq;
import com.howbuy.jso.service.queue.NioQueue;

public class HostSelectorTest
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        HostSelector selector = HostSelector.getInstance();
        
        SelectionKey key = selector.select();
        
        Attachment attachment = (Attachment)key.attachment();
        CmdReq cmdReq = new CmdReq();
        cmdReq.setCmd(CmdEnum.CLEAR_DIS_ACK.getCommand());
        NioQueue<CmdReq> queue = attachment.getReqQueue();
        queue.offer(cmdReq);
        
        while (true);
    }

}
