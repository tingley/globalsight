package com.globalsight.everest.snippet;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;
import com.globalsight.util.GlobalSightLocale;

public class SnippetImplTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from SnippetImpl";

	public void testSave()
	{
		SnippetImpl snippetImpl = new SnippetImpl();
		snippetImpl.setName("SnippetImpl_test4");

		snippetImpl.setDescription("SnippetImpl_description!");
		snippetImpl.setContentString("contenString");
		snippetImpl.setContentClob("contenClob");
		try
		{
			GlobalSightLocale local = (GlobalSightLocale) HibernateUtil.get(
					GlobalSightLocale.class, 1);
			snippetImpl.setLocale(local);
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(snippetImpl);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = snippetImpl.getId();
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
			SnippetImpl snippetImpl = (SnippetImpl) HibernateUtil.get(
					SnippetImpl.class, obj_id);
			if (snippetImpl != null)
			{
				System.out.println("Geted object id is " + snippetImpl.getId());
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
			SnippetImpl snippetImpl = (SnippetImpl) HibernateUtil.get(
					SnippetImpl.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(snippetImpl);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
