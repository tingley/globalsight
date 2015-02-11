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

package com.globalsight.calendar;

import java.util.List;

import junit.framework.TestCase;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class HolidayTest extends TestCase
{
	private String derek = "derek";

	public void testSave() throws Exception
	{
        int resultSize = listResult().size();

		Holiday holiday = new Holiday();

		holiday.setName(derek);
		holiday.setDescription(derek);
		holiday.setTimeExpression(derek);
		holiday.setCompanyId("1");
		holiday.setMonth(1);
		holiday.setDayOfMonth(1);
		holiday.setWeekOfMonth("1");
		holiday.setDayOfWeek(new Integer(1));
		holiday.setEndingYear(new Integer(1));
		holiday.setIsAbsolute(true);

		HibernateUtil.save(holiday);

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

	public void testGet() throws Exception
	{
		Holiday christmas = (Holiday) HibernateUtil.get(Holiday.class, 1);
		Holiday newYear = (Holiday) HibernateUtil.get(Holiday.class, 2);
		
		assertTrue(christmas.getName().equals("Christmas Day"));
		assertTrue(newYear.getName().equals("New Year's Day"));
	}

    private List listResult()
    {
        String hql = "from Holiday h where h.name = '" + derek + "'";
        return HibernateUtil.search(hql);
    }
}
