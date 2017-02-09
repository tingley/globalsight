package com.globalsight.connector.blaise.util;

import com.globalsight.cxe.entity.blaise.BlaiseConnector;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.JsonUtil;
import com.globalsight.util.StringUtil;
import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2017/2/9.
 */
public class BlaiseAutoHelper
{
    private static Logger logger = Logger.getLogger(BlaiseAutoHelper.class);
    private static boolean running = false;
    private static BlaiseAutoHelper autoHelper = new BlaiseAutoHelper();

    private BlaiseAutoHelper()
    {
    }

    public static BlaiseAutoHelper getInstance()
    {
        if (autoHelper == null)
            autoHelper = new BlaiseAutoHelper();
        return autoHelper;
    }

    public void runAutomatic(BlaiseConnector bc)
    {
        if (running || bc == null)
            return;

        BlaiseHelper helper = new BlaiseHelper(bc);
        running = true;
        helper.groupInboxEntries();
        running = false;
    }

    public String getJobAttributes(String fpIdStr)
    {
        if (StringUtil.isEmpty(fpIdStr))
            return "";
        long fpId = Long.parseLong(fpIdStr);
        try
        {
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager().getFileProfileById(fpId, false);
            if (fp == null)
                return "";
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(fp.getL10nProfileId());
            Project project = lp.getProject();
            AttributeSet attributeSet = project.getAttributeSet();
            String data =  JsonUtil.toJson(attributeSet);
            logger.info("json data == " + data);
            return data;
        } catch (Exception e) {
            logger.error("Error found when invoking getJobAttributes().", e);
            return "";
        }
    }
}
