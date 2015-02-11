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

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.ling.tm2.TmUtil;
import com.globalsight.ling.tm3.core.DefaultManager;
import com.globalsight.ling.tm3.core.TM3Exception;
import com.globalsight.ling.tm3.core.TM3Manager;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.ling.tm3.integration.Tm3Migrator;
import com.globalsight.util.progress.ProgressReporter;
import com.globalsight.util.progress.TmProcessStatus;

public class Tm3ConvertHelper extends Thread
{
    private static final Logger logger = Logger
            .getLogger(Tm3ConvertHelper.class.getName());

    private TmProcessStatus status = null;
    private Session session = null;
    private Transaction transaction = null;
    private long companyId = 0;
    private ProjectTM oldTm = null;
    private ProjectTM newTm = null;
    private ProgressReporter proReport = null;
    private Tm3Migrator migrator = null;
    private boolean isCanceled = false;
    
    public Tm3ConvertHelper (Session p_session, long p_companyId, ProjectTM p_oldTm, ProgressReporter p_pr) {
        this.session = p_session;
        this.transaction = session.beginTransaction();
        this.companyId = p_companyId;
        this.oldTm = p_oldTm;
        this.proReport = p_pr;
        
        this.migrator = new Tm3Migrator(p_session, p_companyId, p_oldTm);
    }
    
    public String getUniqueName() {
        if (migrator == null) {
            return "";
        }
        return migrator.getUniqueTmName(session, oldTm.getName());
    }

    public void convert()
    {
        MultiCompanySupportedThread thread = new MultiCompanySupportedThread(
                this);
        thread.start();
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
                    newTm = migrator.migrate(proReport,
                            new TransactionWrapper());
                    transaction.commit();
                }
            }
            catch (Exception e)
            {
                if (transaction != null && transaction.isActive())
                    transaction.rollback();

                if (!isCanceled)
                    logger.error("Error found when migrating TM to TM3. " + e.getMessage());
            }
            finally
            {
                TmUtil.closeStableSession(session);
            }
        }
    }
    
    public void cancel() {
        try {
            isCanceled = true;
            proReport.setMessageKey("lb_tm_convert_cancel", "User cancel the conversion");
            
            this.migrator.cancelConvert();
            TM3Tm<GSTuvData> tm3tm = migrator.getCurrrentTm3();
            TM3Manager manager = DefaultManager.create();
            manager.removeTm(session, tm3tm);
            logger.info("User interrupt the conversion. TM3 ID is " + tm3tm.getId().toString());
        } catch (TM3Exception e) {
            proReport.setMessageKey("lb_tm_convert_cancel", "User cancel the conversion");
        }
    }

    class TransactionWrapper implements Tm3Migrator.TransactionControl
    {
        @Override
        public void commitAndRestartTransaction()
        {
            Tm3ConvertHelper.this.commitAndRestartTransaction();
        }
    }

    public Transaction commitAndRestartTransaction()
    {
        try
        {
            transaction.commit();
            transaction = session.beginTransaction();
            return transaction;
        }
        catch (Exception e)
        {
            transaction.rollback();
            
            proReport.setMessageKey("lb_tm_convert_cancel", "User cancel the conversion");
            logger.error("User interrupt the conversion.");
            return null;
        }
    }
}
