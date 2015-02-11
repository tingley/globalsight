package com.globalsight.bo;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.globalsight.entity.Host;
import com.globalsight.entity.User;
import com.globalsight.util.RubyHelper;
import com.globalsight.util.UsefulTools;
import com.globalsight.util2.ConfigureHelperV2;

public class ConfigureBO
{
    static Logger log = Logger.getLogger(ConfigureBO.class.getName());
    
	public boolean configureServerXml(String hostName, String port)
			throws Exception
	{
		Host h = new Host(hostName, port);
		if (ConfigureHelperV2.writeHost(h) != null)
		{
			return true;
		}
		else
			return false;
	}

	public boolean configureUser(User u)
			throws Exception
	{
		if (ConfigureHelperV2.writeDefaultUser(u) != null)
		{
			return true;
		}
		else
			return false;
	}

	public boolean configureAllRuby(String hostName, String port,
			String userName, String password, boolean useSSL)
	{
	    try
		{
			RubyHelper.writeLines(hostName, port, userName, password, useSSL);
			return true;
		}
		catch (IOException e)
		{
		    // Added for GBS-1392. 
	        // When cann't write ruby file successfully, then ignore.
            if (UsefulTools.isWindowsVista())
            {
                return true;
            }
		    
			return false;
		}
	}
}
