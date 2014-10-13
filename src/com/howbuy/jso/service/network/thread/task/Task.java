package com.howbuy.jso.service.network.thread.task;

import java.util.concurrent.Future;

import com.howbuy.network.entity.Result;

public interface Task
{
    public Future<Result> submit();
}
