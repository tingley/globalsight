package com.globalsight.action;

import javax.net.ssl.SSLException;
import javax.swing.JOptionPane;

import com.globalsight.bo.ConfigureBO;
import com.globalsight.bo.LoginBO;
import com.globalsight.entity.User;
import com.globalsight.exception.NotSupportHttpsException;
import com.globalsight.ui.AmbOptionPane;
import com.globalsight.ui.UserOptionsPanel;
import com.globalsight.util.WebClientHelper;
import com.globalsight.util2.CacheUtil;

public class LoginAction extends Action
{
	public static String success = "success";

	public static String fail = "fail";
	public static User USER = null;

	/**
     * when login successfullt, then save user into configure.xml as default.
     */
	public String execute(String[] args) throws Exception
	{
		User user = CacheUtil.getInstance().getLoginingUser();
		if (user == null)
		{
			throw new Exception(
					"invoke CacheUtil.getInstance().setLoginingUser(User) first ");
		}
		LoginBO loginBO = new LoginBO();
		String result = "";
        try
        {
            result = loginBO.login(user.getName(), user.getPassword());
        }
        catch (Exception e)
        {
            if (e instanceof SSLException || e.getCause() instanceof SSLException)
            {
                if (user.isUseSSL())
                {
                    boolean flag = false;
                    try
                    {
                        user.setUseSSL(false);
                        result = loginBO.login(user.getName(), user
                                .getPassword());
                        flag = true;
                    }
                    catch (Exception e2)
                    {
                        //ignore
                    }
                    
                    if (flag)
                    {
                        int useHttp = AmbOptionPane.showConfirmDialog(
                                UserOptionsPanel.notHttpsForLogin,  "Info", JOptionPane.YES_NO_OPTION);
                        if (useHttp != JOptionPane.YES_OPTION)
                        {
                            user.setUseSSL(true);
                            throw new NotSupportHttpsException();
                        }
                    }
                    else
                    {
                        boolean reset = WebClientHelper.installCert(user.getHost().getName(), user.getHost().getPort());
                        if (reset)
                        {
                            return restartDI;
                        }
                        else
                        {
                            throw e;
                        }
                    }
                }
            }
            else
            {
                throw e;
            }
        }
		String separator = "+_+";
		int index = result.indexOf(separator);
		String accessToken1 = result;
		if (index != -1)
		{
			accessToken1 = result.substring(0, index);
			String companyName = result.substring(index + separator.length());
			user.setCompanyName(companyName);
		}
		setAccessToken(accessToken1);

		ConfigureBO configureBO = new ConfigureBO();
		boolean writeToXml = configureBO.configureUser(user);
		boolean writeToRuby = configureBO.configureAllRuby(user.getHost()
				.getName(), user.getHost().getPortString(), user.getName(),
				user.getPassword(), user.isUseSSL());
		if (writeToXml && writeToRuby)
		{
		    this.USER = user;
			return success;
		}
		else
		{
			return fail;
		}
	}
}
