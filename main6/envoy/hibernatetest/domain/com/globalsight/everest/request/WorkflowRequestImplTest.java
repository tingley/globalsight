package com.globalsight.everest.request;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;

public class WorkflowRequestImplTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from WorkflowRequestImpl";

	public void testSave()
	{
		try
		{
			WorkflowRequestImpl workflowRequestImpl = new WorkflowRequestImpl();

			JobImpl jobImpl = new JobImpl();
			jobImpl.setId(System.currentTimeMillis());

			workflowRequestImpl.setJob(jobImpl);

			workflowRequestImpl
					.setTypeStr("ADD_WORKFLOW_REQUEST_TO_EXISTING_JOB");
			workflowRequestImpl.setExceptionXml("exceptionXml");

			ArrayList templateList = new ArrayList();

			WorkflowTemplateInfo workflowTemplateInfo = new WorkflowTemplateInfo();
			workflowTemplateInfo.setId(System.currentTimeMillis());
			templateList.add(workflowTemplateInfo);

			workflowRequestImpl.setWorkflowTemplateList(templateList);
			int record_count = 0;
			try
			{

				record_count = HibernateUtil.count(record_cound_hql);	
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}

			HibernateUtil.save(workflowRequestImpl);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = workflowRequestImpl.getId();
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
			WorkflowRequestImpl workflowRequestImpl = (WorkflowRequestImpl) HibernateUtil
					.get(WorkflowRequestImpl.class, obj_id);
			if (workflowRequestImpl != null)
			{
				System.out.println("Geted object id is "
						+ workflowRequestImpl.getId());
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
			WorkflowRequestImpl workflowRequestImpl = (WorkflowRequestImpl) HibernateUtil
					.get(WorkflowRequestImpl.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(workflowRequestImpl);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
