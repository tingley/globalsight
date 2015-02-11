package com.globalsight.cxe.entity.cms.teamsitedbmgr;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;

import java.util.List;

public class TeamSiteBranchTest extends TestCase
{
	private long obj_id = 10566; // test get and delete method

	public void testSave()
	{
		TeamSiteBranch teamSiteBranch = new TeamSiteBranch();
		teamSiteBranch.setBranchLanguage(123456);
		teamSiteBranch.setBranchSource("en");
		teamSiteBranch.setBranchTarget("Cn");
		teamSiteBranch.setIsActive(true);
		teamSiteBranch.setName("teamSiteBranch_tese1");
		teamSiteBranch.setServer(1101);
		teamSiteBranch.setStore(2201);
		try
		{
			int record_count = HibernateUtil
					.search("from TeamSiteBranch", null).size();
			HibernateUtil.save(teamSiteBranch);
			if(((List)HibernateUtil.search("from TeamSiteBranch", null)).size() - record_count == 1)
			{
				System.out.println("Save successful!");
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
			TeamSiteBranch teamSiteBranch = (TeamSiteBranch) HibernateUtil.get(
					TeamSiteBranch.class, obj_id);
			if (teamSiteBranch != null)
			{
				System.out.println("get is ok!");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}

	}

	/**
	 * Test delete function
	 */
	public void testDelete()
	{
		try
		{
			int record_count = 0;
			TeamSiteBranch teamSiteBranch = (TeamSiteBranch) HibernateUtil.get(
					TeamSiteBranch.class, obj_id);
			if (teamSiteBranch != null)
			{
				System.out.println("get is ok!");
				List list = HibernateUtil.search("from TeamSiteBranch", null);
				record_count = list.size();
				HibernateUtil.delete(teamSiteBranch);
				list = HibernateUtil.search("from TeamSiteBranch", null);
				assertFalse(record_count == list.size());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}

	public void testUpdate()
	{

	}
}
