package com.android.ppp.ui.login;

import android.app.Activity;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ppp.R;
import com.android.ppp.adapter.BaseApplication;
import com.android.ppp.adapter.HttpUtils;
import com.android.ppp.config.Config;
import com.android.ppp.data.AppData;
import com.android.ppp.data.JsonUtils;
import com.android.ppp.data.Result;
import com.android.ppp.data.ShopData;
import com.android.ppp.data.model.LoggedInUser;
import com.android.ppp.mail.SendMailUtil;
import com.android.ppp.mail.ShareUtils;
import com.android.ppp.ui.common.CustomProgressDialog;
import com.android.ppp.ui.common.OnMultiClickListener;
import com.android.ppp.ui.common.ToastUtils;
import com.android.ppp.ui.home.RouteSettingActivity;
import com.android.ppp.ui.shop.ShopActivity;
import com.android.ppp.ui.vpn.RoutesActivity;
import com.android.ppp.config.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.internal.CustomAdapt;
import me.jessyan.autosize.AutoSizeConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements CustomAdapt
{
    private LoginViewModel loginViewModel;
    private ImageButton loginButton;
    private CustomProgressDialog progressDialog;
    private ImageView loadingImageView;
    private AnimationDrawable animationDrawable;

    private boolean autoLogin;
    private boolean autoRecordPassword;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            this.finish();
            return;
        }
        //SendTextMail(usernameEditText.getText().toString(), "666");
        setContentView(R.layout.activity_login);

        //从app拷贝到本地设备
        _CopyFile(Config.APP_PACKAGE_PATH, Config.APP_SETTINGS_NAME, false);
        _CopyFile(Config.APP_PACKAGE_PATH, Config.APP_IP_ADDRESS, true);
        _CopyFile(Config.APP_PACKAGE_PATH, Config.APP_USER_NAME, true);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final ImageButton exitButton = findViewById(R.id.exitbtn);
        loginButton = findViewById(R.id.loginbtn);
        final Button signupButton = findViewById(R.id.login_sign_up_btn);
        final Button forgetpwdButton = findViewById(R.id.login_forget_pwd_btn);
        final CheckBox checkAutoLogin = findViewById(R.id.check_auto_login);
        final CheckBox checkRemeberPwd = findViewById(R.id.check_remeber_pwd);
        loadingImageView = (ImageView) findViewById(R.id.loading_fram_image);
        loadingImageView.setImageResource(R.drawable.anim_loading_progress);
        animationDrawable = (AnimationDrawable)loadingImageView.getDrawable();
        if(loadingImageView != null)
            loadingImageView.setVisibility(View.INVISIBLE);

        autoLogin = false;//(boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "autoLogin");
        autoRecordPassword = (boolean)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "autoRecordPassword");
        if(checkAutoLogin != null)
        {
            checkAutoLogin.setButtonDrawable(autoLogin ? R.mipmap.ic_checkbox_selected : R.mipmap.ic_checkbox_select);
            checkAutoLogin.setChecked(autoLogin);
        }

        if(checkRemeberPwd != null)
        {
            checkRemeberPwd.setButtonDrawable(autoRecordPassword ? R.mipmap.ic_checkbox_selected : R.mipmap.ic_checkbox_select);
            checkRemeberPwd.setChecked(autoRecordPassword);
        }

        exitButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                _ExitApplication();
            }
        });

        signupButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                //打开signupactivity
                Intent it = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(it);
                _FinishActivity();
            }
        });

        forgetpwdButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                //关闭此activity，打开forgetpwdactivity
                Intent it = new Intent(getApplicationContext(), ForgetPasswordActivity.class);
                startActivity(it);
            }
        });

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>()
        {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState)
            {
                if (loginFormState == null)
                    return;

                loginButton.setEnabled(loginFormState.isDataValid());
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
                    loginButton.setEnabled(false);
                    _SignIn(usernameEditText.getText().toString(), passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new OnMultiClickListener()
        {
            @Override
            public void onMultiClick(View v)
            {
                loginButton.setEnabled(false);
                _SignIn(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        checkAutoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(buttonView.isPressed())
                {
                    if(checkAutoLogin.isChecked())
                    {
                        autoLogin = true;
                        checkAutoLogin.setButtonDrawable(R.mipmap.ic_checkbox_selected);
                    }
                    else
                    {
                        autoLogin = false;
                        checkAutoLogin.setButtonDrawable(R.mipmap.ic_checkbox_select);
                    }
                    checkAutoLogin.setChecked(autoLogin);
                }
            }
        });

        checkRemeberPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(buttonView.isPressed())
                {
                    if(checkRemeberPwd.isChecked())
                    {
                        autoRecordPassword = true;
                        checkRemeberPwd.setButtonDrawable(R.mipmap.ic_checkbox_selected);
                    }
                    else
                    {
                        autoRecordPassword = false;
                        checkRemeberPwd.setButtonDrawable(R.mipmap.ic_checkbox_select);
                    }
                    checkRemeberPwd.setChecked(autoRecordPassword);
                }
            }
        });

        String userEmail = (String)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "email");
        String userPwd = (String)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "password");
        if(autoRecordPassword)
        {
            if(usernameEditText != null && userEmail != null && !userEmail.isEmpty())
                usernameEditText.setText(userEmail);
            if(passwordEditText != null && userPwd != null && !userPwd.isEmpty())
                passwordEditText.setText(userPwd);
        }

        if(autoLogin && userEmail != null && !userEmail.isEmpty() && userPwd != null && !userPwd.isEmpty())
        {
            loginButton.setEnabled(false);
            _SignIn(userEmail, userPwd);
        }
    }

    //复制文件到目标路径
    private void _CopyFile(String fileDirPath, String fileName, boolean isExistReturn)
    {
        // 文件路径
        String filePath = fileDirPath + "/" + fileName;
        try
        {
            // 目录路径
            File dir = new File(fileDirPath);
            if (!dir.exists())
            {
                // 如果不存在，则创建路径名
                dir.mkdirs();
            }

            //目录文件存在，则删除；不存在，则将apk中assets文件夹中的需要的文档复制到该目录下
            File file = new File(filePath);
            if(isExistReturn && file.exists())
                return;

            if(!file.exists())
                file.createNewFile();

            //通过assets得到数据资源
            InputStream is = getResources().getAssets().open(fileName);
            FileOutputStream fs = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count = 0; //循环写出
            while ((count = is.read(buffer)) > 0)
            {
                fs.write(buffer, 0, count);
            }
            //关闭流
            fs.close();
            is.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //创造All.txt
    private void _CreateRoutesFile()
    {
        String sCountryRouterSettings = JsonUtils.ReadAssetJson(LoginActivity.this, "countryroutersettings.json");
        List<String> itemsList = new ArrayList<>();
        try {
            JSONObject obj = new JSONObject(sCountryRouterSettings);
            JSONArray array = obj.getJSONArray("region_routers");
            JSONObject regionObj;
            for (int k = 1; k < array.length(); ++k)
            {
                regionObj = array.getJSONObject(k);
                itemsList.add(regionObj.getString("File"));
            }
        }
        catch (JSONException e)
        {
        }

        String AllInfo = "";
        for (int i = 0; i < itemsList.size(); ++i)
        {
            String info = JsonUtils.ReadAssetJson(LoginActivity.this, itemsList.get(i));
            AllInfo += info;
        }

        String filePath = Config.APP_PACKAGE_PATH + "/All.txt";
        try {
            //目录文件存在，则删除；不存在，则将apk中assets文件夹中的需要的文档复制到该目录下
            File file = new File(filePath);
            if (!file.exists())
                file.createNewFile();

            JsonUtils.WriteJsonFile(AllInfo, filePath);
        }
        catch (IOException e)
        {

        }
    }

    private void _DoGet()
    {
        String routers = JsonUtils.ReadAssetJson(LoginActivity.this, "countryroutersettings.json");
        try {
            JSONObject obj = new JSONObject(routers);
            JSONArray array = obj.getJSONArray("region_routers");
            for(int i = 0; i < 1; ++i)
            {
                JSONObject obj1 = array.getJSONObject(i);
                _HttpGetSettings(obj1.getString("File"));
            }
        }
        catch (JSONException e)
        {

        }
    }

    private void _HttpGetSettings(String name)
    {
        OkHttpClient client = new OkHttpClient();
        //Request request = new Request.Builder().url("https://raw.githubusercontent.com/metowolf/iplist/master/data/country/" + name).get().build();
        Request request = new Request.Builder().url(Config.GLOBAL_SETTINGS).get().build();

        final Call call = client.newCall(request);
        call.enqueue(new okhttp3.Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("error","出错");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                _DownFile(Config.APP_PACKAGE_PATH, name, response.body().string());
            }
        });
    }

    //复制文件到目标路径
    private void _DownFile(String fileDirPath, String fileName, String content)
    {
        // 文件路径
        String filePath = fileDirPath + "/" + fileName;
        try
        {
            // 目录路径
            File dir = new File(fileDirPath);
            if (!dir.exists())
            {
                // 如果不存在，则创建路径名
                dir.mkdirs();
            }

            //目录文件存在，则删除；不存在，则将apk中assets文件夹中的需要的文档复制到该目录下
            File file = new File(filePath);
            if(!file.exists())
                file.createNewFile();

            FileOutputStream fs = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int line = 0;
            String[] lines = content.split("\n");
            while (lines.length > line)
            {
                buffer = lines[line].getBytes();
                fs.write(buffer, 0, buffer.length);
                line++;
            }
            //关闭流
            fs.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void _SetLoginEnable()
    {
        if(loginButton != null && loginButton.isEnabled() == false)
            loginButton.setEnabled(true);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            if(progressDialog != null)
                progressDialog.hide();

            if(animationDrawable != null)
                animationDrawable.stop();

            if(loadingImageView != null)
                loadingImageView.setVisibility(View.INVISIBLE);

            if (msg.what == 0)
            {
                Toast.makeText(LoginActivity.this, AppData.GetServerResponse(LoginActivity.this, (Status)msg.obj), Toast.LENGTH_SHORT).show();
                _StartRoutesActivity();
            }
            else if(msg.what == 1)
            {
                Toast.makeText(LoginActivity.this, AppData.GetServerResponse(LoginActivity.this, (Status)msg.obj), Toast.LENGTH_SHORT).show();
                _SetLoginEnable();
            }
        }
    };

    public interface Callback
    {
        public abstract void LoginResult(Status status);
    }

    /*   * 定义回调接口的成员变量   */
    private Callback mCallback = new Callback(){
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
        }
    };

    private void _StartRoutesActivity()
    {
        //打开RoutesActivity
        Intent it = new Intent(getApplicationContext(), RoutesActivity.class);
        startActivity(it);
        _FinishActivity();
    }

    private void _SignIn(String username, String password)
    {
        if(_GetVersionName() < "1.0.1")
        {
            new AlertDialog.Builder(LoginActivity.this)
                    .setTitle("发现新版本")
                    .setMessage("您还未下载新版本，是否进行下载？")
                    .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Uri uri = Uri.parse(downloadAddress);
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            this.startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            _ExitApplication();
                        }
                    })
                    .show();

            /*
            downloadAddress = downloadAdd;
            BaseDialog dialog = new BaseDialog();
            dialog.setTitle("提示");
            dialog.setContent("您还未下载“" + DelegateDataBase.ENTRUST_LIST[index] + "”是否进行下载？");
            dialog.setConfirm("下载", new BaseDialog.DialogListener() {
                @Override
                public void onListener() {
                        Uri uri = Uri.parse(downloadAddress.toString());
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        wm.startActivity(intent);
                    }

            });
            dialog.setCancel("取消", null);
            dialog.show(wm);
            */
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

        String macAddress = (String)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "mac");
        if(macAddress == null || macAddress.isEmpty())
        {
            macAddress = _GetPhoneMAC();
            JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "mac", macAddress);
        }

        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "autoLogin", autoLogin);
        JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "autoRecordPassword", autoRecordPassword);

        loginViewModel.SignIn(username, password, macAddress, mCallback);
        //显示Loading动画
        if(loadingImageView != null)
            loadingImageView.setVisibility(View.VISIBLE);

        if(animationDrawable != null)
            animationDrawable.start();
    }

    public String _GetVersionName()
    {
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return this.getString(R.string.version_name) + info.versionName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return this.getString(R.string.can_not_find_version_name);
        }
    }

    private void _ShowLoadingAnim()
    {
        //实例化自定义CustomProgressDialog
        progressDialog = new CustomProgressDialog(LoginActivity.this, R.style.progressDialog);
        //设置不可点击外边取消动画
        progressDialog.setCanceledOnTouchOutside(false);

        if(progressDialog != null)
        {
            //动画显示或者隐藏
            progressDialog.show();
        }
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

    private void updateUiWithUser(LoggedInUserView model)
    {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString)
    {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    //关闭Activity
    private void _FinishActivity()
    {
        if (this != null)
            this.finish();
    }

    //退出应用程序
    private void _ExitApplication()
    {
        try
        {
            _FinishActivity();
            // 退出JVM,释放所占内存资源,0表示正常退出
            System.exit(0);
            // 从系统中kill掉应用程序
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    public void SendTextMail(String toMailAddress, String sAuthCode)
    {
        ShareUtils.putString(BaseApplication.GetInstance(), "FROM_ADD", "cagehe@qq.com");
        ShareUtils.putString(BaseApplication.GetInstance(), "FROM_PSW", "mecxacfqolplcbbg");
        ShareUtils.putString(BaseApplication.GetInstance(), "HOST", "smtp.qq.com");
        ShareUtils.putString(BaseApplication.GetInstance(), "PORT", "587");

        SendMailUtil.send(toMailAddress, sAuthCode);
        Toast.makeText(LoginActivity.this, "邮件已发送", Toast.LENGTH_SHORT).show();
    }//mecxacfqolplcbbg

    @Override
    protected void onDestroy()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        super.onDestroy();
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
    public float getSizeInDp()
    {
        return 640;
    }
}