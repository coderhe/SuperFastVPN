package com.android.ppp.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ppp.R;

import java.util.List;

public class ShopItemAdapter extends ArrayAdapter<ShopItem>
{
    //resourceID指定ListView的布局方式
    private int resourceID;
    private int selectItem = -1;

    //重写ShopItemAdapter的构造器
    public ShopItemAdapter(Context context, int textViewResourceID , List<ShopItem> objects)
    {
        super(context, textViewResourceID, objects);
        resourceID = textViewResourceID;
    }

    public void setSelection(int position)
    {
        selectItem = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ShopItem shop = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null)
        {
            view = LayoutInflater.from(getContext()).inflate(resourceID,null);
            viewHolder = new ViewHolder();
            viewHolder.shopBg = (ImageView)view.findViewById(R.id.shop_bg);
            viewHolder.shopItemName = (TextView)view.findViewById(R.id.shop_item_name);
            viewHolder.shopItemPrice = (TextView)view.findViewById(R.id.shop_item_price);
            viewHolder.shopItemDes = (TextView)view.findViewById(R.id.shop_item_des);

            //将ViewHolder存储在View中
            view.setTag(viewHolder);
        }
        else {
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.shopBg.setImageResource(R.mipmap.ic_shop_suit_frame);
        viewHolder.shopItemName.setText(shop.getName());
        viewHolder.shopItemPrice.setText(shop.getPrice());
        viewHolder.shopItemDes.setText(shop.getDes());
        if (selectItem == position) {
            viewHolder.shopBg.setImageResource(R.mipmap.ic_shop_suit_frame_yellow);
        }
        return view;
    }

    class ViewHolder
    {
        ImageView shopBg;
        TextView shopItemName;
        TextView shopItemPrice;
        TextView shopItemDes;
    }
}