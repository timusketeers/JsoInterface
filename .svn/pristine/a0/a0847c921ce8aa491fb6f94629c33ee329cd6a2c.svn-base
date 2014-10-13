/***********************************************************************
 * Module:  ObjectDecoder.java
 * Author:  li.zhang
 * Purpose: Defines the Class ObjectDecoder
 ***********************************************************************/
package com.howbuy.jso.utils.serialize.decode;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

import com.howbuy.jso.utils.serialize.ISelfCodec;

/**
 * 解码器
 * @author li.zhang
 */
public class ObjectDecoder
{
    /**
     * 从字节码中解码出对象.
     * @param bytes 对象的字节流
     * @param clzz bytes对应的类的class.
     * @return 返回对应的对象
     * @throws Exception 
     */
    public Object decode(byte[] data) throws Exception
    {
        ByteBuffer buffer = null;
        if (null == data || 0 == data.length)
        {
            return null;
        }
        
        buffer = ByteBuffer.wrap(data);
        buffer.rewind();
        Object obj = readObject(buffer);
        
        return obj;
    }

    /**
     * 
     * @param buffer
     * @param clzz
     * @return
     * @throws Exception
     */
    private Object readObject(ByteBuffer buffer) throws Exception
    {
        Object obj = null;
        int nullFlag = buffer.getInt();
        
        //如果为0表示当前对象是null,否则当前对象不为null.
        if (0 == nullFlag)
        {
            return obj;
        }
        
        int len = buffer.getInt();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        String className = new String(bytes, "utf-8");
        Class<?> clzz = Class.forName(className);
        
        //判断是否需要使用自编码方式进行编解码.
        if (isSelfCodec(clzz))
        {
            obj = clzz.newInstance();
            ((ISelfCodec)obj).decode(buffer);
        }
        //反射的方式进行编解码.
        else
        {
            if (isPrimitive(clzz))
            {
                obj = readPrimitive(buffer, clzz);
            }
            else if (isString(clzz))
            {
                obj = readString(buffer);
            }
            //是否是数组
            else if (clzz.isArray())
            {
                obj = readArray(buffer);
            }
            //是否是集合类,包括list, set等.
            else if (Collection.class.isAssignableFrom(clzz))
            {
                obj = readCollection(buffer, clzz);
            }
            //判断是否是map.
            else if (Map.class.isAssignableFrom(clzz))
            {
                readMap(buffer, clzz);
            }
            //否则是object类型
            else
            {
                obj = clzz.newInstance();
                Field[] fields = clzz.getDeclaredFields();
                for (int i = 0; i < fields.length; i++)
                {
                    Field field = fields[i];
                    field.setAccessible(true);
                    
                    Object fieldValue = readObject(buffer);
                    field.set(obj, fieldValue);
                }
            }
        }
        return obj;
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
    private boolean isPrimitive(Class<?> clzz)
    {
        boolean isPrimitive = false;
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
    private boolean isString(Class<?> clzz)
    {
        boolean isStr = false;
        if (String.class == clzz)
        {
            isStr = true;
        }
        
        return isStr;
    }
    
    private Object readPrimitive(ByteBuffer buffer, Class<?> clzz)
    {
        Object obj = null;
        if (byte.class == clzz || Byte.class == clzz)
        {
            obj = buffer.get();
        }
        else if (short.class == clzz || Short.class == clzz)
        {
            obj = buffer.getShort();
        }
        else if (int.class == clzz || Integer.class == clzz)
        {
            obj = buffer.getInt();
        }
        else if (long.class == clzz || Long.class == clzz)
        {
            obj = buffer.getLong();
        }
        else if (float.class == clzz || Float.class == clzz)
        {
            obj = buffer.getFloat();
        }
        else if (double.class == clzz || Double.class == clzz)
        {
            obj = buffer.getDouble();
        }
        else if (char.class == clzz || Character.class == clzz)
        {
            obj = buffer.getChar();
        }
        else if (boolean.class == clzz || Boolean.class == clzz)
        {
            obj = 1 == buffer.get() ? true : false;
        }
        
        return obj;
    }

    private String readString(ByteBuffer buffer) throws Exception
    {
        int length = buffer.getInt();
        byte[] dst = new byte[length];
        buffer.get(dst);
        
        String str = new String(dst, "utf-8");
        return str;
    }
    
    /**
     * 数组类型解码
     * @param buffer
     * @return
     * @throws Exception
     */
    private Object readArray(ByteBuffer buffer) throws Exception
    {
        int size = buffer.getInt();
        if (0 == size)
        {
            return new Object[0];
        }
        
        //得到数组中元素的类型.
        int length = buffer.getInt();
        byte[] dst = new byte[length];
        buffer.get(dst);
        String itemClassName = new String(dst, "utf-8");
        
        Class<?> clzz = Class.forName(itemClassName);
        
        //Array.newInstance(itemClass, size) 第一个参数指的是数组中元素的class类型.
        Object array = Array.newInstance(clzz, size);
        
        for (int i = 0; i < size; i++)
        {
            Array.set(array, i, readObject(buffer));
        }
        
        return array;
    }
    
    /**
     * 集合类型解码
     * @param buffer
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private Collection<Object> readCollection(ByteBuffer buffer, Class<?> clzz) throws Exception
    {
        Collection<Object> collection = (Collection<Object>)clzz.newInstance();
        
        int size = buffer.getInt();
        for (int i = 0; i < size; i++)
        {
            collection.add(readObject(buffer));
        }
        
        return collection;
    }
    
    /**
     * Map类型解码
     * @param buffer
     * @param clzz clzz
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void readMap(ByteBuffer buffer, Class<?> clzz) throws Exception
    {
        Map<Object, Object> map = (Map<Object, Object>)clzz.newInstance();
        
        int size = buffer.getInt();
        for (int i = 0; i < size; i++)
        {
            Object key = readObject(buffer);
            Object value = readObject(buffer);
            map.put(key, value);
        }
    }
}