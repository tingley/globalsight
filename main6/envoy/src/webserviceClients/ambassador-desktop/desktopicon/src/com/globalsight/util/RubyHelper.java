package com.globalsight.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RubyHelper
{
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

	//If one needn't be changed, set the vaule "".
	public static void writeLines(String p_hostName, String p_port,
			String p_userName, String p_password, boolean useSSL) throws IOException
	{
		writeOneFile(Constants.RUBY_PARAMETER, p_hostName, p_port, p_userName, p_password, useSSL);
		writeSafariRubyFile(p_hostName, p_port, p_userName, p_password, useSSL);
		writeFirefoxRubyFile(p_hostName, p_port, p_userName, p_password, useSSL);
	}

	private static void writeFirefoxRubyFile(String p_hostName, String p_port, String p_userName, String p_password, boolean useSSL) throws IOException
	{
		String [] filenames = {
				Constants.RUBY_INPROGRESS_FF,
				Constants.RUBY_REPORT_FF,
				Constants.RUBY_PENDING_FF
		};
		
		for (int i = 0; i < filenames.length; i++)
		{
			String p_fileName = filenames[i];
			List lines = readLines(p_fileName);
			PrintWriter pw = null;
			String token1 = "browser = FireWatir::Firefox.start(";
			String token2 = "browser.text_field(:name, \"nameField\").set(";
			String token3 = "browser.text_field(:name, \"passwordField\").set(";

			try
			{
				pw = new PrintWriter(new FileOutputStream(p_fileName));
				for (Iterator itor = lines.iterator(); itor.hasNext();)
				{
					String line = (String) itor.next();
					line = line.trim();
					if (line.startsWith(token1) && p_hostName.trim() != ""
							&& p_port.trim() != "")
					{
						line = token1 + (useSSL ? "\"https://" : "\"http://") + p_hostName.trim() + ":" + p_port
								+ "/globalsight/wl\")";
					}
					else if (line.startsWith(token2) && p_userName.trim() != "")
					{
						line = token2 + "\"" + p_userName + "\")";
					}
					else if (line.startsWith(token3) && p_password.trim() != "")
					{
						line = token3 + "\"" + p_password + "\")";
					}
					line = line + "\n";
					pw.print(line);
				}
			}
			finally
			{
				if (pw != null)
				{
					pw.close();
				}
			}
		}
	}

	private static void writeSafariRubyFile(String p_hostName, String p_port, String p_userName, String p_password, boolean useSSL) throws IOException
	{
		String [] filenames = {
				Constants.RUBY_INPROGRESS_SF,
				Constants.RUBY_REPORT_SF,
				Constants.RUBY_PENDING_SF,
				Constants.RUBY_GOTOURL_SF
		};
		
		for (int i = 0; i < filenames.length; i++)
		{
			String p_fileName = filenames[i];
			List lines = readLines(p_fileName);
			PrintWriter pw = null;
			String token1 = "browser.goto(";
			String token2 = "browser.text_field(:name, \"nameField\").set(";
			String token3 = "browser.text_field(:name, \"passwordField\").set(";

			try
			{
				pw = new PrintWriter(new FileOutputStream(p_fileName));
				for (Iterator itor = lines.iterator(); itor.hasNext();)
				{
					String line = (String) itor.next();
					line = line.trim();
					if (line.startsWith(token1) && p_hostName.trim() != ""
							&& p_port.trim() != "")
					{
						line = token1 + (useSSL ? "\"https://" : "\"http://") + p_hostName.trim() + ":" + p_port
								+ "/globalsight/wl\")";
					}
					else if (line.startsWith(token2) && p_userName.trim() != "")
					{
						line = token2 + "\"" + p_userName + "\")";
					}
					else if (line.startsWith(token3) && p_password.trim() != "")
					{
						line = token3 + "\"" + p_password + "\")";
					}
					line = line + "\n";
					pw.print(line);
				}
			}
			finally
			{
				if (pw != null)
				{
					pw.close();
				}
			}
		}
	}

	private static void writeOneFile(String p_fileName, String p_hostName,
			String p_port, String p_userName, String p_password, boolean useSSL) throws IOException
	{
		List lines = readLines(p_fileName);
		PrintWriter pw = null;
		String token1 = "$server_url = ";
		String token2 = "$username = ";
		String token3 = "$password = ";

		try
		{
			pw = new PrintWriter(new FileOutputStream(p_fileName));
			for (Iterator itor = lines.iterator(); itor.hasNext();)
			{
				String line = (String) itor.next();
				line = line.trim();
				if (line.startsWith(token1) && p_hostName.trim() != ""
						&& p_port.trim() != "")
				{
					line = token1 + (useSSL ? "\"https://" : "\"http://") + p_hostName.trim() + ":" + p_port
							+ "/globalsight/wl\"";
				}
				else if (line.startsWith(token2) && p_userName.trim() != "")
				{
					line = token2 + "\"" + p_userName + "\"";
				}
				else if (line.startsWith(token3) && p_password.trim() != "")
				{
					line = token3 + "\"" + p_password + "\"";
				}
				line = line + "\n";
				pw.print(line);
			}
		}
		finally
		{
			if (pw != null)
			{
				pw.close();
			}
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			writeLines("httpfakjfla", "7001", "user", "pwd", false);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
