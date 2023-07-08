package com.android.ppp.ui.shop;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.ppp.R;
import com.android.ppp.adapter.HttpUtils;
import com.android.ppp.config.Config;
import com.android.ppp.config.Status;
import com.android.ppp.data.AppData;
import com.android.ppp.data.ShopData;
import com.android.ppp.data.ShopItem;
import com.android.ppp.data.ShopItemAdapter;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.common.ToastUtils;
import com.android.ppp.ui.home.HomeActivity;
import com.android.ppp.ui.vpn.RoutesActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.AutoSizeConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShopActivity extends AppCompatActivity implements CustomAdapt
{
    private ImageButton btnBuySuit;
    private ImageButton btnCoupon;

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

    private WebView mWebView;
    private TextView txt_money;
    private ImageButton btnAlipay;
    private ImageButton btnWXPay;
    private boolean isWXPay = true;

    //ShopItem实体集合
    private List<ShopItem> mShopItems = new ArrayList<>();
    private ShopItem mSelctedShopItem;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // 初始化控件
        _InitView();
        // 初始化底部按钮事件
        _InitEvent();

        if(ShopData.ShopItems == "")
        {
            _HttpGetShopSettings();
            while(true)
            {
                if(ShopData.ShopItems != "")
                {
                    _ShowShopItems();
                    break;
                }
            }
        }
        else
        {
            _ShowShopItems();
        }

        mWebView = findViewById(R.id.web_view);
        if(mWebView != null)
            mWebView.setVisibility(View.INVISIBLE);
        txt_money = findViewById(R.id.money_text);
        btnBuySuit = findViewById(R.id.buy_button);
        btnBuySuit.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                btnBuySuit.setEnabled(false);
                _HttpCreatePayOrder();
            }
        });

        btnCoupon = findViewById(R.id.coupon_btn);
        btnCoupon.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                Toast.makeText(getApplicationContext(), R.string.not_have_coupon, Toast.LENGTH_SHORT).show();
            }
        });

        /*
        btnAlipay = findViewById(R.id.alipay_button);
        btnAlipay.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                if(isWXPay)
                {
                    isWXPay = false;
                    _UpdatePaySelect();
                }
            }
        });

        btnWXPay = findViewById(R.id.wx_pay_button);
        btnWXPay.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                if(!isWXPay)
                {
                    isWXPay = true;
                    _UpdatePaySelect();
                }
            }
        });

        _UpdatePaySelect();*/
    }

    private void _HttpCreatePayOrder()
    {
        if(mSelctedShopItem == null)
        {
            Toast.makeText(getApplicationContext(), R.string.not_select_pay_order, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject params = new JSONObject();
            params.put("PricingId", mSelctedShopItem.getId());
            params.put("AccountId", AppData.mUserUUId);

            HttpUtils.post(Config.PAY_URL, HttpUtils.OKHttpPost(params), new okhttp3.Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    _SendErrorMsg(Status.UnknownError);
                    if(Config.CanLog)
                        Log.e("ShopActivity", Config.PAY_URL);
                }

                @Override
                public void onResponse(Call call, Response respo) throws IOException
                {
                    String data = respo.body().string();
                    if(Config.CanLog)
                        Log.e("ShopActivity", data);

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
                            Message msg = new Message();
                            msg.what = 1;
                            msg.obj = result.getString("Tag");
                            handler.sendMessage(msg);
                        }
                        else
                        {
                            _SendErrorMsg(status);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    private void _SendErrorMsg(Status status)
    {
        Message msg = new Message();
        msg.what = 2;
        msg.obj = status;
        handler.sendMessage(msg);
    }

    private void OpenWebView(String url)
    {
        if(mWebView != null)
        {
            //mWebView.loadUrl(url);
            mWebView.setVisibility(View.VISIBLE);
            //如果访问的页面中有Javascript，则webview必须设置支持Javascript
            mWebView.getSettings().setJavaScriptEnabled(true);
            //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
            mWebView.setWebViewClient(new WebViewClient()
            {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url)
                {
                    //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                    view.loadUrl(url);
                    return true;
                }
            });
        }
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if (msg.what == 1)
            {
                _SetBuyEnable();
                OpenWebView(msg.obj.toString());
            }
            else {
                _SetBuyEnable();
                Toast.makeText(getApplicationContext(), AppData.GetServerResponse(ShopActivity.this, (Status)msg.obj), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void _SetBuyEnable()
    {
        if(btnBuySuit != null && btnBuySuit.isEnabled() == false)
            btnBuySuit.setEnabled(true);
    }

    private void _HttpGetShopSettings()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(Config.GLOBAL_SETTINGS).get().build();
        final Call call = client.newCall(request);
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("error","出错");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ShopData.ShopItems = response.body().string();
            }
        });
    }

    //展示所有线路
    private void _ShowShopItems()
    {
        try {
            JSONObject item = null;
            JSONObject shopitems = new JSONObject(ShopData.ShopItems);
            JSONArray json = shopitems.getJSONArray("pricings");
            for (int i = 0; i < json.length(); ++i)
            {
                item = json.getJSONObject(i);
                if(item != null)
                {
                    ShopItem sim = new ShopItem(item.getInt("id"), item.getString("title"), item.getInt("price"), item.getString("description"));
                    mShopItems.add(sim);
                }
            }

            //初始化适配器
            ShopItemAdapter myAdapter = new ShopItemAdapter(this, R.layout.shop_item, mShopItems);
            GridView gridView = (GridView)findViewById(R.id.grid_view);
            gridView.setAdapter(myAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    mSelctedShopItem = mShopItems.get(position);
                    _ShowMoneyText();
                    myAdapter.setSelection(position);
                    myAdapter.notifyDataSetChanged();
                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    private void _UpdatePaySelect()
    {
        if(isWXPay)
        {
            btnAlipay.setImageResource(R.mipmap.ic_shop_checkbox_select);
            btnWXPay.setImageResource(R.mipmap.ic_shop_checkbox_selected);
        }
        else
        {
            btnAlipay.setImageResource(R.mipmap.ic_shop_checkbox_selected);
            btnWXPay.setImageResource(R.mipmap.ic_shop_checkbox_select);
        }
    }

    private void _ShowMoneyText()
    {
        if(txt_money != null && mSelctedShopItem != null)
            txt_money.setText(mSelctedShopItem.getPrice());
    }

    private void _InitEvent()
    {
        // 设置按钮监听
        ll_change_ip.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v) {
                _FinishActivity();
                _TurnToRoutesActivity();
            }
        });
        ll_home.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v) {
                _FinishActivity();
                _TurnToHomeActivity();
            }
        });
    }

    private void _InitView()
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

        iv_change_ip.setImageResource(R.mipmap.ic_connect_change_ip);
        tv_change_ip.setTextColor(0xffffffff);
        iv_shopping.setImageResource(R.mipmap.ic_connect_shopping_green);
        tv_shopping.setTextColor(0xff1B940A);
        iv_home.setImageResource(R.mipmap.ic_connect_mine);
        tv_home.setTextColor(0xffffffff);
    }

    private void _TurnToRoutesActivity()
    {
        //关闭此activity，打开RoutesActivity
        Intent it = new Intent(getApplicationContext(), RoutesActivity.class);
        startActivity(it);
    }

    private void _TurnToHomeActivity()
    {
        //关闭此activity，打开HomeActivity
        Intent it = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(it);
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

        if(mShopItems != null)
            mShopItems.clear();

        mShopItems = null;
        mSelctedShopItem = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            //如果返回键按下
            return false;
        }

        return super.onKeyDown(keyCode, event);
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