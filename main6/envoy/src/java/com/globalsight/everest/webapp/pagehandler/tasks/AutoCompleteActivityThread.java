package com.globalsight.everest.webapp.pagehandler.tasks;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class AutoCompleteActivityThread implements Runnable
{
    static private final Logger logger = Logger
            .getLogger(AutoCompleteActivityThread.class);

    public AutoCompleteActivityThread()
    {
        super();
    }

    @SuppressWarnings({ "unchecked" })
    public void start() throws Exception
    {
        while (true)
        {
			String taskhql = "from TaskImpl t where t.stateStr='"
					+ Task.STATE_ACCEPTED_STR + "'";
            ArrayList<Task> listTask = (ArrayList<Task>) HibernateUtil
                    .search(taskhql);
            String acceptor = null;
            if (listTask != null && listTask.size() > 0)
            {
            	JobHandler jobHandler = ServerProxy.getJobHandler();
                for (Task t : listTask)
                {
                	try
                	{
                        Activity act = jobHandler.getActivity(t.getTaskName());
                        if (act.getAutoCompleteActivity())
                        {
							CompanyThreadLocal.getInstance().setIdValue(
									t.getCompanyId());
                            acceptor = t.getAcceptor();
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

                            if (minutes1 > minutes2)
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
            }

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
