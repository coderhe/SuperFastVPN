package com.android.ppp.ui.vpn;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.data.AppData;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.data.PPPVpnService;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.home.HomeActivity;
import com.android.ppp.ui.login.LoginActivity;
import com.android.ppp.ui.shop.ShopActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.AutoSizeConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.android.ppp.config.Config.*;

public class RoutesActivity extends AppCompatActivity implements CustomAdapt
{
    private static final int VPN_REQUEST_CODE = 0x0F;
    private static final int ROUTE_REQUEST_CODE = 0x1F;

    private ImageButton btnStart;
    private TextView textStart;

    private ImageButton btnIpFrame;
    private ImageButton btnShoppingFrame;

    private TextView txtRoute;
    private TextView txtSelectedRoute;
    private TextView defaultRouteText;
    private TextView txtMemberVipTime;

    // 底部菜单4个Linearlayout
    private LinearLayout ll_home;
    private LinearLayout ll_change_ip;
    private LinearLayout ll_shopping;

    // 底部菜单4个ImageView
    private ImageView iv_home;
    private ImageView iv_change_ip;
    private ImageView iv_shopping;

    // 底部菜单4个菜单标题
    private TextView tv_home;
    private TextView tv_change_ip;
    private TextView tv_shopping;

    private TextView txtOutgoingSpeed;
    private TextView txtIncomingSpeed;

    private Drawable mConnectedButtonDrawable;
    private Drawable mNonConnectButtonDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //VerifyStoragePermissions(this);
        setContentView(R.layout.activity_routes);

        // 初始化控件
        _InitView();
        // 初始化底部按钮事件
        _InitEvent();

        if(!AppData.isShowNotification)
        {
            AppData.isShowNotification = true;
            ShowNotification();
        }

        registerReceiver(vpnStateReceiver, new IntentFilter(PPPVpnService.BROADCAST_VPN_STATE));
        btnStart = findViewById(R.id.connect_btn);
        //textStart = findViewById(R.id.connect_btn_text);
        txtRoute = findViewById(R.id.route_txt);
        //txtSelectedRoute = findViewById(R.id.route_text);
        //txtMemberVipTime = findViewById(R.id.member_time_text);
        btnIpFrame = findViewById(R.id.ip_frame);
        //btnShoppingFrame = findViewById(R.id.shopping_frame);
        //defaultRouteText = findViewById(R.id.default_route_txt);
        txtIncomingSpeed = findViewById(R.id.incoming_speed_txt);
        txtOutgoingSpeed = findViewById(R.id.outgoing_speed_txt);

        mConnectedButtonDrawable = getResources().getDrawable(R.mipmap.ic_connect_already_connect_word);
        mNonConnectButtonDrawable = getResources().getDrawable(R.mipmap.ic_connect_non_connect_word);

        String routeName = (String)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "route_name");
        if(routeName == null || routeName.isEmpty() || (AppData.mCurRouteName != null && AppData.mCurRouteName.equals(routeName)))
        {
            _SetRouteName(AppData.mCurRouteName != null ? AppData.mCurRouteName : getString(R.string.default_route));
        }
        else
        {
            AppData.mCurRouteName = routeName;
            AppData.mCurRouteAddresses = (String)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "route_address");
            AppData.mCurRouteCipher = (String)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "route_cipher");
            AppData.mCurQos = (int)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "route_qos");
            _ShowCurRoteInfo();
        }

        if(textStart != null)
            textStart.setText(AppData.isConnect ? R.string.disconnect : R.string.connect);

        //_ShowMemberVipTime();
        _UpdateRouteSpeed();
        if(btnStart != null)
        {
            btnStart.setImageDrawable(AppData.isConnect ? mConnectedButtonDrawable : mNonConnectButtonDrawable);
            btnStart.setOnClickListener(new OnMultiClickListener()
            {
                @Override
                public void onMultiClick(View v)
                {
                    if(!AppData.isConnect)
                    {
                        if(AppData.IsCanUseVpnService() == false)
                        {
                            Toast.makeText(getApplicationContext(), R.string.user_not_charge, Toast.LENGTH_SHORT);
                            return;
                        }
                        _StartVPN();
                    }
                    else
                        sendBroadcast(new Intent(PPPVpnService.BROADCAST_STOP_VPN));
                }
            });
        }

        if(btnIpFrame != null)
        {
            btnIpFrame.setOnClickListener(new OnMultiClickListener() {
                @Override
                public void onMultiClick(View v)
                {
                    if (AppData.isConnect){
                        Toast.makeText(getApplicationContext(), R.string.select_warning, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(AppData.IsCanUseVpnService() == false)
                        {
                            Toast.makeText(getApplicationContext(), R.string.user_not_charge_1, Toast.LENGTH_SHORT);
                            return;
                        }
                        _OpenSelectRouteActivity();
                    }
                }
            });
        }

        if(btnShoppingFrame != null)
        {
            btnShoppingFrame.setOnClickListener(new OnMultiClickListener() {
                @Override
                public void onMultiClick(View v) {
                    _FinishActivity();
                    _TurnToShopActivity();
                }
            });
        }
    }

    private void _InitEvent()
    {
        // 设置按钮监听
        ll_shopping.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v) {
                _FinishActivity();
                _TurnToShopActivity();
            }
        });
        ll_home.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v) {
                _FinishActivity();
                _TurnToHomeActivity();
            }
        });
    }

    private void _InitView()
    {
        // 底部菜单4个Linearlayout
        this.ll_change_ip = (LinearLayout) findViewById(R.id.ll_change_ip);
        this.ll_shopping = (LinearLayout) findViewById(R.id.ll_shopping);
        this.ll_home = (LinearLayout) findViewById(R.id.ll_home);

        // 底部菜单4个ImageView
        this.iv_home = (ImageView) findViewById(R.id.iv_home);
        this.iv_change_ip = (ImageView) findViewById(R.id.iv_change_ip);
        this.iv_shopping = (ImageView) findViewById(R.id.iv_shopping);

        // 底部菜单4个菜单标题
        this.tv_home = (TextView) findViewById(R.id.tv_home);
        this.tv_change_ip = (TextView) findViewById(R.id.tv_change_ip);
        this.tv_shopping = (TextView) findViewById(R.id.tv_shopping);

        iv_change_ip.setImageResource(R.mipmap.ic_connect_change_ip_green);
        tv_change_ip.setTextColor(0xff1B940A);
        iv_shopping.setImageResource(R.mipmap.ic_connect_shopping);
        tv_shopping.setTextColor(0xffffffff);
        iv_home.setImageResource(R.mipmap.ic_connect_mine);
        tv_home.setTextColor(0xffffffff);
    }

    private void _ShowMemberVipTime()
    {
        if(txtMemberVipTime != null)
        {
            UserType type = UserType.values()[AppData.mUserType];
            if(type.equals(UserType.Free))
            {
                txtMemberVipTime.setText(AppData.mUserExpirationTime + getString(R.string.to_date));
            }
            else if(type.equals(UserType.Time))
            {
                txtMemberVipTime.setText(AppData.mUserExpirationTime + getString(R.string.to_date));
            }
            else if(type.equals(UserType.Data))
            {
                DecimalFormat df = new DecimalFormat("0.00");
                txtMemberVipTime.setText(df.format((double)(AppData.mRemainIncomingTraffic + AppData.mRemainOutgoingTraffic) / 1024 / 1024) + getString(R.string.remain_data));
            }
            else if(type.equals(UserType.Inner))
            {
                txtMemberVipTime.setText(R.string.vip);
            }
        }
    }

    private void _OpenSelectRouteActivity()
    {
        //打开SelectRouteActivity
        Intent it = new Intent(getApplicationContext(), SelectRouteActivity.class);
        startActivityForResult(it, ROUTE_REQUEST_CODE);
    }

    private void _TurnToHomeActivity()
    {
        //关闭此activity，打开HomeActivity
        Intent it = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(it);
    }

    private void _TurnToShopActivity()
    {
        //关闭此activity，打开shopactivity
        Intent it = new Intent(getApplicationContext(), ShopActivity.class);
        startActivity(it);
    }

    //关闭Activity
    private void _FinishActivity()
    {
        if (this != null)
            this.finish();
    }

    private void _SetRouteName(String name)
    {
        if(txtRoute != null)
            txtRoute.setText(getString(R.string.current_route) + name);
    }

    private Handler _StopHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    });

    private Runnable _StopRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            stopService(new Intent(RoutesActivity.this, PPPVpnService.class));
        }
    };

    private Runnable _PingGoogleRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            boolean isConnect = _PingGoogle();
            AppData.isConnect = isConnect;
            _UpdateConnectShowData();
        }
    };

    private void _HttpGetGoogle()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://www.google.com/").get().build();
        final Call call = client.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                if(Config.CanLog)
                    Log.e("PingGoogle", "HttpGet Error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                if(Config.CanLog)
                    Log.e("PingGoogle", "HttpGet Success");
                AppData.isConnect = true;
            }
        });
    }

    private boolean _PingGoogle()
    {
        try
        {
            Process process = Runtime.getRuntime().exec("ping www.google.com");//-c 3 指定ping次数3
            int result = process.waitFor();
            //result为0，表示网络正常，否则异常
            Log.e("PingGoogle", result + "");
            return result == 0;
        }
        catch (IOException | InterruptedException var)
        {
            var.printStackTrace();
        }

        return false;
    }

    private void _UpdateRouteSpeed()
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, Config.LogScheudlerTimer, Config.LogScheudlerTimer);
    }

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what){
                case 1: {
                    _ShowRouteSpeed();
                    //_RefreshRemainDataSize();
                    break;
                }
            }
        }
    };

    private void _ShowRouteSpeed()
    {
        if(AppData.isConnect)
        {
            if(txtIncomingSpeed != null)
                txtIncomingSpeed.setText(getString(R.string.incoming_speed) + _GetFormatSpeed(AppData.mIncomingSpeed));

            if(txtOutgoingSpeed != null)
                txtOutgoingSpeed.setText(getString(R.string.outgoing_speed) + _GetFormatSpeed(AppData.mOutgoingSpeed));
        }
        else
        {
            if(txtIncomingSpeed != null)
                txtIncomingSpeed.setText(getString(R.string.empty));

            if(txtOutgoingSpeed != null)
                txtOutgoingSpeed.setText(getString(R.string.empty));
        }
    }

    private String _GetFormatSpeed(long speed)
    {
        DecimalFormat df = new DecimalFormat("0.00");
        if(speed < 1024)
            return speed + " B/S";
        else if(speed >= 1024 && speed < (1024 * 1024))
            return df.format((double)speed / 1024) + " KB/S";
        else
            return df.format((double)speed / 1024 / 1024) + " MB/S";
    }

    //如果用户类型是Data，刷新剩余数据流量
    private void _RefreshRemainDataSize()
    {
        UserType type = UserType.values()[AppData.mUserType];
        if(type.equals(UserType.Data) && txtMemberVipTime != null)
        {
            DecimalFormat df = new DecimalFormat("0.00");
            txtMemberVipTime.setText(df.format((double)(AppData.mRemainIncomingTraffic + AppData.mRemainOutgoingTraffic) / 1024 / 1024) + getString(R.string.remain_data));
        }
    }

    //刷新显示是否连接vpn的控件信息
    private void _UpdateConnectShowData()
    {
        if(textStart != null)
            textStart.setText(AppData.isConnect ? R.string.disconnect : R.string.connect);

        if(btnStart != null)
            btnStart.setImageDrawable(AppData.isConnect ? mConnectedButtonDrawable : mNonConnectButtonDrawable);

        _ShowRouteSpeed();
    }

    private BroadcastReceiver vpnStateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (PPPVpnService.BROADCAST_VPN_STATE.equals(intent.getAction()))
            {
                if (intent.getBooleanExtra("running", false))
                {
                    //_StopHandler.postDelayed(_PingGoogleRunnable,1500);
                    AppData.isConnect = true;
                }
                else
                {
                    AppData.isConnect = false;
                    _StopHandler.postDelayed(_StopRunnable,200);
                }
                _UpdateConnectShowData();
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK)
        {
            startService(new Intent(this, PPPVpnService.class));
        }
        else if(requestCode == ROUTE_REQUEST_CODE && resultCode == RESULT_OK)
        {
            AppData.mCurRouteName = data.getStringExtra("name");
            AppData.mCurRouteRegion = data.getStringExtra("region");
            AppData.mCurRouteAddresses = data.getStringExtra("address");
            AppData.mCurRouteCipher = data.getStringExtra("cipher");
            AppData.mCurRouteFullnat = data.getIntExtra("fullnat", 0);
            AppData.mCurRouteMultiple = data.getIntExtra("multiple", TEN_THOUSAND);
            AppData.mCurQos = data.getIntExtra("qos", 0);
            JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "route_name", AppData.mCurRouteName);
            JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "route_address", AppData.mCurRouteAddresses);
            JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "route_cipher", AppData.mCurRouteCipher);
            JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "route_qos", AppData.mCurQos);

            _ShowCurRoteInfo();
        }
    }

    private void _ShowCurRoteInfo()
    {
        _SetRouteName(AppData.mCurRouteName);
        _ModifyAddress(AppData.mCurRouteAddresses);
        _ModifyCipher(AppData.mCurRouteCipher);

        //限制QOS流量
        JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "ppp", "bandwidthQOS", AppData.mCurQos);
        if((boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fullNatMode"))
            JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fullNatMode", false);
    }

    private void _ModifyAddress(String address)
    {
        String data = null;
        String[] datas = null;
        try {
            JSONArray ary = new JSONArray(address);
            data = ary.getString(0);
            if(data != null)
            {
                datas = data.split(":");
                if(datas != null)
                {
                    JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "ppp", "proxyAddress", datas[0]);
                    JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "ppp", "proxyPort", AppData.String2Int(datas[1]));
                }
            }

            data = ary.getString(1);
            if(data != null)
            {
                datas = data.split(":");
                if (datas != null)
                {
                    JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "ppp", "datagramAddress", datas[0]);
                    JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "ppp", "datagramPort", AppData.String2Int(datas[1]));
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void _ModifyCipher(String cipher)
    {
        try {
            JSONObject object = new JSONObject(cipher);
            if(object == null)
                return;

            JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "cipher", "protocol", object.getString("Protocol"));
            JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "cipher", "protocolKey", object.getString("ProtocolKey"));
            JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "cipher", "transport", object.getString("Transport"));
            JsonUtils.ModifyElement(APP_PACKAGE_PATH + "/" + APP_SETTINGS_NAME, "cipher", "transportKey", object.getString("TransportKey"));
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void _StartVPN()
    {
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null)
        {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        }
        else
        {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    public void VerifyStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            Log.e("RoutesActivity", "VerifyStoragePermissions");
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    public void ShowNotification()
    {
        //先设定RemoteViews
        RemoteViews view_custom = new RemoteViews(getPackageName(), R.layout.activity_notification);
        //设置对应IMAGEVIEW的ID的资源图片
        //view_custom.setImageViewResource(R.id.custom_icon, R.mipmap.icon);
        view_custom.setTextViewText(R.id.upload_data, getString(R.string.incoming_speed) + _GetFormatSpeed(AppData.mIncomingSpeed));
        view_custom.setTextViewText(R.id.download_data, getString(R.string.outgoing_speed) + _GetFormatSpeed(AppData.mOutgoingSpeed));

        if(UserType.values()[AppData.mUserType] == UserType.Data)
        {
            DecimalFormat df = new DecimalFormat("0.00");
            view_custom.setTextViewText(R.id.user_data, df.format((double)(AppData.mRemainIncomingTraffic + AppData.mRemainOutgoingTraffic) / 1024 / 1024) + getString(R.string.remain_data));
        }
        else
        {
            view_custom.setTextViewText(R.id.user_data,  AppData.mUserExpirationTime + getString(R.string.to_date));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContent(view_custom);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());            //通知产生的时间，会在通知信息里显示
        builder.setPriority(Notification.PRIORITY_HIGH);        //设置该通知优先级

        //设置Notification.Default_ALL(默认启用全部服务(呼吸灯，铃声等)
        builder.setDefaults(Notification.DEFAULT_ALL);
        //获取Notification
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        //通过NotificationCompat.Builder.build()来获得notification对象自己
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //点击通知后的Intent，此例子点击后还是在当前界面
        Intent descIntent = new Intent(this, LoginActivity.class);
        PendingIntent intent = PendingIntent.getBroadcast(this, 0, descIntent, 0);//DEFAULT_SOUND;//默认声音提示
        builder.setContentIntent(intent);
        //发送通知
        manager.notify(0, notification);
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //更新速度条
                view_custom.setTextViewText(R.id.upload_data, getString(R.string.incoming_speed) + _GetFormatSpeed(AppData.mIncomingSpeed));
                view_custom.setTextViewText(R.id.download_data, getString(R.string.outgoing_speed) + _GetFormatSpeed(AppData.mOutgoingSpeed));
                builder.setContent(view_custom);
                manager.notify(0, notification);
                //计时器退出
                //this.cancel();
                //进度条退出
                //manager.cancel(0);
                //return;
            }
        }, 0);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(_StopHandler != null)
        {
            _StopHandler.removeCallbacks(_StopRunnable);
            //_StopHandler.removeCallbacks(_PingGoogleRunnable);
        }
        unregisterReceiver(vpnStateReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            //如果返回键按下
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 是否按照宽度进行等比例适配 (为了保证在高宽比不同的屏幕上也能正常适配, 所以只能在宽度和高度之中选择一个作为基准进行适配)
     *
     * @return {@code true} 为按照宽度进行适配, {@code false} 为按照高度进行适配
     */
    @Override
    public boolean isBaseOnWidth()
    {
        return false;
    }

    /**
     * 返回设计图上的设计尺寸, 单位 dp
     * {@link #getSizeInDp} 须配合 {@link #isBaseOnWidth()} 使用, 规则如下:
     * 如果 {@link #isBaseOnWidth()} 返回 {@code true}, {@link #getSizeInDp} 则应该返回设计图的总宽度
     * 如果 {@link #isBaseOnWidth()} 返回 {@code false}, {@link #getSizeInDp} 则应该返回设计图的总高度
     * 如果您不需要自定义设计图上的设计尺寸, 想继续使用在 AndroidManifest 中填写的设计图尺寸, {@link #getSizeInDp} 则返回 {@code 0}
     * @return 设计图上的设计尺寸, 单位 dp
     */
    @Override
    public float getSizeInDp()
    {
        return 640;
    }
}