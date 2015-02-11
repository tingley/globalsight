package com.globalsight.everest.workflowmanager;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
public class WorkflowOwnerTest extends TestCase
{
private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from WorkflowOwner";

	public void testSave()
	{
		WorkflowOwner workflowOwner = new WorkflowOwner();
		WorkflowImpl workflowImpl = new WorkflowImpl();
		workflowImpl.setId(1111111);
		
		workflowOwner.setWorkflow(workflowImpl);
		workflowOwner.setOwnerId("owner_id");
		workflowOwner.setOwnerType("ProjectManager"); // only in {ProjectManager, WorkflowManager}
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(workflowOwner);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = workflowOwner.getId();
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
			WorkflowOwner workflowOwner = (WorkflowOwner) HibernateUtil.get(WorkflowOwner.class,
					obj_id);
			if (workflowOwner != null)
			{
				System.out.println("Geted object id is " + workflowOwner.getId());
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
			WorkflowOwner workflowOwner = (WorkflowOwner) HibernateUtil.get(WorkflowOwner.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(workflowOwner);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
