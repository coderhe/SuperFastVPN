package com.android.ppp.algorithm.standard;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class RC4SHA1_Base extends RC4
{
    public RC4SHA1_Base(String key)
            throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
        super(key, SBox(GetKeyMessage(key)));
    }
}