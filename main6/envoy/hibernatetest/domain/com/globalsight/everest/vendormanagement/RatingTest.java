package com.globalsight.everest.vendormanagement;

import java.util.Date;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.vendormanagement.Vendor;
import com.globalsight.everest.taskmanager.TaskImpl;

import junit.framework.TestCase;

public class RatingTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from Rating";

	public void testSave()
	{
		Rating rating = new Rating();
		
		Vendor vendor = new Vendor();
		vendor.setId(System.currentTimeMillis());
		
		TaskImpl taskImpl = new TaskImpl();
		taskImpl.setId(System.currentTimeMillis());
		
		rating.setVendor(vendor);
		rating.setTask(taskImpl);
		rating.setRating(12);
		Date date = new Date();
		rating.setModifiedDate(date);
		rating.setRaterUserId("userId");
		rating.setComment("m_comment");
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(rating);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = rating.getId();
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
			Rating rating = (Rating) HibernateUtil.get(Rating.class,
					obj_id);
			if (rating != null)
			{
				System.out.println("Geted object id is " + rating.getId());
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
			Rating rating = (Rating) HibernateUtil.get(Rating.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(rating);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
