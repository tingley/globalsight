package com.globalsight.mediasurface;


import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class CmsUserInfoTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from CmsUserInfo";

	public void testSave()
	{
		CmsUserInfo cmsUserInfo = new CmsUserInfo();
		cmsUserInfo.setAmbassadorUserId("CmsUserInfo_test4" + System.currentTimeMillis());
		cmsUserInfo.setCmsUserId("1");
		cmsUserInfo.setCmsPassword("password");

		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(cmsUserInfo);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = cmsUserInfo.getId();
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
			CmsUserInfo cmsUserInfo = (CmsUserInfo) HibernateUtil.get(
					CmsUserInfo.class, obj_id);
			if (cmsUserInfo != null)
			{
				System.out.println("Geted object id is " + cmsUserInfo.getId());
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
			CmsUserInfo cmsUserInfo = (CmsUserInfo) HibernateUtil.get(
					CmsUserInfo.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(cmsUserInfo);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
