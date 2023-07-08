package com.android.ppp.ui.login;

import android.annotation.SuppressLint;
import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ppp.R;
import com.android.ppp.adapter.BaseApplication;
import com.android.ppp.config.Config;
import com.android.ppp.config.Status;
import com.android.ppp.data.AppData;
import com.android.ppp.mail.SendMailUtil;
import com.android.ppp.mail.ShareUtils;
import com.android.ppp.ui.common.CustomProgressDialog;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.common.ToastUtils;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.AutoSizeConfig;

public class SignUpActivity extends AppCompatActivity implements CustomAdapt
{
    private LoginViewModel loginViewModel;
    private ImageButton signupButton;
    private ImageView loadingImageView;
    private AnimationDrawable animationDrawable;
    private TextView authCodeText;

    private boolean bAlreadySendAuthCode = false;
    private int nRandomAuthCode = 0;
    private final String AUTH_CODE_RESOURCE = "0123456789";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        bAlreadySendAuthCode = false;
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory()).get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final EditText authcodeEditText = findViewById(R.id.authcode);

        final ImageButton exitButton = findViewById(R.id.exitbtn);
        final ImageButton sendAuthCodeBtn = findViewById(R.id.send_authcode_btn);
        authCodeText = findViewById(R.id.send_authcode_btn_text);

        signupButton = findViewById(R.id.sign_up_btn);
        final CheckBox readCB = findViewById(R.id.check_read);
        loadingImageView = (ImageView) findViewById(R.id.loading_fram_image);
        loadingImageView.setImageResource(R.drawable.anim_loading_progress);
        animationDrawable = (AnimationDrawable) loadingImageView.getDrawable();
        if(loadingImageView != null)
            loadingImageView.setVisibility(View.INVISIBLE);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>()
        {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState)
            {
                if (loginFormState == null)
                    return;

                signupButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null)
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));

                if (loginFormState.getPasswordError() != null)
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

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
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };

        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    if(readCB.isChecked())
                    {
                        _SignUp(usernameEditText.getText().toString(), passwordEditText.getText().toString(), authcodeEditText.getText().toString());
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.please_check_signup_protocol), Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

        signupButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                if(readCB.isChecked())
                {
                    signupButton.setEnabled(false);
                    _SignUp(usernameEditText.getText().toString(), passwordEditText.getText().toString(), authcodeEditText.getText().toString());
                }
                else {
                    String info = getResources().getString(R.string.please_check_signup_protocol);
                    Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
                }
            }
        });

        String readInfo = "<font color=\"#000000\">阅读并同意</font><font color=\"#1d9b84\">《用户注册协议》</font><font color=\"#000000\">和</font><font color=\"#1d9b84\">《隐私协议》</font>";
        readCB.setText(Html.fromHtml(readInfo));
        readCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(buttonView.isPressed())
                {
                    if(readCB.isChecked())
                    {
                        readCB.setButtonDrawable(R.mipmap.ic_checkbox_selected);
                        readCB.setChecked(true);
                    }
                    else
                    {
                        readCB.setButtonDrawable(R.mipmap.ic_checkbox_select);
                        readCB.setChecked(false);
                    }
                }
            }
        });

        exitButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(it);
                _FinishActivity();
            }
        });

        sendAuthCodeBtn.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                if(!bAlreadySendAuthCode)
                {
                    bAlreadySendAuthCode = true;
                    _SetTimeDelayTask();
                    nRandomAuthCode = _GetRandomAuthCode();
                    _SendTextMail(usernameEditText.getText().toString(), String.valueOf(nRandomAuthCode));
                }
            }
        });
    }

    private void _SetTimeDelayTask()
    {
        Timer timer = new Timer();
        final int[] index = {0};
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if(++index[0] >= 60)
                {
                    bAlreadySendAuthCode = false;
                    nRandomAuthCode = 0;
                    timer.cancel();
                    if(authCodeText != null)
                        authCodeText.setText(getResources().getString(R.string.send));
                }
                else
                {
                    if(authCodeText != null)
                        authCodeText.setText((60- index[0]) + "s");
                }
            }
        }, 60000, 1000);
    }

    private int _GetRandomAuthCode()
    {
        Random rand = new Random();
        StringBuffer flag = new StringBuffer();
        for (int i = 0; i < 6; i++)
        {
            flag.append(AUTH_CODE_RESOURCE.charAt(rand.nextInt(9)) + "");
        }

        return AppData.String2Int(flag.toString());
    }

    private void _SignUp(String username, String password, String authcode)
    {
        //请先获取邮箱验证码
        if(!bAlreadySendAuthCode)
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_authcode), Toast.LENGTH_SHORT).show();
            return;
        }

        if(username.isEmpty())
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_username), Toast.LENGTH_SHORT).show();
            return;
        }

        if(_IsValidMailAddress(username) == false)
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invliad_mail_type), Toast.LENGTH_SHORT).show();
            return;
        }

        if(password.isEmpty())
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_password), Toast.LENGTH_SHORT).show();
            return;
        }

        if(nRandomAuthCode != AppData.String2Int(authcode))
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_authcode), Toast.LENGTH_SHORT).show();
            return;
        }

        loginViewModel.SignUp(username, password, _GetPhoneMAC(), mCallback);
        //显示Loading动画
        if(loadingImageView != null)
            loadingImageView.setVisibility(View.VISIBLE);

        if(animationDrawable != null)
            animationDrawable.start();
    }

    public interface Callback
    {
        public abstract void LoginResult(Status status);
    }

    /* 定义回调接口的成员变量  */
    private SignUpActivity.Callback mCallback = new SignUpActivity.Callback(){
        @Override
        public void LoginResult(Status status)
        {
            if(status == Status.Success)
            {
                Message msg = new Message();
                msg.what = 0;
                msg.obj = status;
                handler.sendMessage(msg);
            }
            else
            {
                Message msg = new Message();
                msg.what = 1;
                msg.obj = status;
                handler.sendMessage(msg);
            }
            //ToastUtils.show(getApplicationContext(), AppData.GetServerResponse(SignUpActivity.this, status));
        }
    };

    private void _SetSignupEnable()
    {
        if(signupButton != null && signupButton.isEnabled() == false)
            signupButton.setEnabled(true);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            Toast.makeText(getApplicationContext(), AppData.GetServerResponse(SignUpActivity.this, (Status)msg.obj), Toast.LENGTH_SHORT).show();

            if(animationDrawable != null)
                animationDrawable.stop();

            if(loadingImageView != null)
                loadingImageView.setVisibility(View.INVISIBLE);

            if(msg.what == 0)
            {
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(it);
                _FinishActivity();
            }
            else if(msg.what == 1)
            {
                _SetSignupEnable();
            }
        }
    };

    private void _SendTextMail(String toMailAddress, String sAuthCode)
    {
        ShareUtils.putString(BaseApplication.GetInstance(), "FROM_ADD", "cagehe@qq.com");
        ShareUtils.putString(BaseApplication.GetInstance(), "FROM_PSW", "mecxacfqolplcbbg");
        ShareUtils.putString(BaseApplication.GetInstance(), "HOST", "smtp.qq.com");
        ShareUtils.putString(BaseApplication.GetInstance(), "PORT", "587");

        SendMailUtil.send(toMailAddress, sAuthCode);
        Toast.makeText(SignUpActivity.this, "邮件已发送", Toast.LENGTH_SHORT).show();
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

    private String _GetPhoneMAC()
    {
        java.util.UUID mac = java.util.UUID.randomUUID();
        return _MD5Encrypt(mac.toString());
    }

    public String _MD5Encrypt(String str)
    {
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            digest = md5.digest(str.getBytes("utf-8"));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        //转换为16进制
        return new BigInteger(1, digest).toString(16).toUpperCase();
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        nRandomAuthCode = 0;
        bAlreadySendAuthCode = false;
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
}