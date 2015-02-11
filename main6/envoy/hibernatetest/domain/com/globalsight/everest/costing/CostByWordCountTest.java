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

package com.globalsight.everest.costing;

import java.util.List;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class CostByWordCountTest extends TestCase
{
	private long ID = 888;

	public void testSave() throws Exception
	{
        int resultSize = listResult().size();

		CostByWordCount costByWordCount = new CostByWordCount();

		costByWordCount.setRepetitionCost(0.0f);
		costByWordCount.setContextMatchCost(0.0f);
		costByWordCount.setSegmentTmMatchCost(0.0f);
		costByWordCount.setLowFuzzyMatchCost(0.0f);
		costByWordCount.setMedFuzzyMatchCost(0.0f);
		costByWordCount.setMedHiFuzzyMatchCost(0.0f);
		costByWordCount.setHiFuzzyMatchCost(0.0f);
		costByWordCount.setNoMatchCost(0.0f);

		Cost cost = new Cost();
		cost.setId(ID);
		costByWordCount.setCost(cost);

		HibernateUtil.save(costByWordCount);

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
        String hql = "from CostByWordCount a where a.cost = " + ID;
        return HibernateUtil.search(hql);
    }
}
