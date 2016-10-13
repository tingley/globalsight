package com.globalsight.everest.projecthandler;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.globalsight.everest.foundation.LeverageLocales;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

public class WorkflowTemplateInfoTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from WorkflowTemplateInfo";

	public void testSave() throws Exception
	{
		ProjectImpl projectImp = new ProjectImpl();
		projectImp.setId(111111);
		GlobalSightLocale sourceLocal = (GlobalSightLocale) HibernateUtil.get(
				GlobalSightLocale.class, 1);
		GlobalSightLocale targetLocal = (GlobalSightLocale) HibernateUtil.get(
				GlobalSightLocale.class, 3);
		
		Set leverageLocales = new HashSet();
		LeverageLocales leverageLocale = new LeverageLocales();
		leverageLocale.setLocale((GlobalSightLocale) HibernateUtil.get(
				GlobalSightLocale.class, 1));
		leverageLocale.setIsActive(true);
		leverageLocale.setName("testLeverageLocal");
		leverageLocales.add(leverageLocale);
		
		WorkflowTemplateInfo workflowTemplateInfo = new WorkflowTemplateInfo();
		workflowTemplateInfo.setName("test_workflowT");
		workflowTemplateInfo.setDescription("only for test");
		workflowTemplateInfo.setProject(projectImp);
		workflowTemplateInfo.setSourceLocale(sourceLocal);
		workflowTemplateInfo.setTargetLocale(targetLocal);
		workflowTemplateInfo.setEncoding("utf-8");
		workflowTemplateInfo.setWorkflowTemplateId(1101);
		workflowTemplateInfo.setIsActive(true);
		workflowTemplateInfo.setNotifyPm(true);
		workflowTemplateInfo.setWorkflowType("TRANS");
		workflowTemplateInfo.setCompanyId("1");
		workflowTemplateInfo.setLeveragingLocalesSet(leverageLocales);
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(workflowTemplateInfo);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = workflowTemplateInfo.getId();
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
			WorkflowTemplateInfo workflowTemplateInfo = (WorkflowTemplateInfo) HibernateUtil.get(WorkflowTemplateInfo.class,
					obj_id);
			if (workflowTemplateInfo != null)
			{
				System.out.println("Geted object id is " + workflowTemplateInfo.getId());
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
			WorkflowTemplateInfo workflowTemplateInfo = (WorkflowTemplateInfo) HibernateUtil.get(WorkflowTemplateInfo.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(workflowTemplateInfo);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
