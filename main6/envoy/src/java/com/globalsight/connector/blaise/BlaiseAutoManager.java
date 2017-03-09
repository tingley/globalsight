package com.globalsight.connector.blaise;

import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.StringUtil;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Blaise automatic operation manager
 */
public class BlaiseAutoManager
{
    private static Logger logger = Logger.getLogger(BlaiseAutoManager.class);
    static ConcurrentHashMap<Long, BlaiseTimerTask> threads = new ConcurrentHashMap<>();
    static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    /**
     * Sets up the automatic Blaise server connectors
     */
    public static void init()
    {
        List<?> blaiseConnectors = BlaiseManager.getConnectors();
        if (blaiseConnectors != null && blaiseConnectors.size() > 0)
        {
            BlaiseConnector connector;
            int checkDuration;
            for (int i = 0, size = blaiseConnectors.size(); i < size; i++)
            {
                connector = (BlaiseConnector) blaiseConnectors.get(i);
                if (connector.isAutomatic() && StringUtil.isNotEmpty(connector.getPullDays()))
                {
                    checkDuration = connector.getCheckDuration();
                    logger.info("**** Start timer for Blaise automatic operation ****");
                    logger.info("Connector : " + connector.getId() + ", " + connector.getName());
                    logger.info("Schedule: " + connector.getPullDays() + " -- " + connector
                            .getPullHour());
                    logger.info("Check duration: " + connector.getCheckDuration());
                    // Gets the first delay time schedule to right clock
                    Calendar now = Calendar.getInstance();
                    int hour = now.get(Calendar.HOUR_OF_DAY);
                    int minute = 0;
                    String checkDurationEnabled = SystemConfiguration.getInstance()
                            .getStringParameter("blaise.check.duration.enabled");
                    if ("false".equalsIgnoreCase(checkDurationEnabled) || checkDuration == 0)
                    {
                        // User don't switch on check duration setting, non-debug mode
                        if (hour == connector.getPullHour())
                        {
                            // Check once immediately
                            BlaiseTimerTask timerTask = new BlaiseTimerTask(connector);
                            scheduledExecutorService.schedule(timerTask, 0, TimeUnit.MINUTES);
                        }
                        minute = 60 - now.get(Calendar.MINUTE);
                        checkDuration = 60;
                    }

                    BlaiseTimerTask timerTask = new BlaiseTimerTask(connector);
                    scheduledExecutorService
                            .scheduleAtFixedRate(timerTask, minute, checkDuration, TimeUnit.MINUTES);
                    threads.put(connector.getId(), timerTask);
                    logger.info(
                            "**** Start thread for Blaise automatic. Thread [" + timerTask.getId()
                                    + ", " + timerTask.getName() + "]. After " + minute
                                    + " mins, checkDuration is " + checkDuration + " mins");
                }
            }
        }
    }

    /**
     * Starts a timer task thread
     */
    public static void startThread(BlaiseConnector bc)
    {
        if (bc == null || !bc.isAutomatic())
            return;

        int checkDuration = bc.getCheckDuration();
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        String checkDurationEnabled = SystemConfiguration.getInstance()
                .getStringParameter("blaise.check.duration.enabled");
        if ("false".equalsIgnoreCase(checkDurationEnabled) || checkDuration == 0)
        {
            // User don't switch on check duration setting, non-debug mode
            if (hour == bc.getPullHour())
            {
                // Check once immediately
                BlaiseTimerTask timerTask = new BlaiseTimerTask(bc);
                scheduledExecutorService.schedule(timerTask, 0, TimeUnit.MINUTES);
            }
            minute = 60 - now.get(Calendar.MINUTE);
            checkDuration = 60;
        }
        BlaiseTimerTask timerTask = new BlaiseTimerTask(bc);
        scheduledExecutorService
                .scheduleAtFixedRate(timerTask, minute, checkDuration, TimeUnit.MINUTES);
        threads.put(bc.getId(), timerTask);
        logger.info(
                "**** Start thread for Blaise automatic. Thread [" + timerTask.getId()
                        + ", " + timerTask.getName() + "]. After " + minute
                        + " mins, checkDuration is " + checkDuration + " mins");
    }

    /**
     * Cancels a timer task thread
     */
    public static void cancelThread(long bcId)
    {
        BlaiseTimerTask thread = threads.get(Long.valueOf(bcId));
        if (thread != null)
        {
            thread.cancel();
            thread.interrupt();
            logger.info("**** Cancel thread for Blaise automatic. Thread [" + thread.getId() + ", "
                    + thread.getName() + "]");
            threads.remove(bcId);
        }
    }

    public static boolean isThreadAlive(long bcId)
    {
        BlaiseTimerTask thread = threads.get(bcId);
        return thread != null && thread.isAlive();
    }

    /**
     * Resets timer task thread
     */
    public static void resetThread(BlaiseConnector bc)
    {
        if (bc == null || !bc.isAutomatic())
            return;

        cancelThread(bc.getId());
        startThread(bc);
    }
}
