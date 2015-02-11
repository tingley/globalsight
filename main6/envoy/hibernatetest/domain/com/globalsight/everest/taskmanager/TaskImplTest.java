package com.globalsight.everest.taskmanager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.globalsight.everest.comment.CommentImpl;
import com.globalsight.everest.costing.AmountOfWork;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.vendormanagement.Rating;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class TaskImplTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from TaskImpl";

	public void testSave()
	{
		TaskImpl taskImpl = new TaskImpl();
		
		WorkflowImpl workflowImpl = new WorkflowImpl();
		workflowImpl.setId(System.currentTimeMillis());
		
		taskImpl.setWorkflow(workflowImpl);
		taskImpl.setName("p_name");
		
		taskImpl.setStateStr(Task.STATE_ACTIVE_STR);
		taskImpl.setEstimatedAcceptanceDate(new Date());
		taskImpl.setEstimatedCompletionDate(new Date());
//
		taskImpl.setAcceptedDate(new Date());
//		Date date = new Date();
		taskImpl.setCompletedDate(new Date());
		
		
		Rate expenseRate = new Rate();
		expenseRate.setId(System.currentTimeMillis());
		Rate revenueRate = new Rate();
		revenueRate.setId(System.currentTimeMillis());
		
		
		taskImpl.setExpenseRate(expenseRate);
		taskImpl.setRevenueRate(revenueRate);
		taskImpl.setStfCreationState("");
		taskImpl.setAcceptor("Evan");
		taskImpl.setRateSelectionCriteria(1); // only in {1,2}
		taskImpl.setTypeStr("REVIEW");
		taskImpl.setTaskType(TaskImpl.TYPE_TRANSLATION);
		taskImpl.setCompanyId("1");
		
		Rating rating = new Rating();
		rating.setId(System.currentTimeMillis());
		
		ArrayList rateList = new ArrayList();
		rateList.add(rating);
		
		ArrayList commentList = new ArrayList();
		CommentImpl commentImpl = new CommentImpl();
		commentImpl.setId(System.currentTimeMillis());
		commentList.add(commentImpl);
		
		Set workSet = new HashSet();
		AmountOfWork amountOfWork = new AmountOfWork();
		amountOfWork.setId(System.currentTimeMillis());
		workSet.add(amountOfWork);
		
		taskImpl.setRatings(rateList);
		taskImpl.setTaskComments(commentList);
		taskImpl.setWorkSet(workSet);
		
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(taskImpl);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = taskImpl.getId();
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
			TaskImpl taskImpl = (TaskImpl) HibernateUtil.get(TaskImpl.class,
					obj_id);
			if (taskImpl != null)
			{
				System.out.println("Geted object id is " + taskImpl.getId());
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
			TaskImpl taskImpl = (TaskImpl) HibernateUtil.get(TaskImpl.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(taskImpl);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
