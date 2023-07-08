package com.android.ppp.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Intent;
import android.util.Log;
import android.util.Patterns;

import com.android.ppp.data.LoginRepository;
import com.android.ppp.data.Result;
import com.android.ppp.data.model.LoggedInUser;
import com.android.ppp.R;

import javax.security.auth.callback.Callback;

public class LoginViewModel extends ViewModel
{
    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void SignIn(String username, String password, String imei, LoginActivity.Callback cb)
    {
        Result<LoggedInUser> result = loginRepository.SignIn(username, password, imei, cb);
        if (result instanceof Result.Success)
        {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        }
        else
        {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void SignUp(String username, String password, String imei, SignUpActivity.Callback cb)
    {
        Result<LoggedInUser> result = loginRepository.SignUp(username, password, imei, cb);
        if (result instanceof Result.Success)
        {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
        }
        else
            loginResult.setValue(new LoginResult(R.string.sign_up_failed));
    }

    public void loginDataChanged(String username, String password)
    {
        if (!isUserNameValid(username))
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        else if (!isPasswordValid(password))
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        else
            loginFormState.setValue(new LoginFormState(true));
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username)
    {
        if (username == null)
            return false;

        if (username.contains("@"))
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        else
            return !username.trim().isEmpty();
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password)
    {
        return password != null && password.trim().length() > 0 && password.trim().length() < 21;
    }
}