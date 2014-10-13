package com.howbuy.jso.utils.serialize;

public interface ISerialize
{
    /**
     * 序列化对象obj
     * @param obj
     * @return
     */
    public byte[] serialize(Object obj) throws Exception;
    
    /**
     * 将字节流反序列化为对应的对象.
     * @param bytes
     * @return
     */
    public Object deserialize(byte[] bytes) throws Exception;;
}
