package com.globalsight.everest.permission;

import com.globalsight.persistence.hibernate.HibernateUtil;

import junit.framework.TestCase;

public class PermissionGroupImplTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from PermissionGroupImpl";

	public void testSave()
	{
		PermissionGroupImpl permissionGroupImpl = new PermissionGroupImpl();
		permissionGroupImpl.setName("permissionGroupImpl_test4");
		permissionGroupImpl.setCompanyId("1");
		permissionGroupImpl.setDescription("permissionGroupImpl_description!");
		permissionGroupImpl.setPermissionSetAsString("123|345|456|");
		try
		{
			int record_count = HibernateUtil
					.count(record_cound_hql);
			HibernateUtil.save(permissionGroupImpl);
			if (HibernateUtil.count(record_cound_hql)
					- record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = permissionGroupImpl.getId();
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
			PermissionGroupImpl permissionGroupImpl = (PermissionGroupImpl) HibernateUtil
					.get(PermissionGroupImpl.class, obj_id);
			if (permissionGroupImpl != null)
			{
				System.out.println("Geted object id is "
						+ permissionGroupImpl.getId());
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
			PermissionGroupImpl permissionGroupImpl = (PermissionGroupImpl) HibernateUtil
					.get(PermissionGroupImpl.class, obj_id);
			count = HibernateUtil
					.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(permissionGroupImpl);
			assertFalse(count == HibernateUtil
					.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}
}
