package com.globalsight.everest.util.comparator;

import java.util.Comparator;
import java.util.Map;

import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.leverage.LeveragedTu;

public class ComparatorByTmOrder implements Comparator<LeveragedTu>
{

	private LeverageOptions options;
	
	public ComparatorByTmOrder(LeverageOptions options) 
	{
		this.options = options;
	}

	public int compare(LeveragedTu tu1, LeveragedTu tu2)
	{
		int result = 0;
		if(options.isTmProcedence())
		{
			int projectTmIndex1 = getProjectTmIndex(tu1.getTmId(), options);
			int projectTmIndex2 = getProjectTmIndex(tu2.getTmId(), options);
			result = projectTmIndex1 - projectTmIndex2;
		}
		return result;
	}
	
	private int getProjectTmIndex(long tmId, LeverageOptions options)
	{
		Map<Long, Integer> map = options.getTmIndexsToLeverageFrom();
		return map.get(tmId);
	}
}
