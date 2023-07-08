package com.android.ppp.algorithm;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import org.apache.commons.codec.binary.Base64_New;

import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.security.NoSuchAlgorithmException;

public class Rc4sha1
{
    private static rc4sha rc4;

    static {
        try {
            rc4 = new rc4sha("h#5q4l954>4%k>p43hey28^3c9&?83y%");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] Encrypt(byte[] buffer)
            throws Exception
    {
        return rc4.Encrypt(buffer, 0, buffer.length);
    }

    public static byte[] Decrypt(byte[] buffer)
    {
        return rc4.Decrypt(buffer, 0, buffer.length);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String Encrypt(String s)
            throws Exception
    {
        if (s == null || s.isEmpty())
            return null;

        byte[] buffer = Rc4sha1.Encrypt(s.getBytes("UTF-8"));
        if (buffer == null)
            return null;

        return org.apache.commons.codec.binary.Base64_New.encodeBase64String(buffer);
    }

    public static String Decrypt(String s) throws UnsupportedEncodingException
    {
        if (s == null || s.isEmpty())
            return null;

        if (s.length() <= 0)
            return "";

        byte[] buffer = Base64_New.decodeBase64(s);
        return new String(Rc4sha1.Decrypt(buffer),"UTF-8");
    }
}