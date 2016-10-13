package com.globalsight.everest.vendormanagement;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.costing.Rate;

public class VendorRoleTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from VendorRole";

	public void testSave()
	{
		VendorRole vendorRole = new VendorRole();

		Vendor vendor = new Vendor();
		vendor.setId(System.currentTimeMillis());
		Activity activity = new Activity();
		activity.setId(System.currentTimeMillis());
		LocalePair localePair = new LocalePair();
		localePair.setId(System.currentTimeMillis());
		Rate rate = new Rate();
		rate.setId(System.currentTimeMillis());

		vendorRole.setVendor(vendor);
		vendorRole.setActivity(activity);
		vendorRole.setLocalePair(localePair);
		vendorRole.setRate(rate);
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(vendorRole);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = vendorRole.getId();
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
			VendorRole vendorRole = (VendorRole) HibernateUtil.get(
					VendorRole.class, obj_id);
			if (vendorRole != null)
			{
				System.out.println("Geted object id is " + vendorRole.getId());
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
			VendorRole vendorRole = (VendorRole) HibernateUtil.get(
					VendorRole.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(vendorRole);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
