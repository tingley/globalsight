package com.globalsight.connector.blaise;

import com.globalsight.connector.blaise.util.BlaiseAutoHelper;
import com.globalsight.connector.blaise.util.BlaiseManager;
import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.util.StringUtil;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.List;

/**
 * Timer task class
 */
public class BlaiseTimerTask implements Runnable
{
    private Logger logger = Logger.getLogger(BlaiseTimerTask.class);

    public BlaiseTimerTask()
    {
        super();
    }

    public void start() throws Exception
    {
        try
        {
            logger.info("************ In BlaiseTimerTask *****************");
            List blaiseConnectors = BlaiseManager.getConnectors();
            if (blaiseConnectors == null || blaiseConnectors.size() == 0)
                return;

            BlaiseConnector connector;
            for (int i = 0, size = blaiseConnectors.size(); i < size; i++)
            {
                connector = (BlaiseConnector) blaiseConnectors.get(i);
                if (connector.isAutomatic() && StringUtil.isNotEmpty(connector.getPullDays()))
                {
                    //is an automatic connector
                    String pullDays = connector.getPullDays();
                    if (!pullDays.endsWith(","))
                        pullDays += ",";
                    Calendar calendar = Calendar.getInstance();
                    String currentDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - 1) + ",";
                    logger.info("**** pullDays == " + pullDays + ", currentDaysOfWeek == "
                            + currentDayOfWeek + ", hour == " + connector.getPullHour() + ", "
                            + calendar.get(Calendar.HOUR_OF_DAY));
                    if (pullDays.indexOf(currentDayOfWeek) > -1)
                    {
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        if (connector.getPullHour() == currentHour)
                        {
                            //match the time condition
                            BlaiseAutoHelper.getInstance().setCompanyId(String.valueOf(connector.getCompanyId()));
                            BlaiseAutoHelper.getInstance().runAutomatic(connector);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error found when runing BlaiseTimerTask. ", e);
        }

    }

    @Override public void run()
    {
        try
        {
            start();
        }
        catch (Exception e)
        {
            logger.error("Error found when runing BlaiseTimerTask. ", e);
        }

    }
}
