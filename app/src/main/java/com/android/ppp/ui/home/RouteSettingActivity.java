package com.android.ppp.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
import com.android.ppp.data.RouteSetingAdapters;
import com.android.ppp.ui.common.OnMultiClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jessyan.autosize.internal.CustomAdapt;

public class RouteSettingActivity extends AppCompatActivity implements CustomAdapt
{
    private ImageButton btnAllowGlobalSetting;
    private ImageButton sureButton;
    private ExpandableListView listView;
    private ExpandableListView regionListView;

    private boolean allowGlobalSetting;
    private String mSelfDefineRoute;

    //列表实体集合
    private List<String> services = new ArrayList<>();
    private List<List<String>> datas = new ArrayList<List<String>>();
    private List<Integer> mSelectedRegions = new ArrayList<>();

    private RouteSetingAdapters mRouteSetingAdapters;

    private boolean isSelfDefineOPen = false;
    private boolean isRegionOpen = false;
    private boolean isChangeSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_settings);

        isChangeSetting = false;
        mSelfDefineRoute = "";
        _ShowRouteItems();
        _ShowRegionItems();
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

        allowGlobalSetting = AppData.isAllowGlobalRoute;
        btnAllowGlobalSetting = findViewById(R.id.allow_global_setting_button);
        btnAllowGlobalSetting.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                if(isSelfDefineOPen)
                {
                    Toast.makeText(getApplicationContext(), R.string.already_select_self_define_route, Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isRegionOpen)
                {
                    Toast.makeText(getApplicationContext(), R.string.already_select_region_route, Toast.LENGTH_SHORT).show();
                    return;
                }

                isChangeSetting = true;
                allowGlobalSetting = !allowGlobalSetting;
                _UpdateSelect();
            }
        });

        _UpdateSelect();
    }

    public void InputCorrectSelfDefineRouteIP(String ip)
    {
        mSelfDefineRoute = ip;
    }

    //输入的ip不对
    public void ShowErrorInputIp()
    {
        Toast.makeText(getApplicationContext(), R.string.router_ip_invalid, Toast.LENGTH_SHORT).show();
    }

    public void ShowErrorInputIp1()
    {
        Toast.makeText(getApplicationContext(), R.string.router_ip_not_correct, Toast.LENGTH_SHORT).show();
    }

    public void ShowErrorInputPort()
    {
        Toast.makeText(getApplicationContext(), R.string.router_ip_wrong, Toast.LENGTH_SHORT).show();
    }

    //展示所有代理port
    private void _ShowRouteItems()
    {
        List<String> selfServices = new ArrayList<>();
        List<List<String>> selfDatas = new ArrayList<List<String>>();
        int index = 0;
        String name = Config.allRouteSettings[index];
        selfServices.add(name);

        List<String> itemsList = new ArrayList<>();
        if(AppData.mSelfDefineRouteAddress != "")
            itemsList.add(AppData.mSelfDefineRouteAddress);
        else
            itemsList.add("");

        selfDatas.add(itemsList);

        //初始化适配器
        listView = (ExpandableListView)findViewById(R.id.list_view);
        RouteSetingAdapters myAdapter = new RouteSetingAdapters(this, selfServices, selfDatas, index);
        listView.setAdapter(myAdapter);
    }

    //展示所有代理port
    private void _ShowRegionItems()
    {
        int index = 1;
        String name = Config.allRouteSettings[index];
        services.add(name);
        List<String> itemsList = new ArrayList<>();
        {
            if(AppData.regionRouterSettings == null)
                AppData.regionRouterSettings = JsonUtils.ReadAssetJson(RouteSettingActivity.this, "countryroutersettings.json");

            try {
                JSONObject obj = new JSONObject(AppData.regionRouterSettings);
                JSONArray array = obj.getJSONArray("region_routers");
                JSONObject regionObj;
                for (int k = 0; k < array.length(); ++k)
                {
                    regionObj = array.getJSONObject(k);
                    itemsList.add(regionObj.getString("Name"));
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        datas.add(itemsList);
        //初始化适配器
        regionListView = (ExpandableListView)findViewById(R.id.region_list_view);
        mRouteSetingAdapters = new RouteSetingAdapters(this, services, datas, index);
        regionListView.setAdapter(mRouteSetingAdapters);
    }

    public void SetListner(ImageButton btnSwtich, int index, int groupPosition)
    {
        if(!isChangeSetting)
        {
            isChangeSetting = true;
            _SetSureButtonVisible();
        }

        if(allowGlobalSetting)
        {
            Toast.makeText(getApplicationContext(), R.string.already_select_global_route, Toast.LENGTH_SHORT).show();
            return;
        }

        if(index == 0)
        {
            isSelfDefineOPen = !isSelfDefineOPen;
            btnSwtich.setImageResource(isSelfDefineOPen ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
            _SetListViewState(isSelfDefineOPen, index, groupPosition);
        }
        else if(index == 1)
        {
            isRegionOpen = !isRegionOpen;
            if(isRegionOpen == false)
                mRouteSetingAdapters.ClearSelectRegions();

            btnSwtich.setImageResource(isRegionOpen ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
            _SetListViewState(isRegionOpen, index, groupPosition);
        }
    }

    private void _SetListViewState(boolean open, int index, int groupPosition)
    {
        if(index == 0)
        {
            if(open)
                listView.expandGroup(groupPosition);
            else
                listView.collapseGroup(groupPosition);
        }
        else if(index == 1)
        {
            if(open)
                regionListView.expandGroup(groupPosition);
            else
                regionListView.collapseGroup(groupPosition);
        }
    }

    private void _SetSureButtonVisible()
    {
        if(sureButton != null)
            sureButton.setVisibility(isChangeSetting ? View.VISIBLE : View.INVISIBLE);
    }

    private void _UpdateSelect()
    {
        _SetSureButtonVisible();

        if(btnAllowGlobalSetting != null)
            btnAllowGlobalSetting.setImageResource(allowGlobalSetting ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
    }

    private void _ModifySettings()
    {
        AppData.isAllowGlobalRoute = allowGlobalSetting;

        if(mRouteSetingAdapters != null)
        {
            AppData.selectRegions = mRouteSetingAdapters.getSelectRegions();

            if(AppData.selectRegions.contains(this.getString(R.string.All)))
            {
                //AppData.allRegionRouterInfos = JsonUtils.ReadAssetJson(this, "All.txt").split("\n");
            }
        }

        if(mSelfDefineRoute != null && mSelfDefineRoute != "")
            AppData.mSelfDefineRouteAddress = mSelfDefineRoute;
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
        regionListView = null;
        mRouteSetingAdapters = null;
        if(services != null)
        {
            services.clear();
            services = null;
        }

        if(datas != null)
        {
            datas.clear();
            datas = null;
        }

        if(mSelectedRegions != null)
        {
            mSelectedRegions.clear();
            mSelectedRegions = null;
        }
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