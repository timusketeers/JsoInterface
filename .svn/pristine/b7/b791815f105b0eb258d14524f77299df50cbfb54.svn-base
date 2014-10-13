package com.howbuy.jso.service.network.config;

/**
 * 获取配置.
 * @author li.zhang
 * 2014-9-24
 *
 */
public class Config
{
    /** INSTANCE. **/
    private static final Config INSTANCE = new Config();
    
    /** 清算服务器列表. **/
    private String settleServersList;
    
    /** 就绪选择超时时间. **/
    private long selectionTimeout;
    
    /**
     * 构造方法
     */
    private Config()
    {
        
    }
    
    /**
     * 获取单例.
     * @return
     */
    public static Config getInstance()
    {
        return INSTANCE;
    }

    public String getSettleServersList()
    {
        return settleServersList;
    }

    public void setSettleServersList(String settleServersList)
    {
        this.settleServersList = settleServersList;
    }

    public long getSelectionTimeout()
    {
        return selectionTimeout;
    }

    public void setSelectionTimeout(long selectionTimeout)
    {
        this.selectionTimeout = selectionTimeout;
    }
}
