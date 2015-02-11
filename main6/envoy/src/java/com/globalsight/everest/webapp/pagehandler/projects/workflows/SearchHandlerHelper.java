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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.util.Collection;
import java.util.List;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.searchreplace.ActivitySearchReportQueryResult;
import com.globalsight.everest.tm.searchreplace.JobSearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.JobSearchReportQueryResult;

/**
 * A helper class for all job search/replace related page handlers.
 */
public class SearchHandlerHelper
{
    /**
     * Search locales for a string.
     */
    static JobSearchReportQueryResult searchJobs(boolean isCaseSensitive,
        String query, Collection targetLocales, Collection p_jobIds)
        throws EnvoyServletException
    {
        try
        {
            JobSearchReplaceManager jobSearchMgr =
                ServerProxy.getTmManager().getJobSearchReplaceManager();
            return jobSearchMgr.searchForJobSegments(
                isCaseSensitive, query, targetLocales,p_jobIds);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Replace (but don't persist) a string with another string.
     */
    public static List replaceForPreview(String oldString, String newString,
        List jobInfos, boolean isCaseSensitive)
        throws EnvoyServletException
    {
        try
        {
            JobSearchReplaceManager jobSearchMgr =
                ServerProxy.getTmManager().getJobSearchReplaceManager();
            return (List)jobSearchMgr.replaceForPreview(
                oldString, newString, jobInfos, isCaseSensitive);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Persist the replaces segments.
     */
    public static void replace(List tuvInfos, String companyId)
        throws EnvoyServletException
    {
        try
        {
            JobSearchReplaceManager jobSearchMgr =
                ServerProxy.getTmManager().getJobSearchReplaceManager();
            jobSearchMgr.replace(tuvInfos, companyId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Search locales for a string.
     *
     * This method is called from ../../tasks/SearchTasksResultsHandler.
     */
    public static ActivitySearchReportQueryResult searchTasks(
        boolean isCaseSensitive, String query, Collection targetLocales,
        Collection jobIds)
        throws EnvoyServletException
    {
        try
        {
            JobSearchReplaceManager jobSearchMgr =
                ServerProxy.getTmManager().getJobSearchReplaceManager();
            return jobSearchMgr.searchForActivitySegments(
                isCaseSensitive, query, targetLocales, jobIds);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }
}
