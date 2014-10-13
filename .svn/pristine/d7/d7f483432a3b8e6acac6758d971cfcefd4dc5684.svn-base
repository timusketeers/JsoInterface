package com.howbuy.jso.service;

/**
 * 命令枚举
 * @author li.zhang
 * 2014-9-19
 *
 */
public enum CmdEnum
{
    /** 心跳消息. **/
    HEART_BEAT("heartBeat", (short)3),
    
    /** cleardisack **/
    CLEAR_DIS_ACK("cleardisack", (short)1000),
    
    /** clearack **/
    CLEAR_ACK("clearack", (short)5),
    
    /** cleardisackdtl **/
    CLEAR_DIS_ACK_DTL("cleardisackdtl", (short)6);
    
    
    private String name;
    
    private short command;
    
    /**
     * 构造方法
     * @param name
     * @param command
     */
    private CmdEnum(String name, short command)
    {
        this.name = name;
        this.command = command;
    }

    /**
     * 根据名称得到对应的枚举类型.
     * @param name
     * @return
     */
    public static CmdEnum forName(String name)
    {
        CmdEnum cmdEnum = null;
        CmdEnum[] enums = CmdEnum.values();
        for (int i = 0; i < enums.length; i++)
        {
            cmdEnum = enums[i];
            if (cmdEnum.getName().equals(name))
            {
                break;
            }
        }
        
        return cmdEnum;
    }
    
    /**
     * 根据命令得到对应的枚举值.
     * @param cmd
     * @return
     */
    public static CmdEnum forCommand(short cmd)
    {
        CmdEnum cmdEnum = null;
        CmdEnum[] enums = CmdEnum.values();
        for (int i = 0; i < enums.length; i++)
        {
            cmdEnum = enums[i];
            if (cmdEnum.getCommand() == cmd)
            {
                break;
            }
        }
        
        return cmdEnum;
    }
    
    public String getName()
    {
        return name;
    }

    public short getCommand()
    {
        return command;
    }
}
