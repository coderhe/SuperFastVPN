package com.android.ppp.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView
{
    private String displayName;

    LoggedInUserView(String displayName) {
        this.displayName = displayName;
    }

    String getDisplayName() {
        return displayName;
    }
}