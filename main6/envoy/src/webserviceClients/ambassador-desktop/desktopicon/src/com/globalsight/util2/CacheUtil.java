package com.globalsight.util2;

import java.util.Enumeration;
import java.util.Hashtable;

import com.globalsight.entity.User;

/**
 * A cache util class introduced in DesktopIcon V3.0
 * 
 * @author quincy.zou
 */
public class CacheUtil
{
	private static CacheUtil obj = null;

	private static Hashtable m_cache = null;

	private static String key_user = User.class.getName();

	private static String key_user_login = User.class.getName() + "-login";

	private CacheUtil()
	{
		m_cache = new Hashtable();
	}

	/**
     * Use this method to get an static object of CacheUtil
     * 
     * @return an instance of CacheUtil
     */
	public static CacheUtil getInstance()
	{
		if (obj == null)
		{
			obj = new CacheUtil();
		}

		return obj;
	}

	private void put(Object key, Object value)
	{
		m_cache.put(key, value);
	}

	private Object get(Object key)
	{
		return m_cache.get(key);
	}

	private int size()
	{
		return m_cache.size();
	}

	private Enumeration keys()
	{
		return m_cache.keys();
	}

	private Enumeration elements()
	{
		return m_cache.elements();
	}

	private boolean containsKey(Object key)
	{
		return m_cache.containsKey(key);
	}

	private boolean containsValue(Object value)
	{
		return m_cache.containsValue(value);
	}

	private Object remove(Object key)
	{
		return m_cache.remove(key);
	}

	/**
     * get the user who have login
     * 
     * @return
     */
	public User getCurrentUser()
	{
		try
		{
			CacheUtil c = CacheUtil.getInstance();
			User user = null;
			Object obj = c.get(key_user);
			if (obj != null)
			{
				user = (User) obj;
			}

			return user;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
     * set the user who have login, if user == null, remove current user
     * 
     * @param user
     * @return
     */
	public User setCurrentUser(User user)
	{
		try
		{
			CacheUtil c = CacheUtil.getInstance();
			if (user != null)
			{
				c.put(key_user, user);
			}
			else
			{
				c.remove(key_user);
			}

			return user;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
     * get the user who want to login
     * 
     * @return
     */
	public User getLoginingUser()
	{
		try
		{
			CacheUtil c = CacheUtil.getInstance();
			return (User) c.get(key_user_login);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
     * set the user who want to login, if user == null, remove logining user
     * 
     * @return
     * 
     */
	public User setLoginingUser(User user)
	{
		try
		{
			CacheUtil c = CacheUtil.getInstance();
			if (user != null)
			{
				c.put(key_user_login, user);
			}
			else
			{
				c.remove(key_user_login);
			}

			return user;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
