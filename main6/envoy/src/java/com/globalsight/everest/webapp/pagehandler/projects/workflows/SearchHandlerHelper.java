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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.TmManagerException;
import com.globalsight.everest.tm.searchreplace.ActivitySearchReportQueryResult;
import com.globalsight.everest.tm.searchreplace.JobInfo;
import com.globalsight.everest.tm.searchreplace.JobSearchReplaceManager;
import com.globalsight.everest.tm.searchreplace.JobSearchReportQueryResult;
import com.globalsight.everest.tm.searchreplace.TuvInfo;
import com.globalsight.util.GeneralException;

/**
 * A helper class for all job search/replace related page handlers.
 */
public class SearchHandlerHelper
{
    private static JobSearchReplaceManager jobSearchMgr = null;

    /**
     * Search locales for a string.
     */
    static JobSearchReportQueryResult searchJobs(boolean isCaseSensitive,
            String query, Collection<String> targetLocales,
            Collection<String> p_jobIds) throws EnvoyServletException
    {
        try
        {
            return getJobSearchReplaceManager().searchForJobSegments(
                    isCaseSensitive, query, targetLocales, p_jobIds);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Replace (but don't persist) a string with another string.
     */
    public static List<JobInfo> replaceForPreview(String oldString,
            String newString, List<JobInfo> jobInfos, boolean isCaseSensitive)
            throws EnvoyServletException
    {
        try
        {
            return (List<JobInfo>) getJobSearchReplaceManager()
                    .replaceForPreview(oldString, newString, jobInfos,
                            isCaseSensitive);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Persist the replaces segments.
     */
    public static void replace(List<TuvInfo> tuvInfos)
            throws EnvoyServletException
    {
        try
        {
            getJobSearchReplaceManager().replace(tuvInfos);
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
            boolean isCaseSensitive, String query,
            Collection<String> targetLocales, Collection<String> jobIds)
            throws EnvoyServletException
    {
        try
        {
            return getJobSearchReplaceManager().searchForActivitySegments(
                    isCaseSensitive, query, targetLocales, jobIds);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private static JobSearchReplaceManager getJobSearchReplaceManager()
            throws TmManagerException, RemoteException, GeneralException
    {
        if (jobSearchMgr == null)
            jobSearchMgr = ServerProxy.getTmManager()
                    .getJobSearchReplaceManager();
        
        return jobSearchMgr;
    }
}
