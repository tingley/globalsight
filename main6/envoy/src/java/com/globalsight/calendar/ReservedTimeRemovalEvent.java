/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.calendar;

// GlobalSight
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.persistence.calendar.ReservedTimeDescriptorModifier;
import com.globalsight.persistence.calendar.ReservedTimesQueryResultHandler;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.scheduling.EventHandler;
import com.globalsight.scheduling.EventHandlerException;
import com.globalsight.scheduling.EventInfo;
import com.globalsight.scheduling.KeyFlowContext;
import com.globalsight.scheduling.SchedulerConstants;

/**
 * ReservedTimeRemovalEvent is responsible for removing reserved times that are
 * older than a given period of time (as specified in the envoy.properties
 * file).
 */

public class ReservedTimeRemovalEvent extends EventHandler

{
    // for logging purposes
    private static final Logger s_category = Logger
            .getLogger(ReservedTimeRemovalEvent.class.getName());

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    public ReservedTimeRemovalEvent()
    {
        super();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Implementation of the Abstract Method
    // ////////////////////////////////////////////////////////////////////
    /**
     * This method is called when a scheduled event is fired. Subclasses must
     * implement this method in order to obtain the desired behavior at fire
     * time.
     * 
     * @param p_fireDate
     *            the date/time when the event fired.
     * @param p_event
     *            the event itself.
     * 
     * @throws EventHandlerException
     *             if any error occurs.
     */
    public void eventFired(KeyFlowContext p_flowContext)
            throws EventHandlerException
    {
        try
        {
            EventInfo myKey = (EventInfo) p_flowContext.getKey();

            performRemovalOfReservedTimes(myKey);
        }
        catch (Exception e)
        {
            s_category.error("Failed to perform the calendaring clean-up.", e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Implementation of the Abstract Method
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Private Methods
    // ////////////////////////////////////////////////////////////////////

    /*
     * Remove the reserved times that are older than the given number of days
     * which is stated in the envoy.properties file.
     */
    private void performRemovalOfReservedTimes(EventInfo p_eventInfo)
            throws Exception
    {
        HashMap map = p_eventInfo.getMap();
        Integer integer = (Integer) map.get(SchedulerConstants.NUM_OF_DAYS);
        Timestamp t = new Timestamp();
        t.add(Timestamp.DAY, -integer.intValue());

        Vector args = new Vector(1);
        args.add(t.getDate());

        // the result is a collection (converted to array) with one item
        // as a HashMap
        String sql = ReservedTimeDescriptorModifier.RESERVED_TIMES_BEFORE_GIVEN_END_DATE_SQL;
        Map params = new HashMap();
        params.put("ET", t.getDate());
        List times = HibernateUtil.searchWithSql(sql, params, ReservedTime.class);
        
        Object[] rts = ReservedTimesQueryResultHandler.handleResult(times).toArray();;

        if (rts.length == 0)
        {
            return;
        }

        // The HashMap keys are user calendars and values are a list of
        // their reserved time that should be removed.
        HashMap result = (HashMap) rts[0];
        Object[] keys = result.keySet().toArray();
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            for (int i = 0; i < keys.length; i++)
            {
                UserFluxCalendar cal = (UserFluxCalendar) keys[i];
                ArrayList reservedTimes = (ArrayList) result.get(cal);
                int size = reservedTimes.size();
                for (int h = 0; h < size; h++)
                {
                    ReservedTime rt = (ReservedTime) reservedTimes.get(h);

                    ReservedTime cloneRt = (ReservedTime) session.get(
                            ReservedTime.class, rt.getIdAsLong());

                    UserFluxCalendar clone = (UserFluxCalendar) session.get(
                            UserFluxCalendar.class, cal.getIdAsLong());

                    clone.getCollectionByType(cloneRt.getType())
                            .remove(cloneRt);
                    session.update(clone);
                }
                transaction.commit();
            }
        }
        catch (Exception e2)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            throw e2;
        }
        finally
        {
            if (session != null)
            {
                //session.close();
            }
        }
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Local Private Methods
    // ////////////////////////////////////////////////////////////////////
}
