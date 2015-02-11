package com.globalsight.util;

import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * use ConfigureHelperV2 when handling configure.xml, and use this when getting
 * properties
 * 
 * @author quincy.zou
 * 
 */
public class ConfigureHelper
{

	static Logger log = Logger.getLogger(ConfigureHelper.class.getName());

	static Properties m_properties = new Properties();
	
	static final int MAX_FILE_SIZE = 35;

	static long lasttime = 0l;

	static StringBuffer header;
	static
	{
		header = new StringBuffer();
		header.append("####################################################");
		header.append("\n# ATTACHED_FILE_SIZE, the max size of attachment in");
		header.append("\n# job comments : number (<=35), defalult 35");
		header.append("\n####################################################");
		header.append("\n# CATCH_EXE_ERROR, catch runtime error if needed,");
		header.append("\n# this makes app hanged: true|false, default false");
		header.append("\n####################################################");
		header.append("\n# CHECK_DEPENDENCE, check DI dependence, like RUBY, ");
		header.append("\n# Firewatir on Mac OS : true|false, default true ");
		header.append("\n####################################################");
		header.append("\n# CHECK_ADD_SAME_FILE, check same file when adding ");
		header.append("\n# files to job : true|false, default true");
		header.append("\n####################################################");
		header.append("\n# CHECK_ADD_SAME_FILE_TIMES : s|m, s stands only ");
		header.append("\n# check once, m means check multi-times, default m ");
		header.append("\n####################################################");
		header.append("\n# CLEAN_AFTER_CREATED, clean job panel after ");
		header.append("\n# created job : true|false, default true");
		header.append("\n####################################################");
		header.append("# BASE_FOLDER, LOCATION");
		header.append("\n####################################################");
		/*
         * Load properties and check file (configure.xml) exist or not. If
         * configure file does not exist, then create it. If occur exceptions,
         * log them and exit application
         */
		checkProperties();
	}

	// //////////////////////////////////////////////////////////////
	// everything about configure.xml will invoked ConfigureHelperV2
	// methods from Version 3.0
	// //////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////
	// properties
	// //////////////////////////////////////////////////////////////

	public static String getAttachedFileSize() throws Exception
	{
		String result = getProperty("ATTACHED_FILE_SIZE");
		try
		{
			int size = Integer.parseInt(result);
			if (size > MAX_FILE_SIZE || size <= 0)
			{
				result = Integer.toString(MAX_FILE_SIZE);
				setAttachedFileSize(MAX_FILE_SIZE);
			}
		}
		catch (Exception e)
		{
            setAttachedFileSize(MAX_FILE_SIZE);
            result = Integer.toString(MAX_FILE_SIZE);
		}
		return result;
	}

	private static boolean setAttachedFileSize(int size)
	{
		try
		{
			setProperty("ATTACHED_FILE_SIZE", String.valueOf(size));
			return true;
		}
		catch (Exception e)
		{
			logError("write attachedfilesize");
			return false;
		}
	}

	public static String getBaseFolder()
	{
		try
		{
			return getProperty("BASE_FOLDER");
		}
		catch (Exception e)
		{
			return ".";
		}
	}

	public static boolean setBaseFolder(String basefolder)
	{
		try
		{
			setProperty("BASE_FOLDER", basefolder);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public static boolean canCatchExecError()
	{
		boolean result = false;
		try
		{
			String str = getProperty("CATCH_EXE_ERROR");
			if (str.equalsIgnoreCase("true"))
			{
				result = true;
			}
			else if (!str.equalsIgnoreCase("false"))
			{
				turnOff_catchexecerror();
			}
		}
		catch (Exception e)
		{
		}

		return result;
	}

	public static boolean turnOff_catchexecerror()
	{
		try
		{
			setProperty("CATCH_EXE_ERROR", "false");
			return true;
		}
		catch (Exception e)
		{
			logError("turnOff_catchexecerror");
			return false;
		}
	}

	public static boolean turnOn_catchexecerror()
	{
		try
		{
			setProperty("CATCH_EXE_ERROR", "true");
			return true;
		}
		catch (Exception e)
		{
			logError("turnOn_catchexecerror");
			return false;
		}
	}

	public static boolean canCheckDependence()
	{
		boolean result = true;
		try
		{
			String str = getProperty("CHECK_DEPENDENCE");
			if (str.equalsIgnoreCase("false"))
			{
				result = false;
			}
			else if (!str.equalsIgnoreCase("true"))
			{
				turnOnCheckDependence();
			}
		}
		catch (Exception e)
		{
		}
		return result;
	}

	public static void turnOffCheckDependence()
	{
		try
		{
			setProperty("CHECK_DEPENDENCE", "false");
		}
		catch (Exception e)
		{
			logError("turnOffCheck");
		}
	}

	public static void turnOnCheckDependence()
	{
		try
		{
			setProperty("CHECK_DEPENDENCE", "true");
		}
		catch (Exception e)
		{
			logError("turnOnCheck");
		}
	}

	public static boolean checkAddSameFile()
	{
		boolean result = false;
		try
		{
			String str = getProperty("CHECK_ADD_SAME_FILE");
			if (str.equalsIgnoreCase("true"))
			{
				result = true;
			}
			else if (!str.equalsIgnoreCase("false"))
			{
				turnOnCheckAddSameFile();
			}
		}
		catch (Exception e)
		{
		}

		return result;
	}

	public static void turnOffCheckAddSameFile()
	{
		try
		{
			setProperty("CHECK_ADD_SAME_FILE", "false");
		}
		catch (Exception e)
		{
			logError("turnOffCheckAddSameFile");
		}
	}

	public static void turnOnCheckAddSameFile()
	{
		try
		{
			setProperty("CHECK_ADD_SAME_FILE", "true");
		}
		catch (Exception e)
		{
			logError("turnOnCheckAddSameFile");
		}
	}

	public static boolean isCheckAddSameFileOnce()
	{
		boolean result = false;
		try
		{
			String str = getProperty("CHECK_ADD_SAME_FILE_TIMES");
			if (str.equalsIgnoreCase("s"))
			{
				result = true;
			}
			else if (!str.equalsIgnoreCase("m"))
			{
				setProperty("CHECK_ADD_SAME_FILE_TIMES", "m");
			}
		}
		catch (Exception e)
		{
		}

		return result;
	}

	public static boolean isCleanAfterCreateJob()
	{
		boolean result = true;
		try
		{
			String str = getProperty("CLEAN_AFTER_CREATED");
			if (str.toLowerCase().equalsIgnoreCase("false"))
			{
				result = false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return result;
	}

	public static Point getLocation()
	{
		try
		{
			String str = getProperty("LOCATION");
			if (str != null && !str.equals(""))
			{
				String x = str.substring(0, str.indexOf("_"));
				String y = str.substring(str.indexOf("_") + 1);
				Point p = new Point();
				p.setLocation(Double.parseDouble(x), Double.parseDouble(y));
				return p;
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static boolean setLocation(Point p_point)
	{
		try
		{
			setProperty("LOCATION", p_point.getX() + "_" + p_point.getY());
			return true;
		}
		catch (Exception e)
		{
			logError("write location");
			return false;
		}
	}

	private static String getProperty(String key)
	{
		checkProperties();
		String result = m_properties.getProperty(key);
		return (result == null) ? null : result.trim();
	}

	private static void checkProperties()
	{
		File file = new File(Constants.DI_PROPERTIES_FILE);
		long time = file.lastModified();
		if (lasttime < time)
		{
			try
			{
				FileInputStream in = new FileInputStream(file);
				m_properties.load(in);
				in.close();
				lasttime = time;
			}
			catch (Exception e)
			{
				log.error("error when loading " + Constants.DI_PROPERTIES_FILE,
						e);
				System.exit(1);
			}
		}
	}

	private static void setProperty(String key, String value)
	{
		m_properties.setProperty(key, value.trim());
		try
		{
			File file = new File(Constants.DI_PROPERTIES_FILE);
			FileOutputStream out = new FileOutputStream(file);
			m_properties.store(out, header.toString());
			out.close();
			lasttime = file.lastModified();
		}
		catch (FileNotFoundException e)
		{
			log.error(e.getMessage(), e);
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
		}
	}

	private static void logError(String p_msg)
	{
		log.error("Can't " + p_msg + " to configure.xml.");
	}

	// //////////////////////////////////////////////////////////////
	// test method
	// //////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		try
		{
			String s = getAttachedFileSize();
			System.out.println(s);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
