package com.globalsight.everest.tuv;


import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;
import com.globalsight.everest.tuv.LeverageGroupImpl;
public class TuImplTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from TuImpl";

	public void testSave()
	{
		TuImpl tuImpl = new TuImpl();
		tuImpl.setOrder(System.currentTimeMillis());
		tuImpl.setTmId(System.currentTimeMillis());
		
		tuImpl.setDataType("data1");
		tuImpl.setTuType("tu_type1");
		tuImpl.setLocalizableType('L');// only in {L,T}
		
		LeverageGroupImpl leverageGroup = new LeverageGroupImpl();
		tuImpl.setLeverageGroup(leverageGroup);
		tuImpl.setPid(System.currentTimeMillis());
		tuImpl.setSourceTmName("zh");
		try
		{
			HibernateUtil.save(leverageGroup);
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(tuImpl);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = tuImpl.getId();
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
			TuImpl tuImpl = (TuImpl) HibernateUtil.get(TuImpl.class,
					obj_id);
			if (tuImpl != null)
			{
				System.out.println("Geted object id is " + tuImpl.getId());
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
			TuImpl tuImpl = (TuImpl) HibernateUtil.get(TuImpl.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(tuImpl);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
