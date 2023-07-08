package com.android.ppp.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ppp.R;
import com.android.ppp.ui.shop.QueryOrderActivity;

import java.util.List;

public class QueryOrderAdapters extends BaseExpandableListAdapter
{
    private List<OrderData> groupList;
    private List<List<OrderData>> childList;
    private LayoutInflater inflater;
    private QueryOrderActivity mActivity;

    public QueryOrderAdapters(Context context, List<OrderData> groupList, List<List<OrderData>> childList)
    {
        this.inflater = LayoutInflater.from(context);
        this.groupList = groupList;
        this.childList = childList;
        mActivity = (QueryOrderActivity)context;
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
        OrderData order = groupList.get(groupPosition);
        ViweHolder holder = null;
        if (view == null) {
            holder = new ViweHolder();
            view = inflater.inflate(R.layout.region_item, null);
            holder.regionBg = (ImageView) view.findViewById(R.id.region_bg);
            holder.groupName = (TextView) view.findViewById(R.id.region_name);

            view.setTag(holder);
        } else {
            holder = (ViweHolder) view.getTag();
        }

        holder.regionBg.setVisibility(View.INVISIBLE);
        holder.groupName.setText(mActivity.getString(R.string.order_number) + order.getRowNo());
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup)
    {
        OrderData order = childList.get(groupPosition).get(childPosition);
        OrderViewHolder viewHolder = null;
        if(view == null)
        {
            view = inflater.inflate(R.layout.order_item, null);
            viewHolder = new OrderViewHolder();
            viewHolder.txtId = (TextView)view.findViewById(R.id.order_name);
            viewHolder.txtPrice = (TextView)view.findViewById(R.id.order_price);
            viewHolder.txtStatus = (TextView)view.findViewById(R.id.order_status);
            viewHolder.txtCreationTime = (TextView)view.findViewById(R.id.order_creation_time);
            //将ViewHolder存储在View中
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (OrderViewHolder)view.getTag();
        }

        viewHolder.txtId.setText(mActivity.getString(R.string.order_id) + order.getId());
        viewHolder.txtPrice.setText(mActivity.getString(R.string.order_price) + order.getMoney());
        viewHolder.txtStatus.setText(mActivity.getString(R.string.order_status) + (order.getStatus() == 1 ? mActivity.getString(R.string.order_status_1) : mActivity.getString(R.string.order_status_0)));
        viewHolder.txtCreationTime.setText(mActivity.getString(R.string.order_creation_time) + order.getCreationTime());
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;//子item是否响应
    }

    class ViweHolder
    {
        ImageView regionBg;
        TextView groupName;
    }

    class OrderViewHolder
    {
        TextView txtId;
        TextView txtPrice;
        TextView txtStatus;
        TextView txtCreationTime;
    }
}