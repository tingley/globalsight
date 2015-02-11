/*
 * Copyright (c) 2000-2002 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

package com.globalsight.everest.foundation;

import java.util.List;

import junit.framework.TestCase;

import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

public class LeverageLocalesTest extends TestCase
{
	private long infoId = 777;
	
	public void testSave() throws Exception
	{
		int resultSize = listResult().size();

		LeverageLocales locales = new LeverageLocales();

		WorkflowTemplateInfo info = new WorkflowTemplateInfo();
		info.setId(infoId);
		locales.setBackPointer(info);

		locales.setIsActive(true);
		locales.setLocale((GlobalSightLocale) HibernateUtil.get(
				GlobalSightLocale.class, 1));

		HibernateUtil.save(locales);

		assertTrue(resultSize != listResult().size());
	}

	public void testFind() throws Exception
	{
		assertTrue(listResult().size() != 0);
	}

	public void testDelete() throws Exception
	{
		List result = listResult();

		HibernateUtil.delete(result);

		assertTrue(result.size() != listResult().size());
	}

	private List listResult()
	{
		String hql = "from LeverageLocales l where l.backPointer = " + infoId;
		return HibernateUtil.search(hql);
	}
}
