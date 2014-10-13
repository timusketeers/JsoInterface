package com.howbuy.jso.service.network;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.howbuy.jso.service.CmdEnum;
import com.howbuy.network.entity.Result;

public class JsoExecutorTest
{
    public static void main(String[] args) throws Exception
    {
        JsoExecutor executor = JsoExecutor.getInstance();

        while (true)
        {
            Future<Result> result = executor.execute(CmdEnum.CLEAR_DIS_ACK.getName(), "taCode");
            
            Result rs = result.get(3000, TimeUnit.MILLISECONDS);
            if (null != rs)
            {
                System.out.println(rs.getRetCode() + "_" + rs.getRetMsg());
            }
           
            Thread.sleep(4000);
        }
    }
}
