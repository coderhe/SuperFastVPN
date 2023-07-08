package com.android.ppp.ui.home;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.data.AppData;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.data.PPPVpnService;
import com.android.ppp.ui.common.OnMultiClickListener;

import java.util.Timer;
import java.util.TimerTask;

import me.jessyan.autosize.internal.CustomAdapt;

public class DnsSettingActivity extends AppCompatActivity implements CustomAdapt
{
    private ImageButton btnDnsHijack;
    private ImageButton btnDnsResolve;
    private ImageButton sureButton;
    private EditText dnsServerEditText1;
    private EditText dnsServerEditText2;

    private boolean dnsLocalHijack;
    private boolean dnsLocalResolve;

    public static String mDNSServerAddresses[] = {"1.1.1.1", "8.8.8.8"};
    private static final int VPN_REQUEST_CODE = 0x1F;
    private boolean isChangeSetting = false;
    //app当前连接状态
    private boolean bOldConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dns_settings);

        isChangeSetting = false;
        bOldConnect = false;
        dnsServerEditText1 = findViewById(R.id.dns_server1_edtext);
        dnsServerEditText2 = findViewById(R.id.dns_server2_edtext);
        mDNSServerAddresses = AppData.mDNSServerAddresses;
        if(mDNSServerAddresses[0] != Config.defaultDnsAddresses[0])
            dnsServerEditText1.setText(mDNSServerAddresses[0]);

        if(mDNSServerAddresses[1] != Config.defaultDnsAddresses[1])
            dnsServerEditText2.setText(mDNSServerAddresses[1]);

        dnsServerEditText1.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    isChangeSetting = true;
                    String address = dnsServerEditText1.getText().toString();
                    if(AppData.IsIPAddressByRegex(address))
                    {
                        if(mDNSServerAddresses[0] != address)
                            mDNSServerAddresses[0] = address;
                    }
                    else
                        Toast.makeText(getApplicationContext(), R.string.dns_server_address_warning, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        dnsServerEditText2.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    isChangeSetting = true;
                    String address = dnsServerEditText2.getText().toString();
                    if(AppData.IsIPAddressByRegex(address))
                    {
                        if(mDNSServerAddresses[1] != address)
                            mDNSServerAddresses[1] = address;
                    }
                    else
                        Toast.makeText(getApplicationContext(), R.string.dns_server_address_warning, Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

        dnsLocalHijack = (boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "dnsHijacking");
        dnsLocalResolve = (boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "dnsLocalResolve");
        final ImageButton exitButton = findViewById(R.id.exitbtn);
        exitButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _FinishActivity();
            }
        });
        sureButton = findViewById(R.id.sure_button);
        sureButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _ModifySettings();
                _FinishActivity();
            }
        });

        btnDnsHijack = findViewById(R.id.hijack_button);
        btnDnsHijack.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                isChangeSetting = true;
                dnsLocalHijack = !dnsLocalHijack;
                _UpdateSelect();
            }
        });
        btnDnsResolve = findViewById(R.id.parse_button);
        btnDnsResolve.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                isChangeSetting = true;
                dnsLocalResolve = !dnsLocalResolve;
                _UpdateSelect();
            }
        });
        _UpdateSelect();
    }

    private void _SetSureButtonVisible()
    {
        if(sureButton != null)
            sureButton.setVisibility(isChangeSetting ? View.VISIBLE : View.INVISIBLE);
    }

    private void _UpdateSelect()
    {
        _SetSureButtonVisible();

        if(btnDnsHijack != null)
            btnDnsHijack.setImageResource(dnsLocalHijack ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);

        if(btnDnsResolve != null)
            btnDnsResolve.setImageResource(dnsLocalResolve ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
    }

    private void _ModifySettings()
    {
        //有线路在连接即立即断开连接，参数重设置后再开启连接
        if(AppData.isConnect && AppData.IsCanUseVpnService())
        {
            bOldConnect = true;
            AppData.isConnect = false;
            sendBroadcast(new Intent(PPPVpnService.BROADCAST_STOP_VPN));
        }
        AppData.mDNSServerAddresses = mDNSServerAddresses;
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "dnsHijacking", dnsLocalHijack);
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "dnsLocalResolve", dnsLocalResolve);
        //延迟开启VPNService服务
        if(bOldConnect && AppData.isConnect == false && AppData.IsCanUseVpnService())
        {
            Timer timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    _StartVPNService();
                    //计时器退出
                    this.cancel();
                }
            }, 0);
        }
    }

    private void _StartVPNService()
    {
        Intent vpnIntent = VpnService.prepare(this);
        if (vpnIntent != null)
        {
            startActivityForResult(vpnIntent, VPN_REQUEST_CODE);
        }
        else
        {
            AppData.isConnect = true;
            startService(new Intent(this, PPPVpnService.class));
        }
    }

    //保存修改，关闭Activity
    private void _FinishActivity()
    {
        if (this != null)
            this.finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public boolean isBaseOnWidth()
    {
        return false;
    }

    @Override
    public float getSizeInDp()
    {
        return 640;
    }
}