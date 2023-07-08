package com.android.ppp.ui.common;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.ppp.R;
import androidx.appcompat.app.AppCompatActivity;
import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.AutoSizeConfig;

public class BottomActivity extends AppCompatActivity implements CustomAdapt,OnClickListener
{
    // 底部菜单4个Linearlayout
    private LinearLayout ll_home;
    private LinearLayout ll_change_ip;
    private LinearLayout ll_shopping;

    // 底部菜单4个ImageView
    private ImageView iv_home;
    private ImageView iv_change_ip;
    private ImageView iv_shopping;

    // 底部菜单4个菜单标题
    private TextView tv_home;
    private TextView tv_change_ip;
    private TextView tv_shopping;

    private List<View> views;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bottom);

        // 初始化控件
        initView();
        // 初始化底部按钮事件
        initEvent();
    }

    private void initEvent()
    {
        // 设置按钮监听
        ll_change_ip.setOnClickListener(this);
        ll_shopping.setOnClickListener(this);
        ll_home.setOnClickListener(this);
    }

    private void initView()
    {
        // 底部菜单4个Linearlayout
        this.ll_change_ip = (LinearLayout) findViewById(R.id.ll_change_ip);
        this.ll_shopping = (LinearLayout) findViewById(R.id.ll_shopping);
        this.ll_home = (LinearLayout) findViewById(R.id.ll_home);

        // 底部菜单4个ImageView
        this.iv_home = (ImageView) findViewById(R.id.iv_home);
        this.iv_change_ip = (ImageView) findViewById(R.id.iv_change_ip);
        this.iv_shopping = (ImageView) findViewById(R.id.iv_shopping);

        // 底部菜单4个菜单标题
        this.tv_home = (TextView) findViewById(R.id.tv_home);
        this.tv_change_ip = (TextView) findViewById(R.id.tv_change_ip);
        this.tv_shopping = (TextView) findViewById(R.id.tv_shopping);
    }

    @Override
    public void onClick(View v)
    {
        // 在每次点击后将所有的底部按钮(ImageView,TextView)颜色改为灰色，然后根据点击着色
        restartBotton();
        // ImageView和TetxView置为绿色，页面随之跳转
        switch (v.getId())
        {
            case R.id.ll_home:
                iv_home.setImageResource(R.mipmap.ic_connect_mine_green);
                tv_home.setTextColor(0xff1B940A);
                break;
            case R.id.ll_change_ip:
                iv_change_ip.setImageResource(R.mipmap.ic_connect_change_ip_green);
                tv_change_ip.setTextColor(0xff1B940A);
                break;
            case R.id.ll_shopping:
                iv_shopping.setImageResource(R.mipmap.ic_connect_shopping_green);
                tv_shopping.setTextColor(0xff1B940A);
                break;
        }
    }

    private void restartBotton()
    {
        // ImageView置为绿色
        iv_change_ip.setImageResource(R.mipmap.ic_connect_change_ip);
        iv_shopping.setImageResource(R.mipmap.ic_connect_shopping);
        iv_home.setImageResource(R.mipmap.ic_connect_mine);
        // TextView置为白色
        tv_change_ip.setTextColor(0xffffffff);
        tv_shopping.setTextColor(0xffffffff);
        tv_home.setTextColor(0xffffffff);
    }

    /**
     * 是否按照宽度进行等比例适配 (为了保证在高宽比不同的屏幕上也能正常适配, 所以只能在宽度和高度之中选择一个作为基准进行适配)
     *
     * @return {@code true} 为按照宽度进行适配, {@code false} 为按照高度进行适配
     */
    @Override
    public boolean isBaseOnWidth()
    {
        return false;
    }

    /**
     * 返回设计图上的设计尺寸, 单位 dp
     * {@link #getSizeInDp} 须配合 {@link #isBaseOnWidth()} 使用, 规则如下:
     * 如果 {@link #isBaseOnWidth()} 返回 {@code true}, {@link #getSizeInDp} 则应该返回设计图的总宽度
     * 如果 {@link #isBaseOnWidth()} 返回 {@code false}, {@link #getSizeInDp} 则应该返回设计图的总高度
     * 如果您不需要自定义设计图上的设计尺寸, 想继续使用在 AndroidManifest 中填写的设计图尺寸, {@link #getSizeInDp} 则返回 {@code 0}
     * @return 设计图上的设计尺寸, 单位 dp
     */
    @Override
    public float getSizeInDp()
    {
        return 640;
    }
}