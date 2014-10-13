package com.howbuy.jso.utils.serialize;

import com.howbuy.jso.utils.serialize.decode.ObjectDecoder;
import com.howbuy.jso.utils.serialize.encode.ObjectEncoder;

/**
 * 序列化和反序列化者.
 * @author li.zhang
 * 2014-9-16
 *
 */
public class Serializer implements ISerialize
{
    /** encoder **/
    private final ObjectEncoder encoder;
    
    /** decoder **/
    private final ObjectDecoder decoder;
    
    /**
     * 构造方法
     */
    public Serializer()
    {
        encoder = new ObjectEncoder();
        decoder = new ObjectDecoder();
    }
    
    /**
     * 序列化对象操作.
     * @throws Exception 
     */
    @Override
    public byte[] serialize(Object obj) throws Exception
    {
        return encoder.encode(obj);
    }

    /**
     * 反序列化操作.
     */
    @Override
    public Object deserialize(byte[] bytes) throws Exception
    {
        return decoder.decode(bytes);
    }
}
