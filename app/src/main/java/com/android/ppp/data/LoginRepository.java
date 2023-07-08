package com.android.ppp.data;

import android.os.Handler;
import android.util.Log;

import com.android.ppp.data.model.LoggedInUser;
import com.android.ppp.ui.login.LoginActivity;
import com.android.ppp.ui.login.SignUpActivity;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository
{
    private static volatile LoginRepository instance;
    private LoginDataSource dataSource;
    private LoggedInUser user = null;

    private LoginRepository(LoginDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static LoginRepository getInstance(LoginDataSource dataSource)
    {
        if (instance == null)
            instance = new LoginRepository(dataSource);

        return instance;
    }

    public boolean isLoggedIn()
    {
        return user != null;
    }

    private void setLoggedInUser(LoggedInUser user)
    {
        this.user = user;
    }

    public Result<LoggedInUser> SignIn(String username, String password, String imei, LoginActivity.Callback cb)
    {
        // handle login
        Result<LoggedInUser> result = dataSource.SignIn(username, password, imei, cb);
        if (result instanceof Result.Success)
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());

        return result;
    }

    public Result<LoggedInUser> SignUp(String username, String password, String imei, SignUpActivity.Callback cb)
    {
        Result<LoggedInUser> result = dataSource.SignUp(username, password, imei, cb);
        if (result instanceof Result.Success)
            setLoggedInUser(((Result.Success<LoggedInUser>) result).getData());

        return result;
    }
}