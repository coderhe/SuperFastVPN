package com.android.ppp.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.data.AppData;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.ui.common.OnMultiClickListener;

import me.jessyan.autosize.internal.CustomAdapt;

public class FullNatActivity extends AppCompatActivity implements CustomAdapt
{
    private ImageButton btnFullNat;
    private ImageButton sureButton;

    private boolean is_full_nat;
    private boolean isChangeSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_nat);

        isChangeSetting = false;
        is_full_nat = (boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fullNatMode");
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

        btnFullNat = findViewById(R.id.full_nat_button);
        btnFullNat.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                if(AppData.mCurRouteFullnat == 0 && is_full_nat == false)
                {
                    Toast.makeText(getApplicationContext(), R.string.full_nat_warning, Toast.LENGTH_SHORT).show();
                    return;
                }
                isChangeSetting = true;
                is_full_nat = !is_full_nat;
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

        btnFullNat.setImageResource(is_full_nat ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
    }

    private void _ModifySettings()
    {
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "fullNatMode", is_full_nat);
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