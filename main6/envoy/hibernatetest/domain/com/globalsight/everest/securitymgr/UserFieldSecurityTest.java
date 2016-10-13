package com.globalsight.everest.securitymgr;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class UserFieldSecurityTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from UserFieldSecurity";

	public void testSave()
	{
		UserFieldSecurity userFieldSecurity = new UserFieldSecurity();
		userFieldSecurity.setUsername("UserFieldSecurity_test4");
		userFieldSecurity.setSecurityXml("securityXml");
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(userFieldSecurity);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = userFieldSecurity.getId();
			}
			else
			{
				assertFalse(true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testGet()
	{
		try
		{
			UserFieldSecurity userFieldSecurity = (UserFieldSecurity) HibernateUtil
					.get(UserFieldSecurity.class, obj_id);
			if (userFieldSecurity != null)
			{
				System.out.println("Geted object id is "
						+ userFieldSecurity.getId());
			}
			else
			{
				assertFalse(true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}

	}

	public void testDelete()
	{
		int count = 0;
		try
		{
			UserFieldSecurity userFieldSecurity = (UserFieldSecurity) HibernateUtil
					.get(UserFieldSecurity.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(userFieldSecurity);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
