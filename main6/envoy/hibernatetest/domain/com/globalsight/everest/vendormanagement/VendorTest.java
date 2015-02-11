package com.globalsight.everest.vendormanagement;

import java.util.Date;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;
import java.sql.Timestamp;

public class VendorTest extends TestCase
{

	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from Vendor";

	public void testSave()
	{
		Vendor vendor = new Vendor();
		vendor.setName("Vendor_test4");
		vendor.setCustomVendorId(String.valueOf(System.currentTimeMillis()));
		vendor.setFirstName("Evan");
		vendor.setLastName("Zeng");
		vendor.setPseudonym("p_pseudonym" + String.valueOf(System.currentTimeMillis()));
		vendor.setTitle("p_title");
		vendor.setAddress("shangHai");
		vendor.setCompanyName("p_companyName");
		vendor.setCountry("p_country");
		vendor.setNationalities("p_nationalities");
		vendor.setDob("m_dob");
		vendor.setNotes("p_notes");
		vendor.setResume("p_resume");
		vendor.setResumeFilename("filename");
		vendor.setStatus("p_status");
		vendor.setIsInternalVendor(true);
		vendor.setUseInAmbassador(true);
		vendor.setIsInAllProjects(true);
		vendor.setDefaultUILocale("zh");
		vendor.setUserId("evan_zeng");
		Date date = new Date();
		vendor.setTimestamp(new Timestamp(date.getTime()));
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(vendor);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = vendor.getId();
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
			Vendor vendor = (Vendor) HibernateUtil.get(Vendor.class,
					obj_id);
			if (vendor != null)
			{
				System.out.println("Geted object id is " + vendor.getId());
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
			Vendor vendor = (Vendor) HibernateUtil.get(Vendor.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(vendor);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
