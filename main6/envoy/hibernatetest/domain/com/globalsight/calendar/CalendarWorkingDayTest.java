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

public class CalendarWorkingDayTest extends TestCase
{
    private int day = 8;

    public void testSave() throws Exception
    {
        int resultSize = listResult().size();

        FluxCalendar fluxCalendar = (FluxCalendar) HibernateUtil.get(
                FluxCalendar.class, 1);

        CalendarWorkingDay workingDay = new CalendarWorkingDay(day);
        workingDay.setBaseFluxCalendar(fluxCalendar);

        HibernateUtil.save(workingDay);

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
        String hql = "from CalendarWorkingDay c where c.day = '" + day + "'";
        return HibernateUtil.search(hql);
    }
}
