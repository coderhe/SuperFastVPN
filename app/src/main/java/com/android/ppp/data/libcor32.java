package com.android.ppp.data;

public class libcor32
{
    static
    {
        System.loadLibrary("cor32");
    }

    public native boolean sendack(int connect, int errorcode);

    public native void closesocket(int sock);

    public native int create_unix_socket(String path);

    public native int[] recvfd(int sock);
}