package com.globalsight.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.globalsight.util.Constants;
import com.globalsight.util.RubyHelper;

public class RubyHelperTestCase extends TestCase
{
	private String hostName = "";

	private String port = "";

	private String userName = "";

	private String password = "";

	public static void main(String[] args)
	{
		junit.swingui.TestRunner.run(RubyHelperTestCase.class);
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		hostName = "csdn.net";
		port = "80";
		userName = "name";
		password = "pwd";
	}

	/*
	 * Test method for 'com.globalsight.util.RubyHelper.writeLines(String,
	 * String, String, String)'
	 */
	public void testWriteLines()
	{
		try
		{
			RubyHelper.writeLines(hostName, port, userName, password, false);

			String token1 = "$server_url = ";
			String token2 = "$username = ";
			String token3 = "$password = ";
			
			List lines = readLines(Constants.RUBY_PARAMETER);
			for (int j = 0; j < lines.size(); j++)
			{
				String line = (String) lines.get(j);
				if (line.startsWith(token1))
				{
					String temp = getStringAfterToken(line, token1);
					assertEquals("\"http://" + hostName + ":" + port
							+ "/globalsight/wl\"", temp);
				}
				if (line.startsWith(token2))
				{
					String temp = getStringAfterToken(line, token2);
					assertEquals("\"" + userName + "\"", temp);
				}
				if (line.startsWith(token3))
				{
					String temp = getStringAfterToken(line, token3);
					assertEquals("\"" + password + "\"", temp);
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private String getStringAfterToken(String p_all_str, String p_token)
	{
		String result = "";
		result = p_all_str.substring(p_all_str.indexOf(p_token)
				+ p_token.length());
		return result;
	}

	private static List readLines(String p_fileName) throws IOException
	{
		List lines = new ArrayList();
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new FileReader(p_fileName));
			String line;

			while ((line = br.readLine()) != null)
			{
				lines.add(line);
			}
			return lines;
		}
		finally
		{
			if (br != null)
			{
				br.close();
			}
		}
	}
}
