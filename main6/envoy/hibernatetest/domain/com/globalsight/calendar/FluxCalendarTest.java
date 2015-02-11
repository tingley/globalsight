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

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class FluxCalendarTest extends TestCase
{
	private String derek = "derek";

	public void testSave() throws Exception
	{
        int resultSize = listResult().size();

		FluxCalendar fluxCalendar = new FluxCalendar();

		fluxCalendar.setName(derek);
		fluxCalendar.setHoursPerDay(0);
		fluxCalendar.setTimeZoneId(derek);
		fluxCalendar.setLastUpdatedBy(derek);
		fluxCalendar.setLastUpdatedTime(new Date());
		fluxCalendar.setIsDefault(false);
		fluxCalendar.setIsActive(true);
		fluxCalendar.setCompanyId("00");

		HibernateUtil.save(fluxCalendar);

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

	public void testManyToMany() throws Exception
	{
		Session session = HibernateUtil.getSession();
		
		FluxCalendar result = (FluxCalendar) HibernateUtil.get(FluxCalendar.class, new Long(1));

		assertTrue(result.getHolidaysList().size() != 0);
		
		session.close();
	}

    private List listResult()
    {
        String hql = "from FluxCalendar c where c.name = '" + derek + "'";
        return HibernateUtil.search(hql);
    }
}
