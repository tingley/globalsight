/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package com.globalsight.everest.webapp.pagehandler.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.StringUtil;

public class AutoCompleteActivityThread implements Runnable
{
    static private final Logger logger = Logger
            .getLogger(AutoCompleteActivityThread.class);

	JobHandler jobHandler = null;

	private static final String TASK_SQL = "SELECT tsk.TASK_ID "
			+ "FROM task_info tsk, activity act "
			+ "WHERE tsk.NAME = act.NAME "
			+ "AND tsk.state = '" + Task.STATE_ACCEPTED_STR + "' "
		    + "AND tsk.ACCEPTED_DATE IS NOT NULL "
			+ "AND tsk.USER_ID IS NOT NULL "
		    + "AND act.AUTO_COMPLETE_ACTIVITY = 'Y' ";

	public AutoCompleteActivityThread()
    {
        super();
    }

	public void start() throws Exception
    {
        while (true)
        {
        	if (jobHandler == null)
        	{
        		jobHandler = ServerProxy.getJobHandler();
        	}

        	// Determine task IDs that need auto complete.
        	List<Long> taskIds = new ArrayList<Long>();
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
        	try
        	{
            	conn = DbUtil.getConnection();
            	ps = conn.prepareStatement(TASK_SQL);
            	rs = ps.executeQuery();
            	while (rs.next())
            	{
            		taskIds.add(rs.getLong("TASK_ID"));
            	}
        	}
        	catch (Exception e)
        	{
        		logger.error(e);
        	}
        	finally
        	{
        		DbUtil.silentClose(rs);
        		DbUtil.silentClose(ps);
        		DbUtil.silentReturnConnection(conn);
        	}

        	if (taskIds.size() > 0) {
				logger.info("There are " + taskIds.size()
						+ " tasks which are configured to be auto-compleleted.");        		
        	}
            String acceptor = null;
            Task t = null;
            for (Long taskId : taskIds)
            {
            	try
            	{
        			t = (Task) HibernateUtil.get(TaskImpl.class, taskId);
					CompanyThreadLocal.getInstance().setIdValue(
							t.getCompanyId());
                    Activity act = jobHandler.getActivity(t.getTaskName());
                    if (act.getAutoCompleteActivity())
                    {
                        acceptor = t.getAcceptor();
                        if (StringUtil.isEmpty(acceptor))
                        	continue;

                        Job job = jobHandler.getJobById(t.getJobId());
                        String afterJobCreation = act.getAfterJobCreation();
                        String afterJobDispatch = act.getAfterJobDispatch();
                        String afterActivityStart = act.getAfterActivityStart();

                        String[] arr;
                        long d;
                        long h;
                        long m;
                        long minutes1;
                        long minutes2;
                        Date nowTime = new Timestamp(Calendar.getInstance()
                                .getTimeInMillis());
                        if (afterJobCreation != null
                                && afterJobCreation.length() > 0)
                        {
                            minutes1 = (nowTime.getTime() - job.getCreateDate()
                                    .getTime()) / (1000 * 60);
                            arr = afterJobCreation.split("-");
                        }
                        else if (afterJobDispatch != null
                                && afterJobDispatch.length() > 0)
                        {
                            minutes1 = (nowTime.getTime() - t.getWorkflow()
                                    .getDispatchedDate().getTime())
                                    / (1000 * 60);
                            arr = afterJobDispatch.split("-");
                        }
                        else
                        {
                            minutes1 = (nowTime.getTime() - t.getAcceptedDate()
                                    .getTime()) / (1000 * 60);
                            arr = afterActivityStart.split("-");
                        }
                        d = Long.parseLong(arr[0].trim());
                        h = Long.parseLong(arr[1].trim());
                        m = Long.parseLong(arr[2].trim());
                        minutes2 = d * 24 * 60 + h * 60 + m;

                        if (minutes1 >= minutes2)
                        {
                            ServerProxy.getTaskManager().completeTask(
                                    acceptor, t, null, null);
                        }
                    }
            	}
            	catch (Exception e)
            	{
            		logger.error("Fail to auto complete task:" + t.getId(), e);
            	}
            }

            HibernateUtil.closeSession();
            // Check interval time is fixed 5 minutes.
            Thread.sleep(5 * 60 * 1000);
        }
    }

    @Override
    public void run()
    {
        try
        {
            start();
        }
        catch (InterruptedException ie)
        {
			logger.error("The auto-complete activity thread is interrupted", ie);
        }
        catch (Exception e)
        {
            logger.error(e);
        }

    }

}
