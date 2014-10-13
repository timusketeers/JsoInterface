package com.howbuy.jso.service.network.stream;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang.ArrayUtils;

public class BinaryWriteStream3
{
    public ByteString m_data = null;

    public BinaryWriteStream3()
    {
        m_data = new ByteString(Consts.BINARY_PACKLEN_LEN_2 + Consts.CHECKSUM_LEN);
    }

    // 返回不含头的内容
    public byte[] getContent()
    {
        byte[] data = getData();
        return ArrayUtils.subarray(data, Consts.BINARY_PACKLEN_LEN_2
                + Consts.CHECKSUM_LEN, data.length);
    }

    public boolean Write(String str, int length)
    {
        byte buf[] = new byte[5];

        int compress_ = compress_(length, buf);
        m_data.append(buf, compress_);
        m_data.append(str, length);
        return true;
    }

    public boolean Write(int i)
    {
        byte[] array = FormatTransfer.toHH(i);
        m_data.append(array);
        return true;
    }

    public boolean Write(short s)
    {
        byte[] array = FormatTransfer.toHH(s);
        m_data.append(array);
        return true;
    }

    public boolean Write(char c)
    {
        m_data.append((byte) c);
        return true;
    }

    public static int compress_(int i, byte[] buf)
    {
        int ret = 0;
        for (int a = 4; a >= 0; a--)
        {
            char c;
            c = (char) (i >> (a * 7) & 0x7f);
            if (c == 0x00 && ret == 0)
                continue;

            if (a == 0)
                c &= 0x7f;
            else
                c |= 0x80;
            buf[ret] = (byte) c;
            ret++;
        }
        if (ret == 0)
        {
            ret++;
            buf[0] = 0;
        }
        return ret;
    }

    public static boolean compress_(int i, byte[] buf, AtomicInteger len)
    {
        len.set(0);
        for (int a = 4; a >= 0; a--)
        {
            char c;
            c = (char) (i >> (a * 7) & 0x7f);
            if (c == 0x00 && len.get() == 0)
                continue;

            if (a == 0)
                c &= 0x7f;
            else
                c |= 0x80;
            buf[len.get()] = (byte) c;
            len.set(len.get() + 1);
        }
        if (len.get() == 0)
        {
            len.set(len.get() + 1);
            buf[0] = 0;
        }
        return true;
    }

    public void Flush()
    {
        m_data.flip();

        byte[] ulen = FormatTransfer.toHH((int) m_data.length());
        m_data.put(0, ulen, ulen.length);

    }

    public long getSize()
    {
        return m_data.length();
    }

    public byte[] getData()
    {
        return m_data.getBytes();
    }

    public ByteString getBuffer()
    {
        return m_data;
    }

    public void clear()
    {
        m_data.clear();
        m_data.append(new byte[6]);
    }

    public static String bytes2HexString(byte[] b)
    {
        String ret = "";
        for (int i = 0; i < b.length; i++)
        {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1)
            {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    public static void main(String[] args) throws UnsupportedEncodingException
    {
        int rowCount = 2524;// 5251
        System.out.println(Integer.toHexString(rowCount));

        byte[] rc2 = FormatTransfer.toLH(rowCount);
        System.out.println("toLH: " + bytes2HexString(rc2));
        rc2 = FormatTransfer.toHH(rowCount);
        System.out.println("toHH: " + bytes2HexString(rc2));

        ByteBuffer bb = ByteBuffer.wrap(rc2);
        System.out.println(bb.getInt());

        ByteBuffer bb2 = ByteBuffer.wrap(new byte[16]);
        bb2.order(ByteOrder.BIG_ENDIAN);
        bb2.putInt(2524);
        bb2.putInt(26);

        byte[] array = bb2.array();
        System.out.println(bytes2HexString(array));

        String hex = "79900C00";
        hex = "000C9079";
        hex = "DC090000";
        hex = "000009DC";
        hex = "000C9079";
        hex = "000009DC";
        hex = "789C001D";
        byte[] array2 = hexStringToByteArray(hex);
        System.out.println(array2.length);
        ByteBuffer bbb = ByteBuffer.wrap(array2);

        System.out.println("LEN: " + bbb.getInt());

        // FormatTransfer.
        System.out.println("======================================");
        String str = "iamhere now中文一个字plussomeenglishwords";
        System.out.println(str.length());
        byte[] bytes = str.getBytes();
        System.out.println("DEFAULT: " + bytes.length);
        bytes = str.getBytes("GBK");
        System.out.println("GBK: " + bytes.length);
        bytes = str.getBytes("GB2312");
        System.out.println("GB2312: " + bytes.length);
        bytes = str.getBytes("UTF-8");
        System.out.println("UTF-8: " + bytes.length);

        bytes = str.getBytes("GB2312");
        String string = new String(bytes, "ISO-8859-1");
        System.out.println(string);
        System.out.println(new String(string.getBytes("ISO-8859-1"), "GB2312"));
        System.out.println("ISO-8859-1: "
                + new String(bytes, "ISO-8859-1").length());

        String compatibleString = ByteUtils.toCompatibleString(str);
        System.out.println(compatibleString);
        System.out.println(compatibleString.length());
        System.out.println(compatibleString.getBytes("ISO-8859-1").length);

        ByteBuffer bb4 = ByteBuffer.allocate(0);
        testByteBuffer(bb4);
        System.out.println(bb4.array().length);
    }

    public static void testByteBuffer(ByteBuffer bb)
            throws UnsupportedEncodingException
    {
        String str = "中国";
        byte[] bytes = str.getBytes("ISO-8859-1");
        bb = ByteBuffer.wrap(bytes);
        System.out.println(bb.array().length);
    }

    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
