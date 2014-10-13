package com.howbuy.jso.service.network.stream;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.ArrayUtils;

public class BinaryReadStream2
{

    public ByteString m_data = null;
    int cur = 0;

    public BinaryReadStream2(byte[] bytes)
    {
        m_data = new ByteString(0);
        m_data.append(bytes);
        cur = Consts.BINARY_PACKLEN_LEN_2 + Consts.CHECKSUM_LEN;
    }

    public boolean IsEmpty()
    {
        return m_data.getBytes().length <= Consts.BINARY_PACKLEN_LEN_2 ? true
                : false;
    }

    public boolean ReadString(AtomicReference<String> s, long maxlen,
            AtomicLong outlen) throws UnsupportedEncodingException
    {
        AtomicInteger headlen = new AtomicInteger(0);
        AtomicInteger fieldlen = new AtomicInteger(0);
        if (!ReadLengthWithoutOffset(headlen, fieldlen))
        {
            return false;
        }

        // user buffer is not enough
        if (maxlen != 0 && fieldlen.get() > maxlen)
        {
            return false;
        }

        cur += headlen.get();

        // byte[] subarray = ArrayUtils.subarray(m_data.getBytes(), cur,
        // (int)(cur + fieldlen.get()));
        // s.set(new String(subarray, "ISO-8859-1"));
        s.set(new String(m_data.getBytes(), cur, fieldlen.get(), "ISO-8859-1"));
        outlen.set((long) fieldlen.get());
        cur += outlen.get();
        return true;
    }

    public boolean ReadInt(AtomicInteger i)
    {
        if (cur + 4 > m_data.getBytes().length)
        {
            return false;
        }
        byte[] subarray = ArrayUtils.subarray(m_data.getBytes(), cur,
                (int) (cur + 4));
        i.set(ByteUtils.ntohl(subarray));
        cur += 4;
        return true;
    }

    public boolean ReadShort(AtomicReference<Short> s)
    {
        if (cur + 1 > m_data.getBytes().length)
        {
            return false;
        }
        byte[] subarray = ArrayUtils.subarray(m_data.getBytes(), cur,
                (int) (cur + 2));
        s.set(convertntohs(subarray));
        cur += 2;
        return true;
    }

    public boolean ReadChar(AtomicReference<Character> c)
    {
        if (cur + 1 > m_data.getBytes().length)
        {
            return false;
        }
        c.set((char) m_data.getBytes()[cur]);
        cur += 1;
        return true;
    }

    public boolean ReadLength(AtomicInteger outlen)
    {
        AtomicInteger headlen = new AtomicInteger();
        if (!ReadLengthWithoutOffset(headlen, outlen))
        {
            return false;
        }
        // cur += BINARY_PACKLEN_LEN_2;
        cur += headlen.get();
        return true;
    }

    public boolean ReadLengthWithoutOffset(AtomicInteger headlen,
            AtomicInteger fieldlen)
    {
        headlen.set(0);
        int temp = cur;
        byte buf[] = new byte[5];
        for (int i = 0; i < buf.length; i++)
        {
            // memcpy(buf + i, temp, sizeof(char));
            buf[i] = m_data.getBytes()[temp];
            // temp++;
            temp++;
            // headlen++;
            headlen.set(headlen.get() + 1);

            // if ((buf[i] >> 7 | 0x0) == 0x0)
            if ((buf[i] & 0x80) == 0x00)
                break;
        }
        // if (cur + headlen.get() > m_data.getBytes().length)
        // return false;

        AtomicInteger value = new AtomicInteger(0);
        uncompress_(buf, headlen.get(), value);
        fieldlen.set(value.get());

        return true;
    }

    public boolean uncompress_(byte[] buf, int len, AtomicInteger i)
    {
        i.set(0);
        for (int index = 0; index < (int) len; index++)
        {
            char c = (char) buf[index];
            i.set(i.get() << 7);

            c &= 0x7f;
            i.set(i.get() | c);
        }
        // cout << "uncompress:" << i << endl;
        return true;
    }

    public boolean IsEnd()
    {
        return cur == m_data.getBytes().length;
    }

    public long ReadAll(byte[] buf, long iLen)
    {
        long iRealLen = Math.min(iLen, m_data.getBytes().length);
        byte[] subarray = ArrayUtils.subarray(m_data.getBytes(), 0,
                (int) (cur + iRealLen));
        m_data.clear();
        m_data.append(subarray);
        return iRealLen;
    }

    private short convertntohs(byte[] value)
    {
        ByteBuffer buf = ByteBuffer.wrap(value);
        return buf.getShort();

    }

    public long getSize()
    {
        return m_data.length();
    }

    public byte[] getData()
    {
        return m_data.getBytes();
    }

    void clear()
    {
        m_data.clear();
        m_data.append(new byte[6]);
    }
}
