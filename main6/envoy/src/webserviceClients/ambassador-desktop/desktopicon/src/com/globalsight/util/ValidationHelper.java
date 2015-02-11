package com.globalsight.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

public class ValidationHelper
{

	static Logger log = Logger.getLogger(ValidationHelper.class.getName());

	/**
     * validate string empty or not.
     * 
     * @param p_str
     * @return true if string is not empty or null.
     */
	public static boolean validateEmptyString(String p_str)
	{
		if (p_str == null || p_str.trim().equals(""))
		{
			return false;
		}
		return true;
	}

	/**
     * validate URL.
     * 
     * @param p_hostName
     * @return true if string is not empty or null.
     */
	public static boolean validateHostName(String p_hostName)
	{
		String re = "[\\w-\\.]{1,}";

		if (p_hostName.matches(re))
		{
			return true;
		}

		return false;
	}

	/**
     * validate job name with "[\\w+-]{1,}" pattern (character, '+', '-')
     * 
     * @param p_jobname
     * @return
     */
	public static boolean validateJobName(String p_jobname)
	{
		String re = "[\\w+-.\\s+']{1,}";
		
		//replace space to "" before matching to allow space in job name
//		p_jobname = p_jobname.replaceAll("\\s+", "");
		//replace . to "" before matching to allow . in job name
//		p_jobname = p_jobname.replaceAll("\\.", "");
		
		if (p_jobname.matches(re))
		{
			return true;
		}

		return false;
	}

	/**
     * 
     * @param p_num
     * @return true if string is a positive number
     */
	public static boolean validatePositiveNumber(String p_num)
	{
		boolean result = true;
		try
		{
			int i = Integer.parseInt(p_num);
			if (i <= 0)
			{
				result = false;
			}
		}
		catch (Exception e)
		{
			result = false;
		}
		return result;
	}

}
