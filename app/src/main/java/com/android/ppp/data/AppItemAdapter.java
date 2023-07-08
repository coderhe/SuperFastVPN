package com.android.ppp.data;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ppp.R;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.home.AppUseVPNSettingActivity;

import java.util.ArrayList;
import java.util.List;

public class AppItemAdapter extends ArrayAdapter<AppItem>
{
    //resourceID指定ListView的布局方式
    private int resourceID;
    int selectItem = -1;
    private List<AppItem> mSelectedApps;
    private AppUseVPNSettingActivity mActivity;

    //AppItemAdapter
    public AppItemAdapter(Context context, int textViewResourceID , List<AppItem> objects, List<AppItem> selectedApps)
    {
        super(context, textViewResourceID, objects);

        resourceID = textViewResourceID;
        mActivity = (AppUseVPNSettingActivity)context;
        mSelectedApps = selectedApps;
    }

    public void setSelection(int position)
    {
        selectItem = position;
    }

    public void ClearSelectApps()
    {
        if(mSelectedApps != null)
            mSelectedApps.clear();
    }

    public List<AppItem> GetSelectedApps()
    {
        return mSelectedApps;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        AppItem app = getItem(position);
        ViewHolder viewHolder = null;
        View view;
        if(convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(resourceID,null);
            viewHolder = new ViewHolder();
            viewHolder.txtName = (TextView)view.findViewById(R.id.region_name);
            viewHolder.btnSelect = (ImageButton)view.findViewById(R.id.region_select);
            viewHolder.imgIcon = (ImageView)view.findViewById(R.id.icon_img);
            //将ViewHolder存储在View中
            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.txtName.setText(app.getName());
        Drawable icon = app.getIcon();
        if(icon != null)
            viewHolder.imgIcon.setImageDrawable(icon);
        int resId = mSelectedApps.contains(app) ? R.mipmap.ic_shop_checkbox_selected : R.mipmap.ic_shop_checkbox_select;
        viewHolder.btnSelect.setImageResource(resId);
        ViewHolder finalHolder = viewHolder;
        viewHolder.btnSelect.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                if(mSelectedApps.contains(app))
                {
                    finalHolder.btnSelect.setImageResource(R.mipmap.ic_shop_checkbox_select);
                    mSelectedApps.remove(app);
                }
                else
                {
                    mSelectedApps.add(app);
                    finalHolder.btnSelect.setImageResource(R.mipmap.ic_shop_checkbox_selected);
                }
            }
        });

        return view;
    }

    class ViewHolder
    {
        ImageView imgIcon;
        TextView txtName;
        ImageButton btnSelect;
    }
}