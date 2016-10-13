package com.globalsight.action;

import com.globalsight.bo.ConfigureBO;

public class ConfigureServerAction extends Action{

    public String execute(String args[]) throws Exception
    {                
        ConfigureBO configureBO = new ConfigureBO();
        boolean isXml = configureBO.configureServerXml(args[0], args[1]);
        boolean isRuby = configureBO.configureAllRuby(args[0], args[1], "", "", false);
        if(isXml == true && isRuby == true)
        {
            return "Success";
        }
        else
        {
        	return "Fail";
        }
    }
}
