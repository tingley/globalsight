package com.globalsight.everest.tuv;

import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;

import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class TaskTuvTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from TaskTuv";

	public void testSave()
	{
		try
		{
			TaskTuv taskTuv = new TaskTuv();

			TaskImpl taskImpl = new TaskImpl();
			taskImpl.setId(System.currentTimeMillis());

			TuvImpl currentTuv = new TuvImpl();
			currentTuv.setId(System.currentTimeMillis());

			TuvImpl previousTuv = new TuvImpl();
			previousTuv.setId(234);

			taskTuv.setCurrentTuv(currentTuv);
			taskTuv.setTask(taskImpl);
			taskTuv.setVersion(2);
			taskTuv.setPreviousTuv(previousTuv);
			taskTuv.setTaskName("task_name");
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(taskTuv);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = taskTuv.getId();
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
			String sql = "select id from TASK_TUV t where t.id=?";
			
			Session session = HibernateUtil.getSession();
			
			System.out.println(obj_id);
			
			List list = session.createSQLQuery(sql).setParameter(0, new Long(obj_id)).list();
			assertTrue(list.size() > 0);
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
			TaskTuv taskTuv = (TaskTuv) HibernateUtil
					.get(TaskTuv.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(taskTuv);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
