package com.android.ppp.data;

import android.content.Context;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.config.Status;

import java.util.ArrayList;
import java.util.List;

public class AppData
{
    //-----------------------------------------MemberData-----------------------------------------//
    //用户uid
    public static String mUserUUId;
    //用户类型
    public static int mUserType;
    //用户邮箱
    public static String mUserEmail;
    //用户密码
    public static String mPassword;
    //用户vip截止时间
    public static String mUserExpirationTime;
    //服务器时间戳
    public static String mServerTime;
    //Mac地址
    public static String mUserMac;
    //已用流量（上行/下行）
    public static long mUseIncomingTraffic = 0;
    //已用流量（上行/下行）
    public static long mUseOutgingTraffic = 0;
    //可用流量（上行/下行）
    public static long mRemainIncomingTraffic = 0;
    //可用流量（上行/下行）
    public static long mRemainOutgoingTraffic = 0;
    //当前流量（下行）
    public static long mIncomingSpeed = 0;
    //当前流量（上行）
    public static long mOutgoingSpeed = 0;
    //重置流量时间
    public static String mResetTrafficTime;
    //购买订单列表
    public static String mPayOrders;
    //DNS服务器地址
    public static String mDNSServerAddresses[] = {"1.1.1.1", "8.8.8.8"};// 1.1.1.1, 8.8.8.8

    public static String mIpTxt;//./data/data/com.android.ppp/cache/ip.txt
    //VPN是否连接
    public static boolean isConnect = false;
    public static String mCurRouteName;
    public static String mCurRouteRegion;
    public static String mCurRouteAddresses;
    public static String mCurRouteCipher;
    public static int mCurRouteFullnat;
    public static int mCurRouteMultiple = 10000;
    public static int mCurQos = 0;

    //默认本地记住密码
    //public static boolean autoRecordPassword = true;
    //默认本地记录账号登陆-自动登录
    //public static boolean autoLogin = false;
    //允许局域网连接
    public static boolean isAllowLanConnect = false;
    //全局路由
    public static boolean isAllowGlobalRoute = false;
    //用户自定义路由ip
    public static String mSelfDefineRouteAddress;
    //全部地区路由
    public static List<String> selectRegions = new ArrayList<>();
    //所有区域配置
    public static String regionRouterSettings;
    public static String[] allRegionRouterInfos;

    //显示通知栏
    public static boolean isShowNotification = false;

    //允许使用VPN流量的apps
    public static List<String> allowUseVpnPackageNames = new ArrayList<>();

    //-----------------------------------------Interface------------------------------------------//
    //用正则表达式进行判断字符串是否是ip地址格式
    public static boolean IsIPAddressByRegex(String str)
    {
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        // 判断ip地址是否与正则表达式匹配
        if (str.matches(regex))
        {
            String[] arr = str.split("\\.");
            for (int i = 0; i < 4; i++)
            {
                int temp = Integer.parseInt(arr[i]);
                //如果某个数字不是0到255之间的数 就返回false
                if (temp < 0 || temp > 255)
                    return false;
            }

            return true;
        }
        else
            return false;
    }

    //字符串是纯数字
    public static boolean IsDigit(String str)
    {
        for(int i = 0; i <str.length(); i++)
        {
            if ((str.charAt(i) > '9') || (str.charAt(i) < '0'))
            {
                return false;
            }
        }

        return true;
    }

    //类型转换String转int
    public static int String2Int(String str)
    {
        try
        {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    //类型转换String转float
    public static float String2Float(String str)
    {
        try
        {
            return Float.parseFloat(str);
        }
        catch (NumberFormatException e)
        {
            return 0f;
        }
    }

    //判断用户能否使用Vpn服务
    public static boolean IsCanUseVpnService()
    {
        Config.UserType type = Config.UserType.values()[AppData.mUserType];
        if(type.equals(Config.UserType.Free) || type.equals(Config.UserType.Time))
        {
            return _IsInTimeDeadline();
        }
        else if(type.equals(Config.UserType.Data))
        {
            //你只需要确保规则是：IN >0 && OUT > 0 肯定流量用户
            return AppData.mRemainIncomingTraffic > 0 && AppData.mRemainOutgoingTraffic > 0;
        }
        else
        {
            return true;
        }
    }

    //判断用户套餐时间是否到期
    private static boolean _IsInTimeDeadline()
    {
        if(AppData.mUserExpirationTime == null || AppData.mUserExpirationTime.isEmpty())
            return false;

        if(AppData.mServerTime == null || AppData.mServerTime.isEmpty())
            return false;

        String[] expiration = AppData.mUserExpirationTime.split(" ");
        String[] server = AppData.mServerTime.split(" ");
        if(expiration.length == 2 && server.length == 2)
        {
            String[] expirationDate = expiration[0].split("-");
            String[] serverDate = server[0].split("-");
            if(expirationDate.length != serverDate.length)
                return false;

            for (int i = 0; i < expirationDate.length; ++i)
            {
                if(AppData.String2Int(expirationDate[i]) > AppData.String2Int(serverDate[i]))
                    return true;
            }

            expirationDate = expiration[1].split(":");
            serverDate = server[1].split(":");
            if(expirationDate.length != serverDate.length)
                return false;

            for (int i = 0; i < expirationDate.length; ++i)
            {
                if(AppData.String2Int(expirationDate[i]) > AppData.String2Int(serverDate[i]))
                    return true;
            }

            return false;
        }

        return false;
    }

    //获取服务器消息
    public static String GetServerResponse(Context context, Status status)
    {
        String info = "";
        switch (status)
        {
            case Success:
                info = context.getString(R.string.Success);
                break;
            case UnknownError:
                info = context.getString(R.string.UnknownError);
                break;
            case ArgumentNullException:
                info = context.getString(R.string.ArgumentNullException);
                break;
            case ArgumentOutOfRangeException:
                info = context.getString(R.string.ArgumentOutOfRangeException);
                break;
            case DatabaseAccessException:
                info = context.getString(R.string.DatabaseAccessException);
                break;
            case MacIsNullOrEmpty:
                info = context.getString(R.string.MacIsNullOrEmpty);
                break;
            case MacAddressNotExists:
                info = context.getString(R.string.MacAddressNotExists);
                break;
            case PasswordIsNullOrEmpty:
                info = context.getString(R.string.PasswordIsNullOrEmpty);
                break;
            case PasswordAreIllegal:
                info = context.getString(R.string.PasswordAreIllegal);
                break;
            case PasswordNotEquals:
                info = context.getString(R.string.PasswordNotEquals);
                break;
            case EmailIsNullOrEmpty:
                info = context.getString(R.string.EmailIsNullOrEmpty);
                break;
            case EmailAddressIsExists:
                info = context.getString(R.string.EmailAddressIsExists);
                break;
            case EmailAddressNotExists:
                info = context.getString(R.string.EmailAddressNotExists);
                break;
            case EmailAddressAreIllegal:
                info = context.getString(R.string.EmailAddressAreIllegal);
                break;
            case ConfigurationError:
                info = context.getString(R.string.ConfigurationError);
                break;
            case UnableToCreateOrder:
                info = context.getString(R.string.UnableToCreateOrder);
                break;
            case OrderNotExists:
                info = context.getString(R.string.OrderNotExists);
                break;
            case OrderStatusError:
                info = context.getString(R.string.OrderStatusError);
                break;
            case NotAllowBuyService:
                info = context.getString(R.string.NotAllowBuyService);
                break;
            case GithubAccessException:
                info = context.getString(R.string.GithubAccessException);
                break;
            case ActivationCodeNotExists:
                info = context.getString(R.string.ActivationCodeNotExists);
                break;
            case IsUseActivationCode:
                info = context.getString(R.string.IsUseActivationCode);
                break;
            case NotAllowUseActivationCode:
                info = context.getString(R.string.NotAllowUseActivationCode);
                break;
            case MultipleOnline:
                info = context.getString(R.string.MultipleOnline);
                break;
            case SerializationProtocolError:
                info = context.getString(R.string.SerializationProtocolError);
                break;
        }

        return info;
    }
}