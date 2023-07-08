package com.android.ppp.ui.shop;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.ppp.R;
import com.android.ppp.adapter.HttpUtils;
import com.android.ppp.config.Config;
import com.android.ppp.config.Status;
import com.android.ppp.data.AppData;
import com.android.ppp.data.OrderData;
import com.android.ppp.data.QueryOrderAdapters;
import com.android.ppp.ui.common.OnMultiClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.internal.CustomAdapt;
import okhttp3.Call;
import okhttp3.Response;

public class QueryOrderActivity extends AppCompatActivity implements CustomAdapt
{
    //Order实体集合
    private List<OrderData> pages;
    private List<List<OrderData>> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_order);

        //初始化ArrayList
        pages = new ArrayList<OrderData>();
        orders = new ArrayList<List<OrderData>>();
        if(AppData.mPayOrders == null || AppData.mPayOrders == "")
        {
            _HttpQueryPayOrder();
            while(true)
            {
                if(AppData.mPayOrders != null && AppData.mPayOrders != "")
                {
                    _ShowOrderItems();
                    break;
                }
            }
        }
        else
        {
            _ShowOrderItems();
        }

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

    //展示所有订单
    private void _ShowOrderItems()
    {
        try {
            JSONObject item = null;
            JSONObject json = new JSONObject(AppData.mPayOrders);
            int allCount = json.getInt("All");
            JSONArray server = json.getJSONArray("Rows");
            //ALL参数可以计算出总共多少页
            List<OrderData> itemsList = new ArrayList<OrderData>();
            for (int j = 0; j < server.length(); ++j)
            {
                itemsList.clear();
                item = server.getJSONObject(j);
                if(item != null)
                {
                    pages.add(new OrderData(item.getString("RowNo"), item.getString("Id"), item.getDouble("Money"), item.getInt("Status"), item.getString("CreationTime")));
                    itemsList.add(new OrderData(item.getString("RowNo"), item.getString("Id"), item.getDouble("Money"), item.getInt("Status"), item.getString("CreationTime")));
                    orders.add(itemsList);
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        //初始化适配器
        ExpandableListView listView = (ExpandableListView)findViewById(R.id.list_view);
        QueryOrderAdapters myAdapter = new QueryOrderAdapters(this, pages, orders);
        listView.setGroupIndicator(null);
        //分组展开
        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener(){
            public void onGroupExpand(int groupPosition) {
                //listView.expandGroup(groupPosition);
            }
        });
        //分组关闭
        listView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener(){
            public void onGroupCollapse(int groupPosition) {
                //listView.collapseGroup(groupPosition);
            }
        });
        listView.setAdapter(myAdapter);
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