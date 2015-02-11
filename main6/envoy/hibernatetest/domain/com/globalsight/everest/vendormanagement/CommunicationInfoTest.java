package com.globalsight.everest.vendormanagement;

import java.sql.Timestamp;
import java.util.*;

import org.hibernate.Session;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class CommunicationInfoTest extends TestCase
{

	private String record_cound_hql = "select count(*) from CommunicationInfo";

	private static long vendor_id = 0;

	public void testSave() throws Exception
	{
		CommunicationInfo communicationInfo = new CommunicationInfo();

		Vendor vendor = new Vendor();
		vendor.setName("Vendor_test4");
		vendor.setCustomVendorId(String.valueOf(System.currentTimeMillis()));
		vendor.setFirstName("Evan");
		vendor.setLastName("Zeng");
		vendor.setPseudonym("p_pseudonym"
				+ String.valueOf(System.currentTimeMillis()));
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

		HibernateUtil.save(vendor);
		vendor_id = vendor.getId();
		communicationInfo.setType_char('W');
		communicationInfo.setVendor(vendor);
		communicationInfo.setValue("value");
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(communicationInfo);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
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
			Session session = HibernateUtil.getSession();
			String hql = "from CommunicationInfo c where c.vendor=?";
			List list = session.createQuery(hql).setLong(0, vendor_id).list();
			assertFalse(list.size() <= 0);
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
			Session session = HibernateUtil.getSession();
			String hql = "from CommunicationInfo c where c.vendor=?";
			List list = session.createQuery(hql).setLong(0, vendor_id).list();
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(list);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}

}
