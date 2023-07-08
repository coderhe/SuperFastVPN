package com.android.ppp.data;

public class ShopItem
{
    private int id;
    private String name;
    private int price;
    private String des;

    public ShopItem(int id, String name, int price, String des)
    {
        this.id = id;
        this.name = name;
        this.price = price;
        this.des = des;
    }

    public int getId() { return id; }

    public String getName() { return name; }

    public String getPrice() {
        return "¥" + price;
    }

    public String getDes() {
        return des;
    }
}