/*
 * Copyright 2018 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.ppp.adapter;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.android.ppp.R;
import com.android.ppp.algorithm.Rc4sha1;
import com.android.ppp.config.Config;
import com.android.ppp.config.Status;
import com.android.ppp.data.AppData;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.data.PPPVpnService;
import com.android.ppp.data.RouteData;
import com.android.ppp.data.ShopData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.external.ExternalAdaptInfo;
import me.jessyan.autosize.external.ExternalAdaptManager;
import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.onAdaptListener;
import me.jessyan.autosize.utils.AutoSizeLog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BaseApplication extends Application
{
    private static final int TIME_TASK = 1;
    private static final int LOG_TASK = 2;
    private List<Long> trafficSizeDatas = new ArrayList<>();

    private static BaseApplication application;

    public static BaseApplication GetInstance()
    {
        return application;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        application = this;

        _UpdateAccount();
        _UpdateLogText();

        //当 App 中出现多进程, 并且您需要适配所有的进程, 就需要在 App 初始化时调用 initCompatMultiProcess()
        //在 Demo 中跳转的三方库中的 DefaultErrorActivity 就是在另外一个进程中, 所以要想适配这个 Activity 就需要调用 initCompatMultiProcess()
        AutoSize.initCompatMultiProcess(this);

        //如果在某些特殊情况下出现 InitProvider 未能正常实例化, 导致 AndroidAutoSize 未能完成初始化
        //可以主动调用 AutoSize.checkAndInit(this) 方法, 完成 AndroidAutoSize 的初始化后即可正常使用
//        AutoSize.checkAndInit(this);

//        如何控制 AndroidAutoSize 的初始化，让 AndroidAutoSize 在某些设备上不自动启动？https://github.com/JessYanCoding/AndroidAutoSize/issues/249

        /**
         * 以下是 AndroidAutoSize 可以自定义的参数, {@link AutoSizeConfig} 的每个方法的注释都写的很详细
         * 使用前请一定记得跳进源码，查看方法的注释, 下面的注释只是简单描述!!!
         */
        AutoSizeConfig.getInstance()

                //是否让框架支持自定义 Fragment 的适配参数, 由于这个需求是比较少见的, 所以须要使用者手动开启
                //如果没有这个需求建议不开启
                .setCustomFragment(true)

                //是否屏蔽系统字体大小对 AndroidAutoSize 的影响, 如果为 true, App 内的字体的大小将不会跟随系统设置中字体大小的改变
                //如果为 false, 则会跟随系统设置中字体大小的改变, 默认为 false
//                .setExcludeFontScale(true)

                //区别于系统字体大小的放大比例, AndroidAutoSize 允许 APP 内部可以独立于系统字体大小之外，独自拥有全局调节 APP 字体大小的能力
                //当然, 在 APP 内您必须使用 sp 来作为字体的单位, 否则此功能无效, 不设置或将此值设为 0 则取消此功能
//                .setPrivateFontScale(0.8f)

                //屏幕适配监听器
                .setOnAdaptListener(new onAdaptListener() {
                    @Override
                    public void onAdaptBefore(Object target, Activity activity) {
                        //使用以下代码, 可以解决横竖屏切换时的屏幕适配问题
                        //使用以下代码, 可支持 Android 的分屏或缩放模式, 但前提是在分屏或缩放模式下当用户改变您 App 的窗口大小时
                        //系统会重绘当前的页面, 经测试在某些机型, 某些情况下系统不会主动重绘当前页面, 所以这时您需要自行重绘当前页面
                        //ScreenUtils.getScreenSize(activity) 的参数一定要不要传 Application!!!
//                        AutoSizeConfig.getInstance().setScreenWidth(ScreenUtils.getScreenSize(activity)[0]);
//                        AutoSizeConfig.getInstance().setScreenHeight(ScreenUtils.getScreenSize(activity)[1]);
                        AutoSizeLog.d(String.format(Locale.ENGLISH, "%s onAdaptBefore!", target.getClass().getName()));
                    }

                    @Override
                    public void onAdaptAfter(Object target, Activity activity) {
                        AutoSizeLog.d(String.format(Locale.ENGLISH, "%s onAdaptAfter!", target.getClass().getName()));
                    }
                })

                //是否打印 AutoSize 的内部日志, 默认为 true, 如果您不想 AutoSize 打印日志, 则请设置为 false
               .setLog(true)
                //是否使用设备的实际尺寸做适配, 默认为 false, 如果设置为 false, 在以屏幕高度为基准进行适配时
                //AutoSize 会将屏幕总高度减去状态栏高度来做适配
                //设置为 true 则使用设备的实际屏幕高度, 不会减去状态栏高度
                //在全面屏或刘海屏幕设备中, 获取到的屏幕高度可能不包含状态栏高度, 所以在全面屏设备中不需要减去状态栏高度，所以可以 setUseDeviceSize(true)
                .setUseDeviceSize(true)
        ;
        customAdaptForExternal();

        if(ShopData.ShopItems == "") {
            _HttpGetShopSettings();
        }
    }

    /**
     * 给外部的三方库 {@link Activity} 自定义适配参数, 因为三方库的 {@link Activity} 并不能通过实现
     * {@link CustomAdapt} 接口的方式来提供自定义适配参数 (因为远程依赖改不了源码)
     * 所以使用 {@link ExternalAdaptManager} 来替代实现接口的方式, 来提供自定义适配参数
     */
    private void customAdaptForExternal()
    {
        /**
         * {@link ExternalAdaptManager} 是一个管理外部三方库的适配信息和状态的管理类, 详细介绍请看 {@link ExternalAdaptManager} 的类注释
         */
                //加入的 Activity 将会放弃屏幕适配, 一般用于三方库的 Activity, 详情请看方法注释
                //如果不想放弃三方库页面的适配, 请用 addExternalAdaptInfoOfActivity 方法, 建议对三方库页面进行适配, 让自己的 App 更完美一点
                //.addCancelAdaptOfActivity(DefaultErrorActivity.class)

                //为指定的 Activity 提供自定义适配参数, AndroidAutoSize 将会按照提供的适配参数进行适配, 详情请看方法注释
                //一般用于三方库的 Activity, 因为三方库的设计图尺寸可能和项目自身的设计图尺寸不一致, 所以要想完美适配三方库的页面
                //就需要提供三方库的设计图尺寸, 以及适配的方向 (以宽为基准还是高为基准?)
                //三方库页面的设计图尺寸可能无法获知, 所以如果想让三方库的适配效果达到最好, 只有靠不断的尝试
                //由于 AndroidAutoSize 可以让布局在所有设备上都等比例缩放, 所以只要您在一个设备上测试出了一个最完美的设计图尺寸
                //那这个三方库页面在其他设备上也会呈现出同样的适配效果, 等比例缩放, 所以也就完成了三方库页面的屏幕适配
                //即使在不改三方库源码的情况下也可以完美适配三方库的页面, 这就是 AndroidAutoSize 的优势
                //但前提是三方库页面的布局使用的是 dp 和 sp, 如果布局全部使用的 px, 那 AndroidAutoSize 也将无能为力
                //经过测试 DefaultErrorActivity 的设计图宽度在 380dp - 400dp 显示效果都是比较舒服的
                //.addExternalAdaptInfoOfActivity(DefaultErrorActivity.class, new ExternalAdaptInfo(true, 400));
    }

    /**
     * 获取版本号
     */
    private int _GetVersionCode()
    {
        // 获取packagemanager的实例
        PackageManager packagemanager = getPackageManager();
        // getpackagename()是你当前类的包名，0代表是获取版本信息
        PackageInfo packinfo = packagemanager.getPackageInfo(getPackageName(), 0);

        return packinfo.versionCode;
    }

    private void _HttpGetShopSettings()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(Config.GLOBAL_SETTINGS).get().build();
        final Call call = client.newCall(request);
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("error","出错");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                Log.e("APPLICATION", "GET SHOP");
                ShopData.ShopItems = response.body().string();
            }
        });
    }

    private void _HttpQueryPayOrder()
    {
        try {
            JSONObject params = new JSONObject();
            params.put("AccountId", AppData.mUserUUId);
            params.put("PageIndex", 1);
            params.put("PageCount", 50);

            HttpUtils.post(Config.PAY_QUERY_URL, HttpUtils.OKHttpPost(params), new okhttp3.Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    if(Config.CanLog)
                        Log.e("QueryOrderActivity", Config.PAY_QUERY_URL);
                }

                @Override
                public void onResponse(Call call, Response respo) throws IOException
                {
                    String data = respo.body().string();
                    if(Config.CanLog)
                        Log.e("QueryOrderActivity", data);

                    JSONObject result = null;
                    try {
                        result = new JSONObject(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        int nStatus = result.getInt("Error");
                        Status status = Status.values()[nStatus];
                        if(status == Status.Success)
                        {
                            AppData.mPayOrders = result.getString("Tag");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    private void _UpdateAccount()
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                Message message = new Message();
                message.what = TIME_TASK;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, Config.ScheudlerTimer, Config.ScheudlerTimer);
    }

    private void _UpdateLogText()
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                Message message = new Message();
                message.what = LOG_TASK;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, Config.LogScheudlerTimer, Config.LogScheudlerTimer);
    }

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    if(AppData.mUserUUId != null)
                    {
                        _HttpUpdateAccount();
                        if(AppData.mPayOrders == null || AppData.mPayOrders == "") {
                            _HttpQueryPayOrder();
                        }
                    }
                    break;
                case 2:
                    if(AppData.isConnect)
                        _ReadOutOrInTrafficSize();
                    break;
            }
        }
    };

    private void _HttpUpdateAccount()
    {
        _ReadOutOrInTrafficSize();
        try {
            JSONObject params = new JSONObject();
            params.put("AccountId", AppData.mUserUUId);
            params.put("Mac", AppData.mUserMac);
            params.put("Exit", AppData.isConnect ? 0 : 1);
            long incoming = trafficSizeDatas.size() > 0 ? trafficSizeDatas.get(0) : 0;
            params.put("IncomingTraffic", incoming);
            long outgoing = trafficSizeDatas.size() > 1 ? trafficSizeDatas.get(1) : 0;
            params.put("OutgingTraffic", outgoing);

            HttpUtils.post(Config.ACCOUNT_UPDATE_URL, HttpUtils.OKHttpPost(params), new okhttp3.Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    if(Config.CanLog)
                        Log.e("Application", Config.ACCOUNT_UPDATE_URL);
                }

                @Override
                public void onResponse(Call call, Response respo) throws IOException
                {
                    String data = respo.body().string();
                    if(Config.CanLog)
                        Log.e("Application", data);

                    JSONObject result = null;
                    try {
                        result = new JSONObject(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        int nStatus = result.getInt("Error");
                        Status status = Status.values()[nStatus];
                        if(status == Status.Success)
                        {
                            String mOldPassword = AppData.mPassword != null ? AppData.mPassword : "";
                            String tag = Rc4sha1.Decrypt(result.getString("Tag"));
                            if(Config.CanLog)
                                Log.e("Application", tag);
                            result = new JSONObject(tag);
                            RouteData.Routes = result.getString("Regions");
                            AppData.mUserUUId = result.getString("Id");
                            AppData.mUserType = result.getInt("Type");
                            AppData.mUserEmail = result.getString("Email");
                            AppData.mPassword = result.getString("Password");
                            AppData.mUserMac = result.getString("Mac");
                            AppData.mUserExpirationTime = result.getString("ExpirationTime");
                            AppData.mUseIncomingTraffic = result.getLong("UseIncomingTraffic");
                            AppData.mUseOutgingTraffic = result.getLong("UseOutgingTraffic");
                            AppData.mRemainIncomingTraffic = result.getLong("RemainIncomingTraffic");
                            AppData.mRemainOutgoingTraffic = result.getLong("RemainOutgoingTraffic");
                            AppData.mResetTrafficTime = result.getString("ResetTrafficTime");
                            AppData.mServerTime = result.getString("ServerTime");
                            if(AppData.isConnect && AppData.IsCanUseVpnService() == false)
                            {
                                AppData.isConnect = false;
                                sendBroadcast(new Intent(PPPVpnService.BROADCAST_STOP_VPN));
                            }
                            //修改了密码
                            if(mOldPassword.equals(AppData.mPassword) == false)
                                JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "password", AppData.mPassword);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
        }
    }

    private void _ReadOutOrInTrafficSize()
    {
        if(trafficSizeDatas != null)
            trafficSizeDatas.clear();

        //判断是否有LOG_TXT
        String logFilePath = Config.APP_PACKAGE_PATH + "/" + Config.LOG_TXT;
        File logFile = new File(logFilePath);
        if(logFile.exists() == false)
            return;

        String logData = JsonUtils.ReadJsonFile(logFilePath);
        if(logData.length() <= 0)
            return;

        int index = logData.indexOf(Config.IP);
        int index1 = logData.indexOf(Config.Tcp);
        int index2 = logData.indexOf(Config.Udp);
        int index3 = logData.indexOf(Config.Icmp);
        if(index == -1 || index1 == -1 || index2 == -1 || index3 == -1)
            return;

        String ipData = logData.substring(index, index1);
        String tcpData = logData.substring(index1, index2);
        String udpData = logData.substring(index2, index3);
        String icmpData = logData.substring(index3, logData.length());
        //IP
        int idx = ipData.indexOf(Config.Outgoing_Traffic_Size);
        int idx1 = ipData.indexOf(Config.Incoming_Traffic_Size);
        int idx2 = ipData.indexOf(Config.Outgoing_Unicast_Packet);
        int idx3 = ipData.indexOf(Config.Outgoing_Speed_Seconds);
        int idx4 = ipData.indexOf(Config.Incoming_Speed_Seconds);
        String ipOutgoingData = ipData.substring(idx+Config.Outgoing_Traffic_Size.length(), idx1).trim();
        String ipIncomingData = ipData.substring(idx1+Config.Incoming_Traffic_Size.length(), idx2).trim();
        long ipOutgoingSize = _GetDataSize(ipOutgoingData);
        long ipIncomingSize = _GetDataSize(ipIncomingData);

        String ipOutgoingSpeedData = ipData.substring(idx3+Config.Outgoing_Speed_Seconds.length(), idx4).trim();
        String ipIncomingSpeedData = ipData.substring(idx4+Config.Incoming_Speed_Seconds.length(), ipData.length()).trim();
        long ipOutgoingSpeed = _GetDataSize(ipOutgoingSpeedData);
        long ipIncomingSpeed = _GetDataSize(ipIncomingSpeedData);
        //UDP
        idx = udpData.indexOf(Config.Outgoing_Traffic_Size);
        idx1 = udpData.indexOf(Config.Incoming_Traffic_Size);
        idx2 = udpData.indexOf(Config.Outgoing_Unicast_Packet);
        idx3 = udpData.indexOf(Config.Outgoing_Speed_Seconds);
        idx4 = udpData.indexOf(Config.Incoming_Speed_Seconds);
        int idx5 = udpData.indexOf(Config.Activity_All_Ports);
        String udpOutgoingData = udpData.substring(idx+Config.Outgoing_Traffic_Size.length(), idx1).trim();
        String udpIncomingData = udpData.substring(idx1+Config.Incoming_Traffic_Size.length(), idx2).trim();
        long udpOutgoingSize = _GetDataSize(udpOutgoingData);
        long udpIncomingSize = _GetDataSize(udpIncomingData);

        String udpOutgoingSpeedData = udpData.substring(idx3+Config.Outgoing_Speed_Seconds.length(), idx4).trim();
        String udpIncomingSpeedData = udpData.substring(idx4+Config.Incoming_Speed_Seconds.length(), idx5).trim();
        long udpOutgoingSpeed = _GetDataSize(udpOutgoingSpeedData);
        long udpIncomingSpeed = _GetDataSize(udpIncomingSpeedData);
        //TCP - 1
        idx = tcpData.indexOf(Config.Outgoing_Traffic_Size);
        idx1 = tcpData.indexOf(Config.Incoming_Traffic_Size);
        idx2 = tcpData.indexOf(Config.Outgoing_Unicast_Packet);
        String tcpOutgoingData = tcpData.substring(idx+Config.Outgoing_Traffic_Size.length(), idx1).trim();
        String tcpIncomingData = tcpData.substring(idx1+Config.Incoming_Traffic_Size.length(), idx2).trim();
        long tcpOutgoingSize = _GetDataSize(tcpOutgoingData);
        long tcpIncomingSize = _GetDataSize(tcpIncomingData);
        //ICMP
        idx = icmpData.indexOf(Config.Outgoing_Traffic_Size);
        idx1 = icmpData.indexOf(Config.Incoming_Traffic_Size);
        idx2 = icmpData.indexOf(Config.Outgoing_Unicast_Packet);
        idx3 = icmpData.indexOf(Config.Outgoing_Speed_Seconds);
        idx4 = icmpData.indexOf(Config.Incoming_Speed_Seconds);
        String icmpOutgoingData = icmpData.substring(idx+Config.Outgoing_Traffic_Size.length(), idx1).trim();
        String icmpIncomingData = icmpData.substring(idx1+Config.Incoming_Traffic_Size.length(), idx2).trim();
        long icmpOutgoingSize = _GetDataSize(icmpOutgoingData);
        long icmpIncomingSize = _GetDataSize(icmpIncomingData);

        String icmpOutgoingSpeedData = icmpData.substring(idx3+Config.Outgoing_Speed_Seconds.length(), idx4).trim();
        String icmpIncomingSpeedData = icmpData.substring(idx4+Config.Incoming_Speed_Seconds.length(), icmpData.length()).trim();
        long icmpOutgoingSpeed = _GetDataSize(icmpOutgoingSpeedData);
        long icmpIncomingSpeed = _GetDataSize(icmpIncomingSpeedData);
        //TCP - 2
        idx = tcpData.indexOf(Config.Tunnel_Traffic_Size);
        idx1 = tcpData.indexOf(Config.Connect_Connections);
        idx2 = tcpData.indexOf(Config.Tunnel_Speed_Seconds);
        String sTunnelData = tcpData.substring(idx+Config.Tunnel_Traffic_Size.length(), idx1).trim();
        String[] sTunnelDatas = sTunnelData.split("/");
        String data1 = sTunnelDatas[0].trim();
        String data2 = sTunnelDatas[1].trim();
        String tcpInTunnelData = data1.substring(4, data1.length());
        String tcpOutTunnelData = data2.substring(5, data2.length());
        long tcpInTunnelSize = _GetDataSize(tcpInTunnelData);
        long tcpOutTunnelSize = _GetDataSize(tcpOutTunnelData);

        String stcpSpeedData = tcpData.substring(idx2+Config.Tunnel_Speed_Seconds.length(), idx).trim();
        String[] stcpSpeedDatas = stcpSpeedData.split("/");
        data1 = stcpSpeedDatas[0].trim();
        data2 = stcpSpeedDatas[1].trim();
        String tcpInTunnelSpeedData = data1.substring(4, data1.length());
        String tcpOutTunnelSpeedData = data2.substring(5, data2.length());
        long tcpIncomingSpeed = _GetDataSize(tcpInTunnelSpeedData);
        long tcpOutgoingSpeed = _GetDataSize(tcpOutTunnelSpeedData);
        //---------------------------------------------------------------------//
        //统计流量
        boolean is_full_nat = (boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fullNatMode");
        long tcpOutSize = (AppData.isConnect && is_full_nat) ? tcpOutgoingSize : tcpOutTunnelSize;
        long tcpInSize = (AppData.isConnect && is_full_nat) ? tcpIncomingSize : tcpInTunnelSize;
        //Incoming
        long totalIncomingSize = AppData.isConnect ? (tcpInSize + ipIncomingSize + udpIncomingSize + icmpIncomingSize) * AppData.mCurRouteMultiple / Config.TEN_THOUSAND : (tcpInSize + ipIncomingSize + udpIncomingSize + icmpIncomingSize);
        trafficSizeDatas.add(totalIncomingSize);
        AppData.mIncomingSpeed = is_full_nat ? ipIncomingSpeed * AppData.mCurRouteMultiple / Config.TEN_THOUSAND : (ipIncomingSpeed + udpIncomingSpeed + tcpIncomingSpeed + icmpIncomingSpeed) * AppData.mCurRouteMultiple / Config.TEN_THOUSAND;
        //Outgoing
        long totalOutgoingSize = AppData.isConnect ? (tcpOutSize + ipOutgoingSize + udpOutgoingSize + icmpOutgoingSize) * AppData.mCurRouteMultiple / Config.TEN_THOUSAND : (tcpOutSize + ipOutgoingSize + udpOutgoingSize + icmpOutgoingSize);
        trafficSizeDatas.add(totalOutgoingSize);
        AppData.mOutgoingSpeed = is_full_nat ? ipOutgoingSpeed * AppData.mCurRouteMultiple / Config.TEN_THOUSAND : (ipOutgoingSpeed + udpOutgoingSpeed + tcpOutgoingSpeed + icmpOutgoingSpeed) * AppData.mCurRouteMultiple / Config.TEN_THOUSAND;
    }

    private long _GetDataSize(String data)
    {
        String[] datas = data.split(" ");
        if(datas.length != 2)
            return 0;

        if(datas[1].equals("B"))
        {
            return (long)(Float.valueOf(datas[0].trim()).floatValue());
        }
        else if(datas[1].equals("KB"))
        {
            return (long)(Float.valueOf(datas[0].trim()).floatValue() * 1024);
        }
        else if(datas[1].equals("MB"))
        {
            return (long)(Float.valueOf(datas[0].trim()).floatValue() * 1024 * 1024);
        }
        else if(datas[1].equals("GB"))
        {
            return (long)(Float.valueOf(datas[0].trim()).floatValue() * 1024 * 1024 * 1024);
        }

        return 0;
    }
}