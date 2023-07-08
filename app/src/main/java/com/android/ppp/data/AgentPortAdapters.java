package com.android.ppp.data;

import android.content.Context;
import android.media.Image;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ppp.R;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.home.AgentSettingActivity;
import com.android.ppp.ui.vpn.SelectRouteActivity;

import java.util.List;

public class AgentPortAdapters extends BaseExpandableListAdapter
{
    private List<String> groupList;
    private List<List<PortData>> childList;
    private LayoutInflater inflater;
    private AgentSettingActivity mActivity;

    public AgentPortAdapters(Context context, List<String> groupList, List<List<PortData>> childList)
    {
        this.inflater = LayoutInflater.from(context);
        this.groupList = groupList;
        this.childList = childList;
        mActivity = (AgentSettingActivity)context;
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
                mActivity.SetListner(finalHolder.btnSelect, groupPosition);
            }
        });

        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean b, View view, ViewGroup viewGroup)
    {
        PortViewHolder viewHolder = null;
        if(view == null)
        {
            view = inflater.inflate(R.layout.port_item, null);
            viewHolder = new PortViewHolder();
            viewHolder.txtPort = (EditText)view.findViewById(R.id.port_text);
            //将ViewHolder存储在View中
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (PortViewHolder)view.getTag();
        }

        PortData data = childList.get(groupPosition).get(childPosition);
        if(data.getType() == 0)
            viewHolder.txtPort.setHint(data.getHint());
        else
            viewHolder.txtPort.setText(data.getPort());

        PortViewHolder finalViewHolder = viewHolder;
        viewHolder.txtPort.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_DONE)
                {
                    String port = finalViewHolder.txtPort.getText().toString();
                    if(AppData.IsDigit(port))
                    {
                        int number = Integer.parseInt(port);
                        if(number > 65535)
                            mActivity.ShowErrorPort1();
                        else
                            mActivity.InputCorrectPort(groupPosition, number);
                    }
                    else
                        mActivity.ShowErrorPort();
                }

                return false;
            }
        });

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
}