package com.android.ppp.data;

public class Settings
{
    private String name;
    private int type;

    public Settings(String name, int type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }
}