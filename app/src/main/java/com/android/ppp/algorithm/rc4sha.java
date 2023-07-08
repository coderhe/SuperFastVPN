package com.android.ppp.algorithm;
import com.android.ppp.algorithm.standard.RC4SHA1_Base;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class rc4sha extends Cipher
{
    private RC4SHA1_Base rc4;

    public rc4sha(String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        super("rc4-sha1", key);
        this.rc4 = new RC4SHA1_Base(key);
    }

    @Override
    public byte[] Decrypt(byte[] buffer, int offset, int length)
    {
        return this.rc4.Decrypt(buffer, offset, length);
    }

    @Override
    public byte[] Encrypt(byte[] buffer, int offset, int length)
    {
        return this.rc4.Encrypt(buffer, offset, length);
    }
}