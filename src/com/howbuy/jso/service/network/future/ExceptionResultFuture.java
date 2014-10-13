package com.howbuy.jso.service.network.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.howbuy.network.entity.Result;

public class ExceptionResultFuture implements Future<Result>
{
    private String retCode;
    
    private String retMsg;
    
    /**
     * 构造方法
     * @param retCode
     * @param retMsg
     */
    public ExceptionResultFuture(String retCode, String retMsg)
    {
        this.retCode = retCode;
        this.retMsg = retMsg;
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
        return true;
    }

    @Override
    public Result get() throws InterruptedException, ExecutionException
    {
        Result result = new Result();
        result.setRetCode(retCode);
        result.setRetMsg(retMsg);
        return result;
    }

    @Override
    public Result get(long timeout, TimeUnit unit) throws InterruptedException, 
        ExecutionException, TimeoutException
    {
        Result result = new Result();
        result.setRetCode(retCode);
        result.setRetMsg(retMsg);
        return result;
    }
}
