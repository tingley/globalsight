/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
 * 
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF GLOBALSIGHT
 * CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT IN CONFIDENCE.
 * INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED OR DISCLOSED IN WHOLE OR
 * IN PART EXCEPT AS PERMITTED BY WRITTEN AGREEMENT SIGNED BY AN OFFICER OF
 * GLOBALSIGHT CORPORATION.
 * 
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER SECTIONS 104
 * AND 408 OF TITLE 17 OF THE UNITED STATES CODE. UNAUTHORIZED USE, COPYING OR
 * OTHER REPRODUCTION IS PROHIBITED BY LAW.
 */

package com.globalsight.cxe.entity.cms.teamsite.store;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class BackingStoreImplTest extends TestCase
{
	private static long obj_id; // test get and delete method

	private String record_cound_hql = "select count(*) from BackingStoreImpl";

	public void testSave()
	{
		BackingStoreImpl backingStore = new BackingStoreImpl();
		backingStore.setIsActive(true);
		backingStore.setName("backingStore_test2");
		try
		{
			int record_count = HibernateUtil.count(record_cound_hql);
			HibernateUtil.save(backingStore);
			if (HibernateUtil.count(record_cound_hql) - record_count == 1)
			{
				System.out.println("Save successful!");
				obj_id = backingStore.getId();
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
			BackingStoreImpl backingStore = (BackingStoreImpl) HibernateUtil
					.get(BackingStoreImpl.class, obj_id);
			if (backingStore != null)
			{
				System.out
						.println("Geted object id is " + backingStore.getId());
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
			BackingStoreImpl backingStore = (BackingStoreImpl) HibernateUtil
					.get(BackingStoreImpl.class, obj_id);
			count = HibernateUtil.count(record_cound_hql);
			System.out.println("count is " + count);
			HibernateUtil.delete(backingStore);
			assertFalse(count == HibernateUtil.count(record_cound_hql));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			assertFalse(true);
		}
	}

	// public void testUpdate()
	// {
	// BackingStoreImpl backingStore = new BackingStoreImpl();
	// backingStore.setId(10047);
	// backingStore.setName("backingStore_test1");
	// try
	// {
	// HibernateUtil.update(backingStore);
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// assertFalse(true);
	// }
	// }
}
