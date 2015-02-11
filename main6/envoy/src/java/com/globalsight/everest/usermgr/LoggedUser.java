package com.globalsight.everest.usermgr;

public class LoggedUser
{
    private static UserInfo user = null;
    private static LoggedUser loggedUser = new LoggedUser();

    private LoggedUser()
    {
    }

    public static LoggedUser getInstance()
    {
        if (loggedUser == null)
        {
            loggedUser = new LoggedUser();
        }
        return loggedUser;
    }

    public UserInfo getLoggedUserInfo()
    {
        return user;
    }

    public void setLoggedUserInfo(UserInfo userInfo)
    {
        user = userInfo;
    }
}
