package com.globalsight.everest.integration.ling.tm2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;

import junit.framework.TestCase;

public class LeverageMatchLingManagerLocalTest extends TestCase
{

	LeverageMatchLingManagerLocal leverageMatchLingManagerLocal = new LeverageMatchLingManagerLocal();

	protected void setUp() throws Exception
	{
		super.setUp();
	}

	public void testGetFuzzyMatches()
	{
		long sourcePageId = 1011;
		long targetLocaleId = 86;
		HashMap map = leverageMatchLingManagerLocal.getFuzzyMatches(new Long(
				sourcePageId), new Long(targetLocaleId));
//		assertTrue(map != null);
	}

	public void testGetTuvMatches()
	{
		Long p_sourceTuvId = new Long(0);
		Long p_targetLocaleId = new Long(0);
		String p_subId = "";
                /* true added by Andrew, not sure it's right */
		SortedSet set = leverageMatchLingManagerLocal.getTuvMatches(
				p_sourceTuvId, p_targetLocaleId, p_subId, true);
//		assertTrue(set != null);
	}

	public void testGetExactMatches()
	{
		Long p_sourcePageId = new Long(0);
		Long p_targetLocaleId = new Long(0);
		HashMap map = leverageMatchLingManagerLocal.getExactMatches(
				p_sourcePageId, p_targetLocaleId);
//		assertTrue(map != null);
	}

	public void testGetMatchTypesForStatistics()
	{
		Long p_sourcePageId = new Long(0);
		Long p_targetLocaleId = new Long(0);
		int p_levMatchThreshold = 0;

		MatchTypeStatistics typeStatistics = leverageMatchLingManagerLocal
				.getMatchTypesForStatistics(p_sourcePageId, p_targetLocaleId,
						p_levMatchThreshold);
//		assertTrue(typeStatistics != null);

	}

	public void testSaveLeveragedMatches()
	{
		Collection p_leverageMatchList = new ArrayList();
		LeverageMatch leverageMatch = new LeverageMatch();
		leverageMatch.setMatchedTuvId(1001);
		p_leverageMatchList.add(leverageMatch);
		leverageMatchLingManagerLocal.saveLeveragedMatches(p_leverageMatchList);
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
}
