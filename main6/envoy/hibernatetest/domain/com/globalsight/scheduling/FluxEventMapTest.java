package com.globalsight.scheduling;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

// Note the id is string type
public class FluxEventMapTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from FluxEventMap";

	public void testSave()
	{
		FluxEventMap fluxEventMap = new FluxEventMap();
		fluxEventMap.setEventType(2);
		fluxEventMap.setDomainObjectId(1);
		fluxEventMap.setDomainObjectType(1);
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(fluxEventMap);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = fluxEventMap.getId();
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
			FluxEventMap fluxEventMap = (FluxEventMap) HibernateUtil.get(
					FluxEventMap.class, String.valueOf(obj_id));
			if (fluxEventMap != null)
			{
				System.out
						.println("Geted object id is " + fluxEventMap.getId());
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
			FluxEventMap fluxEventMap = (FluxEventMap) HibernateUtil.get(
					FluxEventMap.class, String.valueOf(obj_id));
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(fluxEventMap);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
