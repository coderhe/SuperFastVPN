package com.android.ppp.data;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.android.ppp.adapter.HttpUtils;
import com.android.ppp.algorithm.Rc4sha1;
import com.android.ppp.config.Config;
import com.android.ppp.data.model.LoggedInUser;
import com.android.ppp.ui.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import com.android.ppp.config.Status;
import com.android.ppp.ui.login.SignUpActivity;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

public class LoginDataSource
{
    public Result<LoggedInUser> SignIn(String username, String password, String imei, LoginActivity.Callback cb)
    {
        try {
            JSONObject params = new JSONObject();
            params.put("EMail", username);
            params.put("Password", password);
            params.put("Mac", imei);

            HttpUtils.post(Config.SIGN_IN_URL, HttpUtils.OKHttpPost(params), new okhttp3.Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    if(Config.CanLog)
                        Log.e("SIGNIN", Config.SIGN_IN_URL);
                    cb.LoginResult(Status.UnknownError);
                }

                @Override
                public void onResponse(Call call, Response respo) throws IOException
                {
                    String data = respo.body().string();
                    if(Config.CanLog)
                        Log.e("SIGNIN", data);

                    JSONObject result = null;
                    try {
                        result = new JSONObject(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        int nStatus = result.getInt("Error");
                        Status status = Status.values()[nStatus];
                        cb.LoginResult(status);
                        if(status == Status.Success)
                        {
                            try {
                                String serverInfo = Rc4sha1.Decrypt(result.getString("Tag"));
                                result = new JSONObject(serverInfo);
                                RouteData.Routes = result.getString("Regions");
                                AppData.mUserUUId = result.getString("Id");
                                AppData.mUserType = result.getInt("Type");
                                AppData.mUserEmail = result.getString("Email");
                                AppData.mPassword = result.getString("Password");
                                AppData.mUserMac = result.getString("Mac");
                                AppData.mUserExpirationTime = result.getString("ExpirationTime");
                                AppData.mUseIncomingTraffic = result.getLong("UseIncomingTraffic");
                                AppData.mUseOutgingTraffic = result.getLong("UseOutgingTraffic");
                                AppData.mRemainIncomingTraffic = result.getLong("RemainIncomingTraffic");
                                AppData.mRemainOutgoingTraffic = result.getLong("RemainOutgoingTraffic");
                                AppData.mResetTrafficTime = result.getString("ResetTrafficTime");
                                AppData.mServerTime = result.getString("ServerTime");

                                JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "email", AppData.mUserEmail);
                                JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "password", AppData.mPassword);
                                if(Config.CanLog)
                                    Log.e("SIGNIN Success", serverInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            LoggedInUser user = new LoggedInUser(username, password);
            return new Result.Success<>(user);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error sign in", e));
        }
    }

    public Result<LoggedInUser> SignUp(String username, String password, String imei, SignUpActivity.Callback cb)
    {
        try
        {
            JSONObject params = new JSONObject();
            params.put("EMail", username);
            params.put("Password", password);

            LoggedInUser[] user = {new LoggedInUser(username, password)};
            HttpUtils.post(Config.SIGN_UP_URL, HttpUtils.OKHttpPost(params), new okhttp3.Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    if(Config.CanLog)
                        Log.e("SIGNUP", Config.SIGN_UP_URL);
                    cb.LoginResult(Status.UnknownError);
                }

                @Override
                public void onResponse(Call call, Response respo) throws IOException
                {
                    String data = respo.body().string();
                    if(Config.CanLog)
                        Log.e("SIGNUP", data);

                    JSONObject result = null;
                    try {
                        result = new JSONObject(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try
                    {
                        int nStatus = result.getInt("Error");
                        Status status = Status.values()[nStatus];
                        if(status == Status.Success)
                        {
                            AppData.mUserEmail = username;
                            AppData.mPassword = password;
                            JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "email", AppData.mUserEmail);
                            JsonUtils.ModifyElement(Config.APP_PACKAGE_PATH + "/" + Config.APP_USER_NAME, "user", "password", AppData.mPassword);

                            user[0] = new LoggedInUser(username, password);
                            if(Config.CanLog)
                                Log.e("SIGN_UP", "SIGNUP Success");
                        }
                        else
                        {
                            AppData.mUserEmail = "";
                            AppData.mPassword = "";
                            user[0] = new LoggedInUser("", "");
                        }
                        cb.LoginResult(status);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            return new Result.Success<>(user[0]);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error sign up", e));
        }
    }

    //test-code
    public String HttpPostTest()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("https://www.baidu.com/").get().build();
        String data = "11111";
        //Response response = client.newCall(request).execute();
        final Call call = client.newCall(request);
        //Log.e("SIGNIN", response.body().toString());

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("error","出错");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Log.e("sucess", data);
            }
        });

        return data;
    }
}