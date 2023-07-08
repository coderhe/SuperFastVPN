package com.android.ppp.ui.home;

import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.data.AgentPortAdapters;
import com.android.ppp.data.AppData;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.data.PortData;
import com.android.ppp.data.Route;
import com.android.ppp.ui.common.OnMultiClickListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.internal.CustomAdapt;

public class AgentSettingActivity extends AppCompatActivity implements CustomAdapt
{
    private ImageButton btnAllowLanConnect;
    private ImageButton sureButton;
    private ExpandableListView listView;

    private boolean allowLanConnect;
    private int webPort;
    private int socks5Port;
    //列表实体集合
    private List<String> agents = new ArrayList<>();
    private List<List<PortData>> ports = new ArrayList<List<PortData>>();

    private boolean isWebOpen = false;
    private boolean isSock5Open = false;
    private boolean isChangeSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_settings);

        isChangeSetting = false;
        webPort = (int)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "webProxy", "port");
        socks5Port = (int)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "socks5Agent", "port");

        _ShowAgentPortItems();
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
        allowLanConnect = AppData.isAllowLanConnect;
        btnAllowLanConnect = findViewById(R.id.allow_wan_connect_button);
        btnAllowLanConnect.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                isChangeSetting = true;
                allowLanConnect = !allowLanConnect;
                _UpdateSelect();
            }
        });

        _UpdateSelect();
    }

    public void InputCorrectPort(int index, int number)
    {
        if(index == 0)
            webPort = number;
        else
            socks5Port = number;
    }

    public void ShowErrorPort()
    {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.port_warning_contains_other, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    public void ShowErrorPort1()
    {
        Toast toast = Toast.makeText(getApplicationContext(), R.string.port_warning, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    //展示所有代理port
    private void _ShowAgentPortItems()
    {
        String name = null;
        for (int i = 0; i < Config.allAgentPortSettings.length; ++i)
        {
            name = Config.allAgentPortSettings[i];
            agents.add(name);

            List<PortData> itemsList = new ArrayList<>();
            if(i == 0)
            {
                if(webPort == Config.allAgentPorts[0])
                    itemsList.add(new PortData(0, R.string.default_port_1, 0));
                else
                    itemsList.add(new PortData(1, 0, webPort));
            }
            else
            {
                if(socks5Port == Config.allAgentPorts[1])
                    itemsList.add(new PortData(0, R.string.default_port_2, 0));
                else
                    itemsList.add(new PortData(1, 0, socks5Port));
            }

            ports.add(itemsList);
        }
        //初始化适配器
        listView = (ExpandableListView)findViewById(R.id.list_view);
        AgentPortAdapters myAdapter = new AgentPortAdapters(this, agents, ports);
        listView.setAdapter(myAdapter);
    }

    public void SetListner(ImageButton btnSwtich, int groupPosition)
    {
        if(!isChangeSetting)
        {
            isChangeSetting = true;
            _SetSureButtonVisible();
        }

        if(groupPosition == 0)
        {
            isWebOpen = !isWebOpen;
            btnSwtich.setImageResource(isWebOpen ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
            _SetListViewState(isWebOpen, groupPosition);
        }
        else
        {
            isSock5Open = !isSock5Open;
            btnSwtich.setImageResource(isSock5Open ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
            _SetListViewState(isSock5Open, groupPosition);
        }
    }

    private void _SetListViewState(boolean open, int groupPosition)
    {
        if(open)
            listView.expandGroup(groupPosition);
        else
            listView.collapseGroup(groupPosition);
    }

    private void _SetSureButtonVisible()
    {
        if(sureButton != null)
            sureButton.setVisibility(isChangeSetting ? View.VISIBLE : View.INVISIBLE);
    }

    private void _UpdateSelect()
    {
        _SetSureButtonVisible();

        if(btnAllowLanConnect != null)
            btnAllowLanConnect.setImageResource(allowLanConnect ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
    }

    private void _ModifySettings()
    {
        AppData.isAllowLanConnect = allowLanConnect;
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "webProxy", "port", webPort);
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "socks5Agent", "port", socks5Port);
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

        listView = null;
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