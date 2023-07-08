package com.android.ppp.data;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.ppp.R;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.home.RouteSettingActivity;

import java.util.ArrayList;
import java.util.List;

public class RouteSetingAdapters extends BaseExpandableListAdapter
{
    private List<String> groupList;
    private List<List<String>> childList;
    private LayoutInflater inflater;
    private RouteSettingActivity mActivity;
    private List<String> mSelectedRegions;

    private int nCurIndex;
    private String sAll;

    public RouteSetingAdapters(Context context, List<String> groupList, List<List<String>> childList, int index)
    {
        this.inflater = LayoutInflater.from(context);
        this.groupList = groupList;
        this.childList = childList;
        nCurIndex = index;
        mActivity = (RouteSettingActivity)context;
        if(AppData.selectRegions != null)
            mSelectedRegions = AppData.selectRegions;

        sAll = mActivity.getString(R.string.All);
    }

    //获取选中地区
    public List<String> getSelectRegions() {
        return mSelectedRegions;
    }

    public void ClearSelectRegions()
    {
        if(mSelectedRegions != null)
            mSelectedRegions.clear();
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
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.agent_item, null);
            holder.name = (TextView) view.findViewById(R.id.agent_name);
            holder.btnSelect = (ImageButton) view.findViewById(R.id.agent_select);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.name.setText(groupList.get(groupPosition));
        ViewHolder finalHolder = holder;
        holder.btnSelect.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                mActivity.SetListner(finalHolder.btnSelect, nCurIndex, groupPosition);
            }
        });

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View convertView, ViewGroup viewGroup)
    {
        String data = childList.get(groupPosition).get(childPosition);
        View view = null;
        if(nCurIndex == 0)
        {
            PortViewHolder viewHolder = null;
            if(convertView == null)
            {
                view = this.inflater.inflate(R.layout.port_item_1,null);
                viewHolder = new PortViewHolder();
                viewHolder.txtPort = (EditText)view.findViewById(R.id.port_text);
                //将ViewHolder存储在View中
                view.setTag(viewHolder);
            }
            else {
                view = convertView;
                viewHolder = (PortViewHolder)view.getTag();
            }

            if(data != "")
                viewHolder.txtPort.setText(data);

            PortViewHolder finalViewHolder = viewHolder;
            viewHolder.txtPort.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                {
                    if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        String ip = finalViewHolder.txtPort.getText().toString();
                        String[] ipData = ip.split("/");
                        if(ipData.length == 2)
                        {
                            if (AppData.IsIPAddressByRegex(ipData[0]) == false)
                                mActivity.ShowErrorInputIp1();
                            else if (AppData.IsDigit(ipData[1]) == false)
                                mActivity.ShowErrorInputPort();
                            else
                                mActivity.InputCorrectSelfDefineRouteIP(ip);
                        }
                        else
                            mActivity.ShowErrorInputIp();
                    }

                    return false;
                }
            });
        }
        else if(nCurIndex == 1)
        {
            RegionViewHolder viewHolder = null;
            if(convertView == null)
            {
                view = inflater.inflate(R.layout.region_router_item, null);
                viewHolder = new RegionViewHolder();
                viewHolder.txtName = (TextView)view.findViewById(R.id.region_name);
                viewHolder.btnSelect = (ImageButton)view.findViewById(R.id.region_select);
                //将ViewHolder存储在View中
                view.setTag(viewHolder);
            }
            else {
                view = convertView;
                viewHolder = (RegionViewHolder)view.getTag();
            }

            viewHolder.txtName.setText(data);
            int resId = mSelectedRegions.contains(data) ? R.mipmap.ic_shop_checkbox_selected : R.mipmap.ic_shop_checkbox_select;
            viewHolder.btnSelect.setImageResource(resId);
            RegionViewHolder finalHolder = viewHolder;
            viewHolder.btnSelect.setOnClickListener(new OnMultiClickListener()
            {
                @Override
                public void onMultiClick(View v)
                {
                    if(data.equals(sAll))
                    {
                        //选择全部后，其余区域不能选择;同理
                        if(mSelectedRegions.contains(sAll) == false && mSelectedRegions.size() > 0)
                            return;
                    }
                    else
                    {
                        if(mSelectedRegions.contains(sAll))
                            return;
                    }

                    if(mSelectedRegions.contains(data))
                    {
                        finalHolder.btnSelect.setImageResource(R.mipmap.ic_shop_checkbox_select);
                        mSelectedRegions.remove(data);
                    }
                    else
                    {
                        mSelectedRegions.add(data);
                        finalHolder.btnSelect.setImageResource(R.mipmap.ic_shop_checkbox_selected);
                    }
                }
            });
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;//子item是否响应
    }

    class ViewHolder
    {
        TextView name;
        ImageButton btnSelect;
    }

    class PortViewHolder
    {
        EditText txtPort;
    }

    class RegionViewHolder
    {
        TextView txtName;
        ImageButton btnSelect;
    }
}