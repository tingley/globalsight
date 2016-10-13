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

import com.globalsight.persistence.hibernate.HibernateUtil;

public class ReservedTimeTest extends TestCase
{
	private String derek = "derek";

	public void testSave() throws Exception
	{
        int resultSize = listResult().size();

		ReservedTime reservedTime = new ReservedTime();

		reservedTime.setSubject(derek);
		reservedTime.setType("EVENT");
		reservedTime.setTaskId(new Long(1));
		reservedTime.setStartDate(new Date());
		reservedTime.setStartHour(1);
		reservedTime.setStartMinute(1);
		reservedTime.setEndDate(new Date());
		reservedTime.setEndHour(1);
		reservedTime.setEndMinute(1);
		reservedTime.setDurationExpression(derek);
		reservedTime.setRepeatExpression(derek);
		
		UserFluxCalendar calendar = (UserFluxCalendar) HibernateUtil.get(UserFluxCalendar.class, new Long(1));
		reservedTime.setUserFluxCalendar(calendar);

		HibernateUtil.save(reservedTime);

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
        String hql = "from ReservedTime t where t.subject = '" + derek + "'";
        return HibernateUtil.search(hql);
    }
}
