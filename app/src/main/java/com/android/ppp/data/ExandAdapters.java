package com.android.ppp.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.vpn.SelectRouteActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ExandAdapters extends BaseExpandableListAdapter
{
    private List<Route> groupList;
    private List<List<Route>> childList;
    private LayoutInflater inflater;
    private SelectRouteActivity mActivity;

    private List<Integer> selectItem;

    public ExandAdapters(Context context, List<Route> groupList, List<List<Route>> childList)
    {
        this.inflater = LayoutInflater.from(context);
        this.groupList = groupList;
        this.childList = childList;
        selectItem = new ArrayList<>();
        mActivity = (SelectRouteActivity)context;
    }

    public void setSelection(int position)
    {
        if(selectItem.contains(position))
            selectItem.remove(position);
        else
            selectItem.add(position);
    }

    //分组总数
    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    //某分组下子属性数
    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(groupPosition).size();
    }

    //分组对象
    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    //子属性对象
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(groupPosition).get(childPosition);
    }

    //分组的id
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    //子item的id
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean b, View view, ViewGroup viewGroup)
    {
        Route route = groupList.get(groupPosition);
        ViweHolder holder = null;
        if (view == null) {
            holder = new ViweHolder();
            view = inflater.inflate(R.layout.region_item, null);
            holder.regionBg = (ImageView)view.findViewById(R.id.region_bg);
            holder.groupName = (TextView)view.findViewById(R.id.region_name);
            holder.selectIcon = (ImageView)view.findViewById(R.id.iv_group);

            view.setTag(holder);
        } else {
            holder = (ViweHolder) view.getTag();
        }

        holder.regionBg.setVisibility(AppData.mCurRouteRegion != null && AppData.mCurRouteRegion.equals(route.getRegion()) ? View.VISIBLE : View.INVISIBLE);
        holder.groupName.setText(route.getName());
        if (selectItem.contains(groupPosition))
            holder.selectIcon.setImageResource(R.mipmap.ic_grey_arrow_down);

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup)
    {
        Route route = childList.get(groupPosition).get(childPosition);
        RouteViewHolder viewHolder = null;
        if(view == null)
        {
            view = inflater.inflate(R.layout.route_item, null);
            viewHolder = new RouteViewHolder();
            viewHolder.routeBg = (ImageView)view.findViewById(R.id.route_bg);
            viewHolder.regionIcon = (ImageView)view.findViewById(R.id.region_icon);
            viewHolder.routeName = (TextView)view.findViewById(R.id.route_name);
            viewHolder.delayTime = (TextView)view.findViewById(R.id.route_delay_time);
            viewHolder.wifiIcon = (ImageView)view.findViewById(R.id.wifi_icon);
            //将ViewHolder存储在View中
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (RouteViewHolder)view.getTag();
        }

        viewHolder.routeBg.setVisibility(AppData.mCurRouteName != null && AppData.mCurRouteName.equals(route.getName()) ? View.VISIBLE : View.INVISIBLE);
        viewHolder.regionIcon.setImageResource(route.getIcon());
        viewHolder.routeName.setText(route.getName());
        viewHolder.delayTime.setText(route.getPingDelay() + "ms");
        viewHolder.wifiIcon.setImageDrawable(_GetWifiDrawable(route.getPingDelay()));
        /*
        viewHolder.btnSelect.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                mActivity.SetSelectedRoute(route);
            }
        });
        */
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;//子item是否响应
    }

    private Drawable _GetWifiDrawable(String delayTime)
    {
        float time = AppData.String2Float(delayTime);
        if(time > 0f && time <= 50f)
            return mActivity.getResources().getDrawable(R.mipmap.wifi_5);
        else if(time > 50f && time <= 80f)
            return mActivity.getResources().getDrawable(R.mipmap.wifi_4);
        else if(time > 80f && time <= 150f)
            return mActivity.getResources().getDrawable(R.mipmap.wifi_3);
        else if(time > 150f && time <= 500f)
            return mActivity.getResources().getDrawable(R.mipmap.wifi_2);
        else if(time > 500f && time <= 1000f)
            return mActivity.getResources().getDrawable(R.mipmap.wifi_1);
        else
            return mActivity.getResources().getDrawable(R.mipmap.wifi_0);
    }

    class ViweHolder
    {
        ImageView regionBg;
        TextView groupName;
        ImageView selectIcon;
    }

    class RouteViewHolder
    {
        ImageView routeBg;
        ImageView regionIcon;
        TextView routeName;
        TextView delayTime;
        ImageView wifiIcon;
    }
}