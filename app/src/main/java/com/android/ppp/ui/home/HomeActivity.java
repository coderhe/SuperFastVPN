package com.android.ppp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.data.AppData;
import com.android.ppp.data.Settings;
import com.android.ppp.data.SettingsAdapter;
import com.android.ppp.data.ShopItem;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.login.LoginActivity;
import com.android.ppp.ui.shop.QueryOrderActivity;
import com.android.ppp.ui.shop.ShopActivity;
import com.android.ppp.ui.vpn.RoutesActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.AutoSizeConfig;

public class HomeActivity extends AppCompatActivity implements CustomAdapt
{
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

    private TextView txtDataInfo;
    private TextView txtDataTip;

    private TextView txtUseIncomingDataInfo;
    private TextView txtUseOutgoingDataInfo;

    private Timer mTimer;
    private WebView mWebView;
    //SettingsItem实体集合
    private List<Settings> mSettingsItems = new ArrayList<>();
    //小数点后两位
    private DecimalFormat mDecimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        // 初始化控件
        _InitView();
        // 初始化底部按钮事件
        _InitEvent();

        mDecimalFormat = new DecimalFormat("0.00");
        mWebView = findViewById(R.id.web_view);
        if(mWebView != null)
            mWebView.setVisibility(View.INVISIBLE);

        txtDataInfo = findViewById(R.id.date_text);
        txtDataTip = findViewById(R.id.date_text_tip);
        txtUseIncomingDataInfo = findViewById(R.id.use_incoming_data_text);
        txtUseOutgoingDataInfo = findViewById(R.id.use_outgoing_data_text);

        _ShowMemberVipTime();
        _RefreshUseDataSize();
        _UpdateDataSize();

        /*
        Button btnExitLogin = findViewById(R.id.exit_login_btn);
        btnExitLogin.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _TurnToLoginActivity();
                _FinishActivity();
            }
        });
        */
        TextView txtMailName = findViewById(R.id.mail_text);
        if(txtMailName != null && AppData.mUserEmail != null)
            txtMailName.setText(AppData.mUserEmail + _GetMemberData());

        _ShowItems();
        ImageButton btnPay = findViewById(R.id.pay_button);
        btnPay.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _FinishActivity();
                _TurnToShopActivity();
            }
        });

        ImageButton btnSettings = findViewById(R.id.settings_button);
        btnSettings.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _TurnToAppUseVpnSettingActivity();
            }
        });

        ImageButton btnPayRrecord = findViewById(R.id.pay_record_button);
        btnPayRrecord.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _TurnToQueryOrderActivity();
            }
        });

        ImageButton btnCoupon = findViewById(R.id.coupon_button);
        btnCoupon.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                Toast.makeText(getApplicationContext(), R.string.not_supply, Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton btnWeb = findViewById(R.id.protocol_button);
        btnWeb.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                if(mWebView != null)
                {
                    //mWebView.loadUrl(Config.STAR_LINK);
                    //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
                    mWebView.setWebViewClient(new WebViewClient()
                    {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url)
                        {
                            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                            view.loadUrl(Config.STAR_LINK);
                            return true;
                        }
                    });
                }
            }
        });
    }

    private String _GetMemberData()
    {
        int resId = 0;
        Config.UserType type = Config.UserType.values()[AppData.mUserType];
        if(type.equals(Config.UserType.Free))
        {
            resId = R.string.Free_Type;
        }
        else if(type.equals(Config.UserType.Time))
        {
            resId = R.string.Time_Type;
        }
        else if(type.equals(Config.UserType.Data))
        {
            resId = R.string.Data_Type;
        }
        else if(type.equals(Config.UserType.Inner))
        {
            resId = R.string.Inner_Type;
        }

        return getString(resId);
    }

    private void _ShowMemberVipTime()
    {
        if(txtDataInfo != null && txtDataTip != null)
        {
            Config.UserType type = Config.UserType.values()[AppData.mUserType];
            if(type.equals(Config.UserType.Free))
            {
                txtDataInfo.setText(AppData.mUserExpirationTime + getString(R.string.to_date));
                txtDataTip.setText(R.string.suit_last_date);
            }
            else if(type.equals(Config.UserType.Time))
            {
                txtDataInfo.setText(AppData.mUserExpirationTime + getString(R.string.to_date));
                txtDataTip.setText(R.string.suit_last_date);
            }
            else if(type.equals(Config.UserType.Data))
            {
                txtDataInfo.setText(mDecimalFormat.format((double)(AppData.mRemainIncomingTraffic + AppData.mRemainOutgoingTraffic) / 1024 / 1024) + getString(R.string.remain_data));
                txtDataTip.setText(R.string.suit_last_data);
            }
            else if(type.equals(Config.UserType.Inner))
            {
                txtDataInfo.setText(R.string.vip);
            }
        }
    }

    //如果用户类型是Data，刷新剩余数据流量
    private void _UpdateDataSize()
    {
        Config.UserType type = Config.UserType.values()[AppData.mUserType];
        if(mTimer == null)
        {
            mTimer = new Timer();
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
            mTimer.schedule(task, Config.LogScheudlerTimer, Config.LogScheudlerTimer);
        }
    }

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1: {
                    if(AppData.isConnect)
                    {
                        _RefreshRemainDataSize();
                        //刷新已用流量
                        _RefreshUseDataSize();
                    }
                    break;
                }
            }
        }
    };

    private void _RefreshRemainDataSize()
    {
        Config.UserType type = Config.UserType.values()[AppData.mUserType];
        if(type.equals(Config.UserType.Data) && txtDataInfo != null && mDecimalFormat != null)
        {
            txtDataInfo.setText(mDecimalFormat.format((double)(AppData.mRemainIncomingTraffic + AppData.mRemainOutgoingTraffic) / 1024 / 1024) + getString(R.string.remain_data));
        }
    }

    private void _RefreshUseDataSize()
    {
        if(txtUseIncomingDataInfo != null && mDecimalFormat != null)
        {
            txtUseIncomingDataInfo.setText(getString(R.string.use_incoming_data_size) + mDecimalFormat.format((double)(AppData.mUseIncomingTraffic) / 1024 / 1024) + getString(R.string.remain_data));
        }
        if(txtUseOutgoingDataInfo != null && mDecimalFormat != null)
        {
            txtUseOutgoingDataInfo.setText(getString(R.string.use_outgoing_data_size) + mDecimalFormat.format((double)(AppData.mUseOutgingTraffic) / 1024 / 1024) + getString(R.string.remain_data));
        }
    }

    //展示所有线路
    private void _ShowItems()
    {
        for (int i = 0; i < Config.allSettings.length; ++i)
        {
            mSettingsItems.add(new Settings(Config.allSettings[i], i));
        }

        //初始化适配器
        ListView listView = (ListView)findViewById(R.id.list_view);
        SettingsAdapter myAdapter = new SettingsAdapter(this, R.layout.settings_item, mSettingsItems);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Settings set = mSettingsItems.get(position);
                switch (set.getType())
                {
                    case 0:
                        _TurnToPacketActivity();
                        break;
                    case 1:
                        _TurnToDNSActivity();
                        break;
                    case 2:
                        _TurnToFullNatActivity();
                        break;
                    case 3:
                        _TurnToAgentSettingActivity();
                        break;
                    case 4:
                        _TurnToRouteSettingActivity();
                        break;
                }
                myAdapter.notifyDataSetChanged();
            }
        });
        listView.setAdapter(myAdapter);
    }

    private void _InitEvent()
    {
        // 设置按钮监听
        ll_change_ip.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v) {
                _FinishActivity();
                _TurnToRoutesActivity();
            }
        });
        ll_shopping.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v) {
                _FinishActivity();
                _TurnToShopActivity();
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

        iv_change_ip.setImageResource(R.mipmap.ic_connect_change_ip);
        tv_change_ip.setTextColor(0xffffffff);
        iv_shopping.setImageResource(R.mipmap.ic_connect_shopping);
        tv_shopping.setTextColor(0xffffffff);
        iv_home.setImageResource(R.mipmap.ic_connect_mine_green);
        tv_home.setTextColor(0xff1B940A);
    }

    private void _TurnToLoginActivity()
    {
        //关闭此activity，打开LoginActivity
        Intent it = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(it);
    }

    private void _TurnToRoutesActivity()
    {
        //关闭此activity，打开RoutesActivity
        Intent it = new Intent(getApplicationContext(), RoutesActivity.class);
        startActivity(it);
    }

    private void _TurnToShopActivity()
    {
        //关闭此activity，打开ShopActivity
        Intent it = new Intent(getApplicationContext(), ShopActivity.class);
        startActivity(it);
    }

    private void _TurnToQueryOrderActivity()
    {
        //关闭此activity，打开QueryOrderActivity
        Intent it = new Intent(getApplicationContext(), QueryOrderActivity.class);
        startActivity(it);
    }

    private void _TurnToPacketActivity()
    {
        //关闭此activity，打开PacketActivity
        Intent it = new Intent(getApplicationContext(), PacketActivity.class);
        startActivity(it);
    }

    private void _TurnToFullNatActivity()
    {
        //关闭此activity，打开FullNatActivity
        Intent it = new Intent(getApplicationContext(), FullNatActivity.class);
        startActivity(it);
    }

    private void _TurnToDNSActivity()
    {
        //关闭此activity，打开DnsSettingActivity
        Intent it = new Intent(getApplicationContext(), DnsSettingActivity.class);
        startActivity(it);
    }

    private void _TurnToAgentSettingActivity()
    {
        //关闭此activity，打开AgentSettingActivity
        Intent it = new Intent(getApplicationContext(), AgentSettingActivity.class);
        startActivity(it);
    }

    private void _TurnToRouteSettingActivity()
    {
        //关闭此activity，打开RouteSettingActivity
        Intent it = new Intent(getApplicationContext(), RouteSettingActivity.class);
        startActivity(it);
    }

    private void _TurnToAppUseVpnSettingActivity()
    {
        //关闭此activity，打开AppUseVpnSettingActivity
        Intent it = new Intent(getApplicationContext(), AppUseVPNSettingActivity.class);
        startActivity(it);
    }

    //关闭Activity
    private void _FinishActivity()
    {
        if (this != null)
            this.finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
        }

        if(mSettingsItems != null)
        {
            mSettingsItems.clear();
            mSettingsItems = null;
        }
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