package com.android.ppp.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.ppp.R;
import com.android.ppp.adapter.BaseApplication;
import com.android.ppp.adapter.HttpUtils;
import com.android.ppp.algorithm.Rc4sha1;
import com.android.ppp.config.Config;
import com.android.ppp.config.Status;
import com.android.ppp.data.AppData;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.data.PPPVpnService;
import com.android.ppp.data.RouteData;
import com.android.ppp.mail.SendMailUtil;
import com.android.ppp.mail.ShareUtils;
import com.android.ppp.ui.common.OnMultiClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.internal.CustomAdapt;

public class ForgetPasswordActivity extends AppCompatActivity implements CustomAdapt
{
    private boolean bAlreadySendPassword = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        final EditText usernameEditText = findViewById(R.id.username);
        final ImageButton exitButton = findViewById(R.id.exitbtn);
        final ImageButton sumitBtn = findViewById(R.id.sumit_btn);

        TextWatcher afterTextChangedListener = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(usernameEditText.getText().toString().length() >= 18)
                {
                    usernameEditText.setTextSize(10f);
                }
                else if(usernameEditText.getText().toString().length() >= 15)
                {
                    usernameEditText.setTextSize(16f);
                }
                else if(usernameEditText.getText().toString().length() >= 10)
                {
                    usernameEditText.setTextSize(18f);
                }
                else
                {
                    usernameEditText.setTextSize(20f);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        sumitBtn.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                String email = usernameEditText.getText().toString().trim();
                if(email.isEmpty())
                {
                    //不能为空
                    return;
                }

                if(_IsValidMailAddress(email) == false)
                {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invliad_mail_type), Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!bAlreadySendPassword)
                {
                    bAlreadySendPassword = true;
                    _GetAccountPassword(email);
                }
            }
        });

        exitButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _FinishActivity();
            }
        });
    }

    private void _GetAccountPassword(String username)
    {
        try {
            JSONObject params = new JSONObject();
            params.put("AccountId", AppData.mUserUUId);
            params.put("Mac", AppData.mUserMac);

            HttpUtils.post(Config.ACCOUNT_UPDATE_URL, HttpUtils.OKHttpPost(params), new okhttp3.Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    bAlreadySendPassword = false;
                    if(Config.CanLog)
                        Log.e("Application", Config.ACCOUNT_UPDATE_URL);
                }

                @Override
                public void onResponse(Call call, Response respo) throws IOException
                {
                    String data = respo.body().string();
                    if(Config.CanLog)
                        Log.e("Application", data);

                    bAlreadySendPassword = false;
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
                            _SendTextMail(username, password);
                            _FinishActivity();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (Exception e) {
        }
    }

    private void _SendTextMail(String toMailAddress, String sAuthCode)
    {
        ShareUtils.putString(BaseApplication.GetInstance(), "FROM_ADD", "cagehe@qq.com");
        ShareUtils.putString(BaseApplication.GetInstance(), "FROM_PSW", "mecxacfqolplcbbg");
        ShareUtils.putString(BaseApplication.GetInstance(), "HOST", "smtp.qq.com");
        ShareUtils.putString(BaseApplication.GetInstance(), "PORT", "587");

        SendMailUtil.send(toMailAddress, sAuthCode);
        Toast.makeText(ForgetPasswordActivity.this, "邮件已发送", Toast.LENGTH_SHORT).show();
    }

    private boolean _IsValidMailAddress(String username)
    {
        for(int i = 0; i < Config.mailHostWhitelist.length; ++i)
        {
            if(username.contains(Config.mailHostWhitelist[i]))
                return true;
        }

        return false;
    }

    /**
     * 需要注意的是暂停 AndroidAutoSize 后, AndroidAutoSize 只是停止了对后续还没有启动的 {@link Activity} 进行适配的工作
     * 但对已经启动且已经适配的 {@link Activity} 不会有任何影响
     */
    public void stop(View view)
    {
        Toast.makeText(getApplicationContext(), "AndroidAutoSize stops working!", Toast.LENGTH_SHORT).show();
        AutoSizeConfig.getInstance().stop(this);
    }

    /**
     * 需要注意的是重新启动 AndroidAutoSize 后, AndroidAutoSize 只是重新开始了对后续还没有启动的 {@link Activity} 进行适配的工作
     * 但对已经启动且在 stop 期间未适配的 {@link Activity} 不会有任何影响
     */
    public void restart(View view)
    {
        Toast.makeText(getApplicationContext(), "AndroidAutoSize continues to work", Toast.LENGTH_SHORT).show();
        AutoSizeConfig.getInstance().restart();
    }

    /**
     * 是否按照宽度进行等比例适配 (为了保证在高宽比不同的屏幕上也能正常适配, 所以只能在宽度和高度之中选择一个作为基准进行适配)
     *
     * @return {@code true} 为按照宽度进行适配, {@code false} 为按照高度进行适配
     */
    @Override
    public boolean isBaseOnWidth() {
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
    public float getSizeInDp() {
        return 640;
    }

    //关闭Activity
    private void _FinishActivity()
    {
        if (this != null)
            this.finish();
    }
}