package com.globalsight.action;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.globalsight.ui.AmbOptionPane;
import com.globalsight.util.ConfigureHelper;
import com.globalsight.util.Constants;

public class ExecAction extends Action
{

	static Logger log = Logger.getLogger(ExecAction.class.getName());

	public String execute(String[] p_args) throws Exception
	{
		try
		{
			Process process = Runtime.getRuntime().exec(p_args);
			
			if(ConfigureHelper.canCatchExecError())
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				StringBuffer msg = new StringBuffer("Message: \n");
				String line = "";
				while((line = reader.readLine())!=null)
				{
					msg.append("\n" + line);
				}
				
				msg.append("\n\nError: \n");
				BufferedReader error_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String error_line = "";
				while((error_line = error_reader.readLine())!=null)
				{
					msg.append("\n" + error_line);
				}
				
				AmbOptionPane.showMessageDialog("Message", msg.toString());
				log.info(msg);
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String execute(String[] p_args, String[] p_envp) throws Exception
	{
		try
		{
			Process process = Runtime.getRuntime().exec(p_args, p_envp);
			
			if(ConfigureHelper.canCatchExecError())
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				StringBuffer msg = new StringBuffer("Message: \n");
				String line = "";
				while((line = reader.readLine())!=null)
				{
					msg.append("\n" + line);
				}
				
				msg.append("\n\nError: \n");
				BufferedReader error_reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				String error_line = "";
				while((error_line = error_reader.readLine())!=null)
				{
					msg.append("\n" + error_line);
				}
				
				AmbOptionPane.showMessageDialog("Message", msg.toString());
				log.info(msg);
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
