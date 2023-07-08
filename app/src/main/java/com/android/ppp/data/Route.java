package com.android.ppp.data;

import android.os.Message;
import android.util.Log;

import com.android.ppp.R;
import com.android.ppp.adapter.PingNet;
import com.android.ppp.adapter.PingNetEntity;
import com.android.ppp.config.Config;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Route
{
    public static final int REGION_TYPE = 0;
    public static final int ROUTE_TYPE = 1;

    private String name;
    private int type;
    private String region;
    private String addresses;
    private String cipher;
    private int fullNat;
    private int multiple;
    private int qos;

    private String delayTime = "";

    public Route(String name, String region, String address, String cipher, int fullNat, int multiple, int qos, int type)
    {
        this.name = name;
        this.region = region;
        this.type = type;
        this.addresses = address;
        this.cipher = cipher;
        this.fullNat = fullNat;
        this.multiple = multiple;
        this.qos = qos;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getIcon()
    {
        switch (this.region)
        {
            case "hk":
                return R.mipmap.ic_hk;
            case "sg":
                return R.mipmap.ic_sg;
            case "us":
                return R.mipmap.ic_us;
            case "jp":
                return R.mipmap.ic_jp;
            case "kr":
                return R.mipmap.ic_kr;
        }

        return 0;
    }

    public int getFullNat() {
        return fullNat;
    }

    public int getMultiple() {
        return multiple;
    }

    public int getQos() {
        return qos;
    }

    public String getAddresses() {
        return addresses;
    }

    public String getRegion(){
        return region;
    }

    public String getCipher() {
        return cipher;
    }

    public String getPingDelay()
    {
        if(delayTime == null || delayTime == "")
        {
            delayTime = "0";
            return delayTime;
        }

        _ShowPingDelay();
        return delayTime;
    }

    private void _ShowPingDelay()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                PingNetEntity pingNetEntity = new PingNetEntity(_GetIPAddress(), 1, 2, new StringBuffer());
                pingNetEntity = PingNet.ping(pingNetEntity);
                delayTime = pingNetEntity.getPingTime();
            }
        }).start();
    }

    private String _GetIPAddress()
    {
        String data = null;
        String[] datas = null;
        try {
            JSONArray ary = new JSONArray(addresses);
            data = ary.getString(0);
            if(data != null)
            {
                datas = data.split(":");
                if(datas != null)
                {
                    return datas[0];
                }
                return "";
            }
            return "";
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "";
        }
    }
}