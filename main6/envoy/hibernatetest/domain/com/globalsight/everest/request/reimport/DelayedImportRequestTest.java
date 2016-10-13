package com.globalsight.everest.request.reimport;

import junit.framework.TestCase;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class DelayedImportRequestTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from DelayedImportRequest";

	public void testSave()
	{
		DelayedImportRequest delayedImportRequest = new DelayedImportRequest();

		RequestImpl requestImpl = new RequestImpl();
		requestImpl.setId(System.currentTimeMillis());
		delayedImportRequest.setRequest(requestImpl);
		
		delayedImportRequest.setName("test4");
		delayedImportRequest.setGxml("m_gxml");
		delayedImportRequest.setExternalPageId("1201");
		SourcePage previousPage = new SourcePage();
		previousPage.setId(System.currentTimeMillis());
		delayedImportRequest.setPreviousPage(previousPage);
		delayedImportRequest.setDataSourceType("");
		delayedImportRequest.setSourceEncoding("utf-8");
		
		Timestamp timestamp = new Timestamp();
		delayedImportRequest.setTime(timestamp);
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(delayedImportRequest);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = delayedImportRequest.getId();
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
			DelayedImportRequest delayedImportRequest = (DelayedImportRequest) HibernateUtil.get(DelayedImportRequest.class,
					obj_id);
			if (delayedImportRequest != null)
			{
				System.out.println("Geted object id is " + delayedImportRequest.getId());
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
			DelayedImportRequest delayedImportRequest = (DelayedImportRequest) HibernateUtil.get(DelayedImportRequest.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(delayedImportRequest);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
