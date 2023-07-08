package com.android.ppp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.ui.common.OnMultiClickListener;

import me.jessyan.autosize.internal.CustomAdapt;

public class PacketActivity extends AppCompatActivity implements CustomAdapt
{
    private ImageButton btnLan;
    private ImageButton btnWan;
    private ImageButton sureButton;

    private boolean isLan;
    private boolean isWan;
    private boolean isChangeSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packet);

        isLan = (boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fastSendPacket", "lan");
        isWan = (boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fastSendPacket", "wan");
        isChangeSetting = false;
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
        btnLan = findViewById(R.id.lan_button);
        btnLan.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                isChangeSetting = true;
                isLan = !isLan;
                _UpdateSelect();
            }
        });

        btnWan = findViewById(R.id.wan_button);
        btnWan.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                isChangeSetting = true;
                isWan = !isWan;
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

        if(btnLan != null && btnWan != null)
        {
            btnLan.setImageResource(isLan ? R.mipmap.ic_shop_checkbox_selected : R.mipmap.ic_shop_checkbox_select);
            btnWan.setImageResource(isWan ? R.mipmap.ic_shop_checkbox_selected : R.mipmap.ic_shop_checkbox_select);
        }
    }

    private void _ModifySettings()
    {
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fastSendPacket", "lan", isLan);
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fastSendPacket", "wan", isWan);
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