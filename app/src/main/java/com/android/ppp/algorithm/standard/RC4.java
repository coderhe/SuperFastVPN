package com.android.ppp.algorithm.standard;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.lang.String;
import java.security.NoSuchAlgorithmException;

public class RC4 implements ICryptography
{
    private static final int MAXBIT = 255;
    private byte[] vk; // s-box
    private String key;

    public RC4(String key, byte[] vk)
    {
        if (key == null || key.isEmpty())
            return;

        this.key = key;
        this.vk = vk;
    }

    public static byte[] SBox(String key)
    {
        byte[] box = new byte[MAXBIT];
        for (int i = 0; i < MAXBIT; i++)
        {
            box[MAXBIT - (i + 1)] = (byte)i;
        }

        byte[] info = key.getBytes();
        for (int i = 0, j = 0; i < MAXBIT; i++)
        {
            j = (j + (box[i] & 0xff)+ info[i % info.length]) % MAXBIT;
            byte b = box[i];
            box[i] = box[j];
            box[j] = b;
        }

        return box;
    }

    public static String GetKeyMessage(String key) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[key.length()];
        md.update(key.getBytes("UTF-8"), 0, key.length());
        buffer = md.digest();
        String message = "";
        //String hex = "";
        for (int i = 0; i < buffer.length; i++)
        {
            //@TODO 換一種寫法
            //hex = Integer.toHexString(buffer[i]);
            //if (hex.length() == 1)
            //    hex = '0' + hex;
            message += Integer.toHexString((buffer[i] & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase();
        }

        return message;
    }

    private void Encrypt(String key, byte[] sbox, byte[] num, int len)
    {
        byte[] v = new byte[sbox.length];
        //Buffer.BlockCopy(sbox, 0, v, 0, v.length);
        System.arraycopy(sbox, 0, v, 0, v.length);
        for (int i = 0, low = 0, high = 0, mid; i < len; i++)
        {
            low = (low + key.length()) % MAXBIT;
            high = (high + (v[i % MAXBIT] & 0xff)) % MAXBIT;

            byte b = v[low];
            v[low] = v[high];
            v[high] = b;

            mid = ((v[low] & 0xff) + (v[high] & 0xff)) % MAXBIT;
            num[i] ^= v[mid];
        }
    }

    public byte[] Encrypt(byte[] buffer, int offset, int length)
    {
        int counts = 0;
        if (buffer != null)
            counts = buffer.length;

        if (buffer == null && (offset != 0 || length != 0))
        {
            Log.i("RC4Encrypt","buffer == null && (offset != 0 || length != 0)");
            return new byte[0];
        }

        if (offset < 0)
        {
            Log.i("RC4Encrypt", "offset < 0");
            return new byte[0];
        }

        if (length < 0)
        {
            Log.i("RC4Encrypt","length < 0");
            return new byte[0];
        }

        int m = (offset + length);
        if (m > counts)
        {
            Log.i("RC4Encrypt","(offset + length) > buffer.Length");
            return new byte[0];
        }

        if (offset == counts)
            return new byte[0];

        byte[] content = new byte[length];
        //Buffer.blockCopy(buffer, offset, content, 0, length);
        System.arraycopy(buffer, offset, content, 0, length);
        //fixed( byte*pinned = content)
        {
            Encrypt(key, vk, content, length);
        }

        return content;
    }

    public byte[] Decrypt(byte[] buffer, int offset, int length)
    {
        int counts = 0;
        if (buffer != null)
            counts = buffer.length;

        if (buffer == null && (offset != 0 || length != 0))
        {
            Log.i("RC4Decrypt","buffer == null && (offset != 0 || length != 0)");
            return new byte[0];
        }

        if (offset < 0)
        {
            Log.i("RC4Decrypt","offset < 0");
            return new byte[0];
        }

        if (length < 0)
        {
            Log.i("RC4Decrypt","length < 0");
            return new byte[0];
        }

        int m = (offset + length);
        if (m > counts)
        {
            Log.i("RC4Decrypt","(offset + length) > buffer.Length");
            return new byte[0];
        }

        if (offset == counts)
            return new byte[0];

        byte[] content = new byte[length];
        //Buffer.BlockCopy(buffer, offset, content, 0, length);
        System.arraycopy(buffer, offset, content, 0, length);
        //fixed(byte*pinned = content)
        {
            Encrypt(this.key, this.vk, content, length);
        }

        return content;
    }
}