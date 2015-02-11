package com.globalsight.everest.secondarytargetfile;

import java.util.Date;

import junit.framework.TestCase;

import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class SecondaryTargetFileTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from SecondaryTargetFile";

	public void testSave()
	{
		SecondaryTargetFile secondaryTargetFile = new SecondaryTargetFile();

		WorkflowImpl workflowImpl = new WorkflowImpl();
		workflowImpl.setId(System.currentTimeMillis());

		secondaryTargetFile.setWorkflow(workflowImpl);

		secondaryTargetFile.setStoragePath("p_storagePath");
		secondaryTargetFile.setModifierUserId("p_modifierUserId");

		secondaryTargetFile.setLastUpdatedTime(new Date().getTime());
		secondaryTargetFile.setFileSize(64);
		secondaryTargetFile.setEventFlowXml("p_eventFlowXml");
		secondaryTargetFile.setState("ACTIVE_JOB"); // only in
													// {ACTIVE_JOB,CANCELLED,LOCALIZED,OUT_OF_DATE,EXPORTED,EXPORT_IN_PROGRESS,EXPORT_FAIL}
		secondaryTargetFile.setIsActive(true);
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(secondaryTargetFile);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = secondaryTargetFile.getId();
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
			SecondaryTargetFile secondaryTargetFile = (SecondaryTargetFile) HibernateUtil
					.get(SecondaryTargetFile.class, obj_id);
			if (secondaryTargetFile != null)
			{
				System.out.println("Geted object id is "
						+ secondaryTargetFile.getId());
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
			SecondaryTargetFile secondaryTargetFile = (SecondaryTargetFile) HibernateUtil
					.get(SecondaryTargetFile.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(secondaryTargetFile);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
