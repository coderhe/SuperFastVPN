package com.android.ppp.data;

public class PortData
{
    public static final int REGION_TYPE = 0;
    public static final int ROUTE_TYPE = 1;
    private int type;
    private int hint;
    private int port;

    public PortData(int type, int hint, int port) {
        this.hint = hint;
        this.type = type;
        this.port = port;
    }

    public int getHint() {
        return hint;
    }

    public int getType() {
        return type;
    }

    public String getPort() {
        return "" + port;
    }
}