package com.android.ppp.data;

import android.graphics.drawable.Drawable;

public class AppItem
{
    private String name;
    private String packageName;
    private Drawable icon;

    public AppItem(String name, String packageName, Drawable icon)
    {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
    }

    public String getName() { return name; }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }
}