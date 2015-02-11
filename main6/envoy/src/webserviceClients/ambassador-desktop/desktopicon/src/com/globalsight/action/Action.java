package com.globalsight.action;

public abstract class Action {

	static volatile String accessToken = "";
	
	public static String restartDI = "restartDI";
    
    void setAccessToken(String accesstoken)
    {
        accessToken = accesstoken;
    }
    
    public abstract String execute(String[] args) throws Exception;
}
