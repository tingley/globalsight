package com.globalsight.cxe.entity.cms.teamsite.server;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.entity.cms.teamsite.store.BackingStoreImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class TeamSiteServerImplTest extends TestCase
{
	private static long obj_id; // test get and delete method
	public void testSave()
	{
		try
		{
			Session s = HibernateUtil.getSession();
			Transaction tx = s.beginTransaction();

			Vector backingStores = new Vector();
			BackingStoreImpl backingStore = new BackingStoreImpl();
			backingStore.setName("s");
			backingStores.add(backingStore);

			TeamSiteServerImpl teamSiteServer = new TeamSiteServerImpl();
			teamSiteServer.setCompanyId("123456789");
			teamSiteServer.setName("teamsite_server");
			teamSiteServer.setDescription("only for test");
			teamSiteServer.setOperatingSystem("linux");
			teamSiteServer.setExportPort(8080);
			teamSiteServer.setImportPort(7001);
			teamSiteServer.setProxyPort(8000);
			teamSiteServer.setHome("home of the teamSiteServer");
			teamSiteServer.setUser("evan");
			teamSiteServer.setUserPass("password");
			teamSiteServer.setType("CMS");
			teamSiteServer.setMount("mount_dir");
			teamSiteServer.setAllowLocaleSpecificReimport(true);
			Date date = new Date();
			teamSiteServer.setTimestamp(new Timestamp(date.getTime()));
			teamSiteServer.setBackingStores(backingStores);
			s.save(teamSiteServer);
			tx.commit();
			s.close();
			obj_id = teamSiteServer.getId();
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
			TeamSiteServerImpl teamSiteServer = (TeamSiteServerImpl) HibernateUtil
					.get(TeamSiteServerImpl.class, obj_id);
			if (teamSiteServer != null)
			{
				System.out.println("Geted teamSiteServer's name is "
						+ teamSiteServer.getName());
			}
			else
			{
				System.out
						.println("Can not Gete teamSiteServer, please check the program!");
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
			TeamSiteServerImpl teamSiteServer = (TeamSiteServerImpl) HibernateUtil
					.get(TeamSiteServerImpl.class, obj_id);
			count = HibernateUtil.search("from TeamSiteServerImpl", null).size();
			System.out.println("count is " + count);
			HibernateUtil.delete(teamSiteServer);
			assertFalse(count == HibernateUtil.search("from TeamSiteServerImpl",
					null).size());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
