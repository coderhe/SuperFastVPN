package com.android.ppp.data;

public class OrderData
{
    private String rowNo;
    private String id;
    private double money;
    private String creationTime;
    private int status;

    public OrderData(String rowNo, String id, double money, int status, String creationTime)
    {
        this.rowNo = rowNo;
        this.id = id;
        this.money = money;
        this.status = status;
        this.creationTime = creationTime;
    }

    public String getRowNo() {
        return rowNo;
    }

    public String getId() {
        return id;
    }

    public int getStatus() {
        return status;
    }

    public double getMoney() {
        return money;
    }

    public String getCreationTime() {
        return creationTime;
    }
}