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

package com.globalsight.everest.webapp.pagehandler.tm.management;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm3.core.TM3Exception;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.Tm3Migrator;
import com.globalsight.util.progress.ProgressReporter;

public class Tm3ConvertHelper extends Thread
{
    private static final Logger logger = Logger
            .getLogger(Tm3ConvertHelper.class.getName());

    private ProjectTM oldTm = null;
    private ProgressReporter proReport = null;
    private Tm3Migrator migrator = null;
    private boolean isCanceled = false;

    public Tm3ConvertHelper(long p_companyId, ProjectTM p_oldTm,
            ProgressReporter p_pr)
    {
        this.oldTm = p_oldTm;
        this.proReport = p_pr;

        this.migrator = new Tm3Migrator(p_companyId, p_oldTm);
    }

    public Tm3ConvertHelper(long p_companyId, ProjectTM p_oldTm)
    {
        this.oldTm = p_oldTm;

        this.migrator = new Tm3Migrator(p_companyId, p_oldTm);
    }

    public String getUniqueName()
    {
        if (migrator == null)
        {
            return "";
        }
        return migrator.getUniqueTmName(oldTm.getName());
    }

    public void convert()
    {
        MultiCompanySupportedThread thread = new MultiCompanySupportedThread(
                this);
        thread.start();
    }

    public ProjectTM getCurrentTM()
    {
        return oldTm;
    }

    public ProjectTM getCurrentTM3TM()
    {
        ProjectTM tm3tm = null;
        if (oldTm != null && oldTm.getConvertedTM3Id() > 0)
        {
            try
            {
                tm3tm = ServerProxy.getProjectHandler().getProjectTMById(
                        oldTm.getConvertedTM3Id(), false);
            }
            catch (Exception e)
            {
                logger.error("Error in getCurrentTM3TM().", e);
            }
        }
        return tm3tm;
    }

    @Override
    public void run()
    {
        if (oldTm != null)
        {
            try
            {
                if (migrator != null)
                {
                    migrator.migrate();
                }
            }
            catch (Exception e)
            {
                if (!isCanceled)
                    logger.error(
                            "Error found when migrating TM to TM3. "
                                    + e.getMessage(), e);
            }
        }
    }

    public void cancel()
    {
        try
        {
            isCanceled = true;
            // proReport.setMessageKey("lb_tm_convert_cancel",
            // "User cancel the conversion");

            this.migrator.cancelConvert();
            TM3Tm<GSTuvData> tm3tm = migrator.getCurrrentTm3();
            // TM3Manager manager = DefaultManager.create();
            // manager.removeTm(session, tm3tm);
            logger.info("User interrupt the conversion. TM3 ID is "
                    + tm3tm.getId().toString());
        }
        catch (TM3Exception e)
        {
            // proReport.setMessageKey("lb_tm_convert_cancel",
            // "User cancel the conversion");
            logger.error("Error found in cancel TM3 Conversion.", e);
        }
    }
}
