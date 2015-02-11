package com.globalsight.everest.projecthandler;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
public class LeverageProjectTMTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from LeverageProjectTM";

	public void testSave()
	{
		LeverageProjectTM projectTM = new LeverageProjectTM();
		projectTM.setProjectTmId(1111111);
		TranslationMemoryProfile translationMemoryProfile = new TranslationMemoryProfile();
		translationMemoryProfile.setId(1253);
		projectTM.setTMProfile(translationMemoryProfile);
		
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
			LeverageProjectTM projectTM = (LeverageProjectTM) HibernateUtil
					.get(LeverageProjectTM.class, obj_id);
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
			LeverageProjectTM projectTM = (LeverageProjectTM) HibernateUtil
					.get(LeverageProjectTM.class, obj_id);
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
