package com.globalsight.everest.workflow;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class ActivityTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from Activity";

	public void testSave()
	{
		Activity activity = new Activity();
		activity.setName("Activity_test4");
		activity.setDisplayName("Activity_test4_displayName");
		activity.setCompanyId("1");
		activity.setDescription("Activity_description!");
		activity.setIsActive(true);
		activity.setUseType(Activity.USE_TYPE_TRANS);
		activity.setType_str(Activity.TYPE_TRANSLATE_STR);
		activity.setIsEditable(true);
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(activity);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = activity.getId();
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
//			String hql = "from WorkflowImpl";
//			String hql = "from JobImpl j where 1=1  and j.state in (null,'READY_TO_BE_DISPATCHED') or j.workflowInstanceSet.state in (null,'READY_TO_BE_DISPATCHED') and j.workflowInstanceSet.state <> :cancelled";
			String hql = "select j from JobImpl j inner join j.workflowInstanceSet w where 1=1  and j.state in (null,'READY_TO_BE_DISPATCHED') or w.state in 'READY_TO_BE_DISPATCHED'";
			HibernateUtil.search(hql);
//			Activity activity = (Activity) HibernateUtil.get(Activity.class,
//					obj_id);
//			if (activity != null)
//			{
//				System.out.println("Geted object id is " + activity.getId());
//			}
//			else
//			{
//				assertFalse(true);
//			}
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
			Activity activity = (Activity) HibernateUtil.get(Activity.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(activity);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
