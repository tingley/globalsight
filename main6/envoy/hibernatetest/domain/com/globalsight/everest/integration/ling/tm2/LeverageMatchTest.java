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

package com.globalsight.everest.integration.ling.tm2;

import java.util.List;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

public class LeverageMatchTest extends TestCase
{
	private String derek = "derek";

	public void testSave() throws Exception
	{
		int resultSize = listResult().size();

		LeverageMatch leverageMatch = new LeverageMatch();

		leverageMatch.setOriginalSourceTuvId(1);
		leverageMatch.setSubId(derek);
		leverageMatch.setOrderNum((short) 1);

		GlobalSightLocale locale = (GlobalSightLocale) HibernateUtil.get(
				GlobalSightLocale.class, new Long(1));
		leverageMatch.setTargetLocale(locale);

		leverageMatch.setSourcePageId(new Long(1));
		leverageMatch.setMatchedClob(derek);
		leverageMatch.setMatchedText(derek);
		leverageMatch.setMatchedTuvId(1);
		leverageMatch.setMatchType(derek);
		leverageMatch.setScoreNum((short) 1);

		HibernateUtil.save(leverageMatch);

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
	    String hql = "from LeverageMatch lm where lm.subId = 'derek'";
		return HibernateUtil.search(hql);
	}
}
