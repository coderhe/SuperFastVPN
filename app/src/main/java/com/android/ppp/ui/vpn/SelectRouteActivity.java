package com.android.ppp.ui.vpn;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.data.AppData;
import com.android.ppp.data.ExandAdapters;
import com.android.ppp.data.Route;
import com.android.ppp.data.RouteData;
import com.android.ppp.ui.common.OnMultiClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.jessyan.autosize.internal.CustomAdapt;

public class SelectRouteActivity extends AppCompatActivity implements CustomAdapt
{
    //Route实体集合
    private List<Route> regions;
    private List<List<Route>> routes;
    private Route mSelctedRoute;
    private ExpandableListView listView;
    private ExandAdapters myAdapter;
    private int expandIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_route);

        //初始化ArrayList
        regions = new ArrayList<Route>();
        routes = new ArrayList<List<Route>>();
        _ShowRouteItems();

        final ImageButton exitButton = findViewById(R.id.exitbtn);
        exitButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _FinishActivity();
            }
        });
    }

    //展示所有线路
    private void _ShowRouteItems()
    {
        try {
            JSONObject item = null;
            JSONArray json = new JSONArray(RouteData.Routes);
            String region = "";
            Route route = null;

            for (int i = 0; i < json.length(); ++i)
            {
                item = json.getJSONObject(i);
                JSONArray server = item.getJSONArray("Servers");
                region = item.getString("Code");
                String regionName = item.getString("Name");
                route = new Route(regionName, region, "", "", 0, 0, 0, Route.REGION_TYPE);
                regions.add(route);

                List<Route> itemsList = new ArrayList<Route>();
                for (int j = 0; j < server.length(); ++j)
                {
                    item = server.getJSONObject(j);
                    if(item != null)
                    {
                        route = new Route(item.getString("Name"), region,
                                item.getString("Addresses"),
                                item.getString("Cipher"),
                                item.getInt("FullNat"),
                                item.getInt("Multiple"),
                                item.getInt("QoS"),
                                Route.ROUTE_TYPE);
                        itemsList.add(route);
                    }
                }
                routes.add(itemsList);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        //初始化适配器
        listView = (ExpandableListView)findViewById(R.id.list_view);
        myAdapter = new ExandAdapters(this, regions, routes);
        //listView.setGroupIndicator(null);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view,
                                        int groupPosition, int childPosition, long id)
            {
                Route route = (Route)myAdapter.getChild(groupPosition, childPosition);
                SetSelectedRoute(route);
                return true;
            }
        });
        /*
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener()
        {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view,
                                        int groupPosition, long id)
            {
                myAdapter.setSelection(groupPosition);
                myAdapter.notifyDataSetChanged();
                return true;
            }
        });
         */
        //分组展开
        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener()
        {
            @Override
            public void onGroupExpand(int groupPosition) {
                //listView.expandGroup(groupPosition);
                expandIndex = groupPosition;
            }
        });
        //分组关闭
        listView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener()
        {
            @Override
            public void onGroupCollapse(int groupPosition) {
                //listView.collapseGroup(groupPosition);
                expandIndex = -1;
            }
        });
        listView.setAdapter(myAdapter);
        _UpdateRoutePingDelay();
    }

    private void _UpdateRoutePingDelay()
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                if(expandIndex != -1)
                {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            }
        };
        timer.schedule(task, Config.LogScheudlerTimer, Config.LogScheudlerTimer);
    }

    private Handler handler = new Handler()
    {
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    myAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public void SetSelectedRoute(Route route)
    {
        mSelctedRoute = route;
        Intent data = new Intent();
        data.putExtra("name", mSelctedRoute.getName());
        data.putExtra("address", mSelctedRoute.getAddresses());
        data.putExtra("region", mSelctedRoute.getRegion());
        data.putExtra("cipher", mSelctedRoute.getCipher());
        data.putExtra("fullnat", mSelctedRoute.getFullNat());
        data.putExtra("multiple", mSelctedRoute.getMultiple());
        data.putExtra("qos", mSelctedRoute.getQos());
        //设置结果码和意图对象,会将这些值在当前Activity销毁后返回到激活当前Activity的Activity中
        this.setResult(RESULT_OK, data);

        _FinishActivity();
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