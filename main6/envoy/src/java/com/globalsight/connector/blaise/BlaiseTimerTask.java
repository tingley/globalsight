package com.globalsight.connector.blaise;

import com.globalsight.connector.blaise.util.BlaiseAutoHelper;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.util.StringUtil;
import org.apache.log4j.Logger;

import java.util.Calendar;

/**
 * Timer task class
 */
public class BlaiseTimerTask extends Thread
{
    private Logger logger = Logger.getLogger(BlaiseTimerTask.class);
    private BlaiseConnector connector = null;
    private boolean isCancel = false;

    public BlaiseTimerTask(BlaiseConnector connector)
    {
        this.connector = connector;
    }

    public void cancel()
    {
        isCancel = true;
        logger.info("Thread [" + this.getName() + "] is cancelled.");
    }

    @Override public void run()
    {
        try
        {
            if (connector == null || isCancel || !connector.isAutomatic())
                return;

            if (StringUtil.isNotEmpty(connector.getPullDays()))
            {
                int checkDuration = connector.getCheckDuration();
                long checkTime = checkDuration * 60000;

                while (true)
                {
                    if (isCancel)
                        break;

                    //is an automatic connector
                    String pullDays = connector.getPullDays();
                    if (!pullDays.endsWith(","))
                        pullDays += ",";
                    Calendar calendar = Calendar.getInstance();
                    String currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) + ",";
                    logger.info("Thread [" + getName() + "] is running check..." + pullDays + ", "
                            + currentDayOfWeek);
                    if (pullDays.indexOf(currentDayOfWeek) > -1)
                    {
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        logger.info(
                                "Current hour == " + currentHour + ", defined hour == " + connector
                                        .getPullHour());
                        if (connector.getPullHour() == currentHour)
                        {
                            //match the time condition
                            BlaiseAutoHelper autoHelper = new BlaiseAutoHelper();
                            autoHelper.setCompanyId(String.valueOf(connector.getCompanyId()));
                            autoHelper.runAutomatic(connector);
                        }
                    }

                    sleep(checkTime);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error found when runing BlaiseTimerTask. ", e);
        }
    }
}
