/**
 * Copyright 2016 Welocalize, Inc.
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
package com.globalsight.everest.tm;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.page.ExtractedSourceFile;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.TuvEventObserver;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

public class PopulatingTmThread implements Runnable
{
    static private final Logger logger = Logger.getLogger(PopulatingTmThread.class);

    private long targetPageId;

    public PopulatingTmThread(long p_targetPageId)
    {
        super();
        this.targetPageId = p_targetPageId;
    }

    @Override
    public void run()
    {
        try
        {
            populateTm();
        }
        catch (Exception e)
        {
            logger.error(e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    private void populateTm() throws Exception
    {
        TargetPage targetPage = null;
        try
        {
            targetPage = ServerProxy.getPageManager().getTargetPage(targetPageId);
            updateExportedSubState(TargetPage.EXPORTED_TM_UPDATING, targetPageId);

            // Must populate TM first
            logger.debug("Populating TM...");
            populateTm(targetPage.getSourcePage(), targetPage.getGlobalSightLocale());

            // Then update target TUVs to 'COMPLETE' state.
            if (targetPage.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
            {
                getTuvEventObserver().notifyPageExportedEvent(targetPage);
            }
        }
        catch (Exception e)
        {
            logger.error(e);
            throw e;
        }
        finally
        {
            // Update "exported_sub_state" to "EXPORTED_TM_UPDATING_DONE"
            // whatever populating TM succeeds or fails.
            updateExportedSubState(TargetPage.EXPORTED_TM_UPDATING_DONE, targetPageId);
        }
    }

    private void populateTm(SourcePage p_sourcePage, GlobalSightLocale p_targetLocale)
            throws PageException
    {
        try
        {
            L10nProfile l10nProfile = p_sourcePage.getRequest().getL10nProfile();
            LeveragingLocales leveragingLocales = l10nProfile.getLeveragingLocales();
            TranslationMemoryProfile tmProfile = l10nProfile.getTranslationMemoryProfile();
            LeverageOptions leverageOptions = new LeverageOptions(tmProfile, leveragingLocales);

            LingServerProxy.getTmCoreManager().populatePageByLocale(p_sourcePage, leverageOptions,
                    p_targetLocale, p_sourcePage.getJobId());
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private void updateExportedSubState(int exportedSubState, Long targetPageId)
    {
        String sql = "UPDATE target_page SET EXPORTED_SUB_STATE = ? WHERE ID = ?";

        List<List> targetPages = new ArrayList<>();
        List params = new ArrayList<>();
        params.add(exportedSubState);
        params.add(targetPageId);
        targetPages.add(params);

        DbUtil.batchUpdate(sql, targetPages);
    }

    private TuvEventObserver getTuvEventObserver() throws PageException
    {
        try
        {
            return ServerProxy.getTuvEventObserver();
        }
        catch (GeneralException ge)
        {
            throw new PageException(PageException.MSG_FAILED_TO_LOCATE_TUV_EVENT_OBSERVER, null, ge);
        }
    }
}
