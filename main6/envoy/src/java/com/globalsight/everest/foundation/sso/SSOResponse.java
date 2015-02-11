package com.globalsight.everest.foundation.sso;

public class SSOResponse
{
    private boolean loginSucess = false;
    private String statusCode;
    private String statusMessage;
    private String userName;
    private String userId;
    private String inResponseTo;
    private String companyName;

    public boolean isLoginSucess()
    {
        return loginSucess;
    }

    public void setLoginSuccess(boolean loginSucess)
    {
        this.loginSucess = loginSucess;
    }

    public String getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(String statusCode)
    {
        this.statusCode = statusCode;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage)
    {
        this.statusMessage = statusMessage;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getInResponseTo()
    {
        return inResponseTo;
    }

    public void setInResponseTo(String inResponseTo)
    {
        this.inResponseTo = inResponseTo;
    }

    public String getCompanyName()
    {
        return companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

}
