package com.globalsight.everest.tuv;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.tuv.TuImpl;
public class LeverageGroupImplTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from LeverageGroupImpl";

	public void testSave()
	{
		LeverageGroupImpl leverageGroupImpl = new LeverageGroupImpl();
		List tusSet = new ArrayList();
		TuImpl tuImpl = new TuImpl();
		tuImpl.setId(22222);
		tusSet.add(tuImpl);
		leverageGroupImpl.setTusSet(tusSet);
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(leverageGroupImpl);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = leverageGroupImpl.getId();
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
			LeverageGroupImpl leverageGroupImpl = (LeverageGroupImpl) HibernateUtil
					.get(LeverageGroupImpl.class, obj_id);
			if (leverageGroupImpl != null)
			{
				System.out.println("Geted object id is "
						+ leverageGroupImpl.getId());
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
			LeverageGroupImpl leverageGroupImpl = (LeverageGroupImpl) HibernateUtil
					.get(LeverageGroupImpl.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(leverageGroupImpl);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
