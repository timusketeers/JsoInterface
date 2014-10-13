package com.howbuy.jso.service.network.utils;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.howbuy.jso.service.network.protocol.CmdResp;
import com.howbuy.jso.service.network.protocol.Param;
import com.howbuy.network.entity.Result;

public class Utils
{
    public static Result fromCmdResp(CmdResp resp)
    {
        Result result = null;
        if (null == resp)
        {
            return result;
        }
        
        result = new Result();
        
        String returnCode = "";
        String returnMsg = "";
        Param[] params = resp.getParams();
        if (null != params && 0 < params.length)
        {
            try
            {
                String jsonstr = new String(params[0].getParamstr().getBytes("ISO-8859-1"), "utf-8");
                if (!StringUtils.isEmpty(jsonstr))
                {
                    JSONObject jsonObj = JSONObject.parseObject(jsonstr);
                    
                    returnCode = (String)jsonObj.get("ret");
                    returnMsg = (String)jsonObj.get("msg");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        result.setRetCode(returnCode);
        result.setRetMsg(returnMsg);
        
        return result;
    }
}
