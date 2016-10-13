package com.globalsight.everest.securitymgr;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class VendorFieldSecurityTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from VendorFieldSecurity";

	public void testSave()
	{
		VendorFieldSecurity vendorFieldSecurity = new VendorFieldSecurity();
		vendorFieldSecurity.setVendorId(123456);
		vendorFieldSecurity.setSecurityXml("SecurityXml");
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(vendorFieldSecurity);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = vendorFieldSecurity.getId();
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
			VendorFieldSecurity vendorFieldSecurity = (VendorFieldSecurity) HibernateUtil
					.get(VendorFieldSecurity.class, obj_id);
			if (vendorFieldSecurity != null)
			{
				System.out.println("Geted object id is "
						+ vendorFieldSecurity.getId());
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
			VendorFieldSecurity vendorFieldSecurity = (VendorFieldSecurity) HibernateUtil
					.get(VendorFieldSecurity.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(vendorFieldSecurity);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
