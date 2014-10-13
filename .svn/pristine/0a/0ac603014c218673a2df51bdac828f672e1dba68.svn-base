/***********************************************************************
 * Module:  ObjectEncoder.java
 * Author:  li.zhang
 * Purpose: Defines the Class ObjectEncoder
 ***********************************************************************/
package com.howbuy.jso.utils.serialize.encode;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.howbuy.jso.utils.serialize.ISelfCodec;

/** 
 * 编码器
 * @author li.zhang
 */
public class ObjectEncoder
{
    /**
     * 编码
     * @param obj 对象
     * @return 返回编码后的字节流.
     * @throws Exception 
     */
    public byte[] encode(Object obj) throws Exception
    {
        //注意:这里最大容量是8K的字节数.就是说如果一个对象的大小超过了8K，不能够存储进来.
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 8 * 8);
        
        writeObject(buffer, obj);
        buffer.flip();
        
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst, 0, buffer.limit());
        return dst;
    }

   
    /**
     * 判断是否需要使用自编码方式编解码
     * @param clzz class
     * @return 判断是否使用自编码.
     */
    private boolean isSelfCodec(Class<?> clzz)
    {
        boolean isSelfCodec = false;
        if (ISelfCodec.class.isAssignableFrom(clzz))
        {
            isSelfCodec = true;
        }
        return isSelfCodec;
    }

    /**
     * 判断是否是基本类型, boolean, int, Boolean等类型都是基本类型.
     * @param obj obj
     * @return 是否是基本类型.
     */
    private boolean isPrimitive(Object obj)
    {
        boolean isPrimitive = false;
        Class<?> clzz = obj.getClass();
        if (byte.class == clzz || Byte.class == clzz)
        {
            isPrimitive = true;
        }
        else if (short.class == clzz || Short.class == clzz)
        {
            isPrimitive = true;
        }
        else if (int.class == clzz || Integer.class == clzz)
        {
            isPrimitive = true;
        }
        else if (long.class == clzz || Long.class == clzz)
        {
            isPrimitive = true;
        }
        else if (float.class == clzz || Float.class == clzz)
        {
            isPrimitive = true;
        }
        else if (double.class == clzz || Double.class == clzz)
        {
            isPrimitive = true;
        }
        else if (char.class == clzz || Character.class == clzz)
        {
            isPrimitive = true;
        }
        else if (boolean.class == clzz || Boolean.class == clzz)
        {
            isPrimitive = true;
        }
        
        return isPrimitive;
    }
    
    /**
     * 判断是否是string类型.
     * @param obj
     * @return
     */
    private boolean isString(Object obj)
    {
        boolean isStr = false;
        if (String.class == obj.getClass())
        {
            isStr = true;
        }
        
        return isStr;
    }
    
    /**
     * 基本类型的编码写入.
     * @param buffer
     * @param obj
     */
    private void writePrimitive(ByteBuffer buffer, Object obj)
    {
        Class<?> clzz = obj.getClass();
        if (byte.class == clzz || Byte.class == clzz)
        {
            buffer.put((Byte)obj);
        }
        else if (short.class == clzz || Short.class == clzz)
        {
            buffer.putShort((Short)obj);
        }
        else if (int.class == clzz || Integer.class == clzz)
        {
            buffer.putInt((Integer)obj);
        }
        else if (long.class == clzz || Long.class == clzz)
        {
            buffer.putLong((Long)obj);
        }
        else if (float.class == clzz || Float.class == clzz)
        {
            buffer.putFloat((Float)obj);
        }
        else if (double.class == clzz || Double.class == clzz)
        {
            buffer.putDouble((Double)obj);
        }
        else if (char.class == clzz || Character.class == clzz)
        {
            buffer.putChar((Character)obj);
        }
        else if (boolean.class == clzz || Boolean.class == clzz)
        {
            buffer.put(((Boolean)obj) ? (byte)1 : (byte)0);
        }
    }
    
    /**
     * 字符串的编码写入
     * @param buffer
     * @param obj
     * @throws Exception 
     */
    private void writeString(ByteBuffer buffer, Object obj) throws Exception
    {
        String str = (String)obj;
        byte[] bytes = str.getBytes("utf-8");
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }
    
    /**
     * 数组的编码写入.
     * @param buffer
     * @param obj
     * @throws Exception 
     */
    private void writeArray(ByteBuffer buffer, Object obj) throws Exception
    {
        int size = Array.getLength(obj);
        buffer.putInt(size);
        
        if (0 == size)
        {
            return;
        }
        
        Object item = Array.get(obj, 0);
        byte[] itemClassName = item.getClass().getName().getBytes("utf-8");
        buffer.putInt(itemClassName.length);
        buffer.put(itemClassName);
        
        
        for (int index = 0; index < size; index++)
        {
            Object member = Array.get(obj, index);
            writeObject(buffer, member);
        }
    }

    /**
     * 集合编码写入.
     * @param buffer
     * @param obj
     * @throws Exception
     */
    private void writeCollection(ByteBuffer buffer, Object obj) throws Exception
    {
        Collection<?> coll = (Collection<?>)obj;
        buffer.putInt(coll.size());
        
        Iterator<?> iterator = coll.iterator();
        while (iterator.hasNext())
        {
            writeObject(buffer, iterator.next());
        }
    }
    
    /**
     * map编码写入
     * @param buffer
     * @param obj
     * @throws Exception
     */
    private void writeMap(ByteBuffer buffer, Object obj) throws Exception
    {
        Map<?, ?> map = (Map<?, ?>)obj;
        buffer.putInt(map.size());
        
        Iterator<?> iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            Object key = iterator.next();
            writeObject(buffer, key);
            writeObject(buffer, map.get(key));
        }
    }
    
    /**
     * 对象编码写入.
     * @param buffer
     * @param obj
     * @throws Exception
     */
    private void writeObject(ByteBuffer buffer, Object obj) throws Exception
    {
        if (null == obj)
        {
            buffer.putInt(0);//0作为这个对象是null的标志.
            return;
        }
        
        buffer.putInt(1);//1作为这个对象非null的标志.
        
        Class<?> clzz = obj.getClass();
        byte[] className = clzz.getName().getBytes("utf-8");
        buffer.putInt(className.length);
        buffer.put(className);
        
        //判断是否需要使用自编码方式进行编解码.
        if (isSelfCodec(clzz))
        {
            ISelfCodec o = (ISelfCodec)obj;
            buffer.put(o.encode());
        }
        //反射的方式进行编解码.
        else
        {
            if (isPrimitive(obj))
            {
                writePrimitive(buffer, obj);
            }
            else if (isString(obj))
            {
                writeString(buffer, obj);
            }
            //是否是数组
            else if (clzz.isArray())
            {
                writeArray(buffer, obj);
            }
            //是否是集合类,包括list, set等.
            else if (Collection.class.isAssignableFrom(clzz))
            {
                writeCollection(buffer, obj);
            }
            //判断是否是map.
            else if (Map.class.isAssignableFrom(clzz))
            {
                writeMap(buffer, obj);
            }
            //否则是object类型
            else
            {
                Field[] fields = clzz.getDeclaredFields();
                for (int i = 0; i < fields.length; i++)
                {
                    Field field = fields[i];
                    field.setAccessible(true);
                    
                    Object fieldValue = field.get(obj);
                    writeObject(buffer, fieldValue);
                }
            }
        }
    }
}