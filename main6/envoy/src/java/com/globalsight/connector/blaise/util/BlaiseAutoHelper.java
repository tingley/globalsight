package com.globalsight.connector.blaise.util;

import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2017/2/9.
 */
public class BlaiseAutoHelper
{
    private static Logger logger = Logger.getLogger(BlaiseAutoHelper.class);
    private static boolean running = false;

    public static void runAutomatic(BlaiseConnector bc)
    {
        if (running || bc == null)
            return;

        BlaiseHelper helper = new BlaiseHelper(bc);

    }
}
