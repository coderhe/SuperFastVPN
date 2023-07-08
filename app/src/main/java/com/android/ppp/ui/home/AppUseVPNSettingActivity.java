package com.android.ppp.ui.home;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.data.AppData;
import com.android.ppp.data.AppItem;
import com.android.ppp.data.AppItemAdapter;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.data.RouteSetingAdapters;
import com.android.ppp.data.ShopItemAdapter;
import com.android.ppp.ui.common.OnMultiClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.jessyan.autosize.internal.CustomAdapt;

public class AppUseVPNSettingActivity extends AppCompatActivity implements CustomAdapt
{
    private ImageButton btnAllowUseVpnAppSetting;
    private ImageButton sureButton;
    private ImageButton chinaIPSettingsButton;
    private AppItemAdapter myAdapter;
    private ListView appListView;

    //列表实体集合
    private List<AppItem> appItems = new ArrayList<>();
    private List<ApplicationInfo> appsInPhone;
    private List<AppItem> mSelectedApps;

    private boolean isAppOpen = false;
    private boolean isBypassChina = true;
    private boolean isChangeSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_use_vpn_setting);

        isChangeSetting = false;
        final ImageButton exitButton = findViewById(R.id.exit_btn);
        exitButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _FinishActivity();
            }
        });

        chinaIPSettingsButton = findViewById(R.id.china_ip_setting_button);
        AppData.mIpTxt = (String)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "addressSeparation");
        isBypassChina = AppData.mIpTxt != null && !AppData.mIpTxt.isEmpty();
        chinaIPSettingsButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                isBypassChina = !isBypassChina;
                _UpdateSelect();
            }
        });

        sureButton = findViewById(R.id.sure_button);
        sureButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _ModifySettings();
                _FinishActivity();
            }
        });

        appsInPhone = _QueryFilterAppInfo();
        btnAllowUseVpnAppSetting = findViewById(R.id.allow_use_vpn_app_setting_button);
        btnAllowUseVpnAppSetting.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                isChangeSetting = true;
                isAppOpen = !isAppOpen;
                if(isAppOpen == false && myAdapter != null)
                {
                    appItems.clear();
                    _ShowPhoneAppsNull();
                }
                else
                    _ShowPhoneApps();

                _UpdateSelect();
            }
        });

        _UpdateSelect();
    }

    private void _ShowPhoneAppsNull()
    {
        //初始化适配器
        appListView = (ListView)findViewById(R.id.app_list_view);
        //初始化适配器
        myAdapter = new AppItemAdapter(this, R.layout.app_region_router_item, appItems, null);
        appListView.setAdapter(myAdapter);
    }

    private void _ShowPhoneApps()
    {
        AppItem appItem = null;
        mSelectedApps = new ArrayList<>();
        for (int i = 0; i < appsInPhone.size(); i++)
        {
            String name = appsInPhone.get(i).loadLabel(getPackageManager()).toString();
            //String appInfoStr = name + " : " + apps.get(i).packageName;
            String packageName = appsInPhone.get(i).packageName;
            Drawable icon = appsInPhone.get(i).loadIcon(getPackageManager());
            appItem = new AppItem(name, packageName, icon);
            appItems.add(appItem);
            if(AppData.allowUseVpnPackageNames.contains(packageName))
            {
                mSelectedApps.add(appItem);
            }
        }

        //初始化适配器
        appListView = (ListView)findViewById(R.id.app_list_view);
        //初始化适配器
        myAdapter = new AppItemAdapter(this, R.layout.app_region_router_item, appItems, mSelectedApps);
        appListView.setAdapter(myAdapter);
    }

    private List<ApplicationInfo> _QueryFilterAppInfo()
    {
        String packageName = getPackageName();
        PackageManager pm = this.getPackageManager();
        // 查询所有已经安装的应用程序, GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
        List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        List<ApplicationInfo> applicationInfos = new ArrayList<>();

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        //通过getPackageManager()的queryIntentActivities方法遍历, 得到所有能打开的app的packageName
        List<ResolveInfo> resolveinfoList = getPackageManager().queryIntentActivities(resolveIntent, 0);
        Set<String> allowPackages = new HashSet();
        for (ResolveInfo resolveInfo : resolveinfoList)
        {
            allowPackages.add(resolveInfo.activityInfo.packageName);
        }

        for (ApplicationInfo app : appInfos)
        {
            //通过flag排除系统应用，会将电话、短信也排除掉
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM &&
                    app.packageName.equals(packageName) == false &&
                    (app.packageName.contains("google") ||
                     app.packageName.contains("chrome") ||
                     app.packageName.contains("vending")))
            {
                applicationInfos.add(app);
            }

            if(allowPackages.contains(app.packageName) && app.packageName.equals(packageName) == false)
            {
                if(applicationInfos.contains(app) == false)
                    applicationInfos.add(app);
            }
        }

        return applicationInfos;
    }

    private void _SetSureButtonVisible()
    {
        if(sureButton != null)
            sureButton.setVisibility(isChangeSetting ? View.VISIBLE : View.INVISIBLE);
    }

    private void _UpdateSelect()
    {
        _SetSureButtonVisible();

        if(btnAllowUseVpnAppSetting != null)
            btnAllowUseVpnAppSetting.setImageResource(isAppOpen ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);

        if(chinaIPSettingsButton != null)
            chinaIPSettingsButton.setImageResource(isBypassChina ? R.mipmap.ic_switch : R.mipmap.ic_switch_bg);
    }

    private void _ModifySettings()
    {
        if(myAdapter == null)
            return;

        AppData.allowUseVpnPackageNames.clear();
        List<AppItem> apps = myAdapter.GetSelectedApps();
        for(int i = 0; i < apps.size(); ++i)
        {
            AppData.allowUseVpnPackageNames.add(apps.get(i).getPackageName());
        }

        AppData.mIpTxt = isBypassChina ? "./data/data/com.android.ppp/cache/ip.txt" : "";
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "addressSeparation", AppData.mIpTxt);
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

        appListView = null;
        myAdapter = null;
        if(appItems != null)
        {
            appItems.clear();
            appItems = null;
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