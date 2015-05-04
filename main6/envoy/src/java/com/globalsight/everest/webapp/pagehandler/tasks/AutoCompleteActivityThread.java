package com.globalsight.everest.webapp.pagehandler.tasks;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandlerLocal;
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

    @SuppressWarnings(
    { "unused", "unchecked" })
    public void start() throws Exception
    {
        try
        {
            while (true)
            {
                String taskhql = "from TaskImpl t where t.stateStr='accepted' ";
                ArrayList<Task> listTask = (ArrayList<Task>) HibernateUtil
                        .search(taskhql);
                StringBuffer taskIds = null;
                String userId = null;
                if (listTask != null && listTask.size() > 0)
                {
                    for (Task t : listTask)
                    {
                        Date nowTime = new Timestamp(Calendar.getInstance()
                                .getTimeInMillis());
                        CompanyThreadLocal.getInstance().setIdValue(t.getCompanyId());
                        Activity act = ServerProxy.getJobHandler().getActivity(
                                t.getTaskName());
                        if (act.getAutoCompleteActivity())
                        {
                            userId = t.getAcceptor();
                            JobHandlerLocal jobHL = new JobHandlerLocal();
                            Job job = jobHL.getJobById(t.getJobId());

                            String afterJobCreation = act.getAfterJobCreation();
                            String afterJobDispatch = act.getAfterJobDispatch();
                            String afterActivityStart = act
                                    .getAfterActivityStart();

                            String[] arr;
                            long d;
                            long h;
                            long m;
                            long temp;
                            long temp2;
                            if (afterJobCreation != null
                                    && afterJobCreation.length() > 0)
                            {
                                temp = (nowTime.getTime() - job.getCreateDate()
                                        .getTime()) / (1000 * 60);
                                arr = afterJobCreation.split("-");
                            }
                            else if (afterJobDispatch != null
                                    && afterJobDispatch.length() > 0)
                            {
                                temp = (nowTime.getTime() - t.getWorkflow()
                                        .getDispatchedDate().getTime())
                                        / (1000 * 60);
                                arr = afterJobDispatch.split("-");
                            }
                            else
                            {
                                temp = (nowTime.getTime() - t.getAcceptedDate()
                                        .getTime()) / (1000 * 60);
                                arr = afterActivityStart.split("-");
                            }
                            d = Long.parseLong(arr[0].trim());
                            h = Long.parseLong(arr[1].trim());
                            m = Long.parseLong(arr[2].trim());
                            temp2 = d * 24 * 60 + h * 60 + m;

                            if (temp > temp2)
                            {
                                ServerProxy.getTaskManager().completeTask(
                                        userId, t, null, null);
                            }
                        }
                    }
                }
                Thread.sleep(5 * 60 * 1000);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    @Override
    public void run()
    {
        try
        {
            start();
        }
        catch (Exception e)
        {
            logger.error(e);
        }

    }

}
