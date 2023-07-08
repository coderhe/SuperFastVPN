package com.android.ppp.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.ppp.R;

import java.util.List;

public class SettingsAdapter extends ArrayAdapter<Settings>
{
    //resourceID指定ListView的布局方式
    private int resourceID;

    //重写RouteAdapter的构造器
    public SettingsAdapter(Context context, int textViewResourceID , List<Settings> objects)
    {
        super(context, textViewResourceID, objects);
        resourceID = textViewResourceID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Settings settings = getItem(position);
        View view = null;
        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(R.layout.settings_item,null);
            viewHolder = new ViewHolder();
            viewHolder.itemName = (TextView)view.findViewById(R.id.settings_name);

            //将ViewHolder存储在View中
            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.itemName.setText(settings.getName());
        return view;
    }

    class ViewHolder
    {
        TextView itemName;
    }
}