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

package com.globalsight.terminology.scheduler;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.scheduling.EventSchedulerHelper;
import com.globalsight.scheduling.FluxEventMap;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.scheduling.SchedulingInformation;
import com.globalsight.terminology.util.SqlUtil;


/**
 * <p>The implementation of the RMI interface for the Terminology
 * Scheduler, which is responsible for setting up cron jobs for
 * termbase indexing.</p>
 */
public class TermbaseScheduler
	extends RemoteServer
	implements ITermbaseScheduler
{
    static public final String TERMBASE_ID = "id";

	static private final String SQL_DELETE = 
		"delete from TB_SCHEDULED_JOBS where TBID=? and TYPE=?";
	static private final String SQL_INSERT = 
		"insert into TB_SCHEDULED_JOBS " + 
		"(TBID,TYPE,MINUTES,HOURS,DAYS_OF_MONTH,MONTHS,DAYS_OF_WEEK," + 
		"DAY_OF_YEAR,WEEK_OF_MONTH,WEEK_OF_YEAR,YEAR)" +
		"values (?,?,?,?,?,?,?,?,?,?,?)";

    //
    // Constructor
    //

    public TermbaseScheduler()
        throws RemoteException
    {
        super(RemoteServer.getServiceName(ITermbaseScheduler.class));
    }

    //
    // RemoteServer method overwrites
    //

    /**
     * <p>Binds the remote server to the ServerRegistry.</p>
     *
     * @throws SystemStartupException when a NamingException or other
     * Exception occurs.
     */
    public void init()
        throws SystemStartupException
    {
        super.init();
    }

    /**
     * <p>Unbinds the remote server from the ServerRegistry.</p>
     *
     * @throws SystemShutdownException when a NamingException or other
     * Exception occurs.
     */
    public void destroy()
        throws SystemShutdownException
    {
        super.destroy();
    }

	//
	// ITermbaseScheduler interface Methods
	//

    /**
     * Schedules the event for re-indexing a termbase.
     */
    public void scheduleEvent(TermbaseReindexExpression p_expr)
        throws Exception, RemoteException
    {
		// First persist the event in the DB.
		createEvent(p_expr);

        Class listener = TermbaseDispatcher.class;

        Integer eventType = (Integer)SchedulerConstants.s_eventTypes.get(
            SchedulerConstants.REINDEX_TERMBASE_TYPE);

        Integer objType = (Integer)SchedulerConstants.s_objectTypes.get(
            TermbaseReindexExpression.class);

        // You need to put all the things that you need when the event
        // is fired.  Let's say you need to get termbase by id, so
        // you'll store the id here.  Note that anything stored in
        // this map should be serializable.  
        // Do not store big classes in the
        // HashMap.

        // (This HashMap is turned into an EventInfo object in
        // EventSchedulerLocal.scheduleEvent().

        HashMap eventInfo = new HashMap();
        eventInfo.put(TERMBASE_ID, p_expr.getObjectId());

        SchedulingInformation info = new SchedulingInformation();
        info.setStartDate(null);
        info.setRecurranceExpression(p_expr.getCronExpression());
        info.setListener(listener);
        info.setEventInfo(eventInfo);
        info.setEventTypeName("reindexingTermbase");
        info.setEventType(eventType.intValue());
        info.setRepeatCount(-1);
        info.setObjectId(p_expr.getObjectId().longValue());
        info.setObjectType(objType.intValue());

        // The following method creates a scheduler based on the time
        // expression and other info that are provided.  It'll also create a
        // FluxEventMap object based on the domain object id (i.e. termbase id),
        // domain object type (i.e Termbase, or Activity), and event type (i.e.
        // re-indexing termbase).  That object is used if you plan to query some
        // jobs in the DB.
        ServerProxy.getEventScheduler().scheduleEvent(info);
    }

    /**
     * Unschedules the event for reindexing the given termbase and
     * removes it from the db.
     */
    public void unscheduleEvent(Long p_termbaseId)
        throws Exception, RemoteException
    {
        Integer eventType = (Integer)SchedulerConstants.s_eventTypes.get(
            SchedulerConstants.REINDEX_TERMBASE_TYPE);

        Integer objType = (Integer)SchedulerConstants.s_objectTypes.get(
            TermbaseReindexExpression.class);

        FluxEventMap map = EventSchedulerHelper.findFluxEventMap(
            eventType, objType, p_termbaseId);

        if (map != null)
        {
            ServerProxy.getEventScheduler().unschedule(map);

            deleteEvent(p_termbaseId);
        }
    }

    /**
     * Retrieves the TermbaseReindexExpression for the given termbase.
     */
    public TermbaseReindexExpression getEvent(Long p_termbaseId)
        throws Exception, RemoteException
    {
		TermbaseReindexExpression result = null;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

		try
        {
            conn = SqlUtil.hireConnection();
            stmt = conn.createStatement();

            rset = stmt.executeQuery(
				"select * from TB_SCHEDULED_JOBS " +
				"where TBID=" + p_termbaseId + 
				"  and TYPE='" + TermbaseReindexExpression.TYPE + "'");

			if (rset.next())
			{
				result = new TermbaseReindexExpression(p_termbaseId,
					rset.getString("MINUTES"),
					rset.getString("HOURS"),
					rset.getString("DAYS_OF_MONTH"),
					rset.getString("MONTHS"),
					rset.getString("DAYS_OF_WEEK"),
					rset.getString("DAY_OF_YEAR"),
					rset.getString("WEEK_OF_MONTH"),
					rset.getString("WEEK_OF_YEAR"),
					rset.getString("YEAR"));
			}
		}
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }

		return result;
    }

	//
	// Private Methods
	//

    private void createEvent(TermbaseReindexExpression p_expr)
        throws Exception
    {
        Connection conn = null;
        PreparedStatement stmt = null;

		try
        {
            conn = SqlUtil.hireConnection();
			conn.setAutoCommit(false);

            stmt = conn.prepareStatement(SQL_DELETE);
			stmt.setLong(1, p_expr.getObjectId().longValue());
			stmt.setString(2, p_expr.getType());
			stmt.executeUpdate();

            stmt = conn.prepareStatement(SQL_INSERT);
			stmt.setLong(1, p_expr.getObjectId().longValue());
			stmt.setString(2, p_expr.getType());
			stmt.setString(3, p_expr.getMinutes());
			stmt.setString(4, p_expr.getHours());
			stmt.setString(5, p_expr.getDaysOfMonth());
			stmt.setString(6, p_expr.getMonths());
			stmt.setString(7, p_expr.getDaysOfWeek());
			stmt.setString(8, p_expr.getDayOfYear());
			stmt.setString(9, p_expr.getWeekOfMonth());
			stmt.setString(10, p_expr.getWeekOfYear());
			stmt.setString(11, p_expr.getYear());
			stmt.executeUpdate();

			conn.commit();
		}
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw e;
        }
        finally
        {
            try
            {
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }
    }

    private void deleteEvent(Long p_termbaseId)
        throws Exception
    {
        Connection conn = null;
        PreparedStatement stmt = null;

		try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.prepareStatement(SQL_DELETE);
			stmt.setLong(1, p_termbaseId.longValue());
			stmt.setString(2, TermbaseReindexExpression.TYPE);
			stmt.executeUpdate();

			conn.commit();
		}
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw e;
        }
        finally
        {
            try
            {
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }
    }
}
