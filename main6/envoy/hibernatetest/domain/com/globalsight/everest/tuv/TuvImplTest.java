package com.globalsight.everest.tuv;

import java.sql.Timestamp;
import java.util.Date;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

public class TuvImplTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from TuvImpl";

	public void testSave()
	{
		try
		{
			TuvImpl tuvImpl = new TuvImpl();

			TuImpl tuImpl = new TuImpl();
			// tuImpl.setId(1);
			tuImpl.setOrder(System.currentTimeMillis());
			tuImpl.setTmId(System.currentTimeMillis());
			tuImpl.setDataType("data1");
			tuImpl.setTuType("tu_type1");
			tuImpl.setLocalizableType('L');// only in {L,T}

			LeverageGroupImpl leverageGroup = new LeverageGroupImpl();
			tuImpl.setLeverageGroup(leverageGroup);
			tuImpl.setPid(System.currentTimeMillis());
			tuImpl.setSourceTmName("zh");

			HibernateUtil.save(leverageGroup);
			HibernateUtil.save(tuImpl);

			tuvImpl.setOrder(System.currentTimeMillis());
			tuvImpl.setGlobalSightLocale((GlobalSightLocale) HibernateUtil.get(
					GlobalSightLocale.class, 1));
			tuvImpl.setTu(tuImpl);
			tuvImpl.setIsIndexed(true);
			tuvImpl.setSegmentClob("setSegmentClob");
			tuvImpl.setSegmentString("segmentString");
			tuvImpl.setWordCount(10);
			tuvImpl.setExactMatchKey(System.currentTimeMillis());
			tuvImpl.setState(TuvState.NOT_LOCALIZED); // only in
														// {NOT_LOCALIZED,LOCALIZED,OUT_OF_DATE}
			tuvImpl.setMergeState(TuvImpl.NOT_MERGED); // only in

			Date date = new Date();
			tuvImpl.setTimestamp(new Timestamp(date.getTime()));
			tuvImpl.setLastModified(date);
			tuvImpl.setLastModifiedUser("evan");
			tuvImpl.setCreatedDate(date);
			tuvImpl.setCreatedUser("Evan");
			tuvImpl.setUpdatedProject("UpdatedProject");

			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(tuvImpl);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = tuvImpl.getId();
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
			TuvImpl tuvImpl = (TuvImpl) HibernateUtil
					.get(TuvImpl.class, obj_id);
			if (tuvImpl != null)
			{
				System.out.println("Geted object id is " + tuvImpl.getId());
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
			TuvImpl tuvImpl = (TuvImpl) HibernateUtil
					.get(TuvImpl.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(tuvImpl);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
