package com.globalsight.everest.projecthandler;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;
import java.util.Date;
public class ProjectTMTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from ProjectTM";

	public void testSave()
	{
		ProjectTM projectTM = new ProjectTM();
		projectTM.setName("ProjectTM_test4");
		projectTM.setCompanyId("1");
		projectTM.setOrganization("aug");

		projectTM.setDescription("ProjectTM_description!");
		Date date = new Date();
		projectTM.setCreationDate(date);
		projectTM.setDomain("localhost");
		projectTM.setCreationUser("Evan");
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(projectTM);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = projectTM.getId();
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
			ProjectTM projectTM = (ProjectTM) HibernateUtil.get(ProjectTM.class,
					obj_id);
			if (projectTM != null)
			{
				System.out.println("Geted object id is " + projectTM.getId());
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
			ProjectTM projectTM = (ProjectTM) HibernateUtil.get(ProjectTM.class,
					obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(projectTM);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
