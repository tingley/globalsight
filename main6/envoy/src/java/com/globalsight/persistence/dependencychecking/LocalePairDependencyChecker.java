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
package com.globalsight.persistence.dependencychecking;

// globalsight
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.job.JobDescriptorModifier;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Checks for objects that may have dependencies on a particular LocalePair. The
 * class dependencies are hard-coded into this class.
 */
public class LocalePairDependencyChecker extends DependencyChecker
{
    private static final Logger c_logger = Logger
            .getLogger(LocalePairDependencyChecker.class);

    /**
     * Returns the dependencies in a Vector of Objects. The vector contains all
     * objects that are dependent on the LocalePair object being passed in.
     * 
     * @param p_object
     *            The object to test if there are dependencies on.
     * @return A vector of all objects that are dependent on p_object.
     * @exception A
     *                general exception that wraps the specific exception of why
     *                retrieving the dependencies failed.
     */
    protected Vector findDependencies(PersistentObject p_object)
            throws DependencyCheckException
    {
        // check this is a LocalePair
        if (p_object.getClass() != LocalePair.class)
        {
            String args[] =
            { this.getClass().getName(), p_object.getClass().getName() };
            throw new DependencyCheckException(
                    DependencyCheckException.MSG_INVALID_OBJECT, args, null);
        }

        LocalePair lp = (LocalePair) p_object;

        Vector deps = l10nProfileDependencies(lp);
        deps.addAll(workflowTemplateDependencies(lp));
        deps.addAll(activeJobDependencies(lp));

        return deps;
    }

    /**
     * Return the L10nProfiles that are dependent on this LocalePair.
     */
    private Vector l10nProfileDependencies(LocalePair p_lp)
            throws DependencyCheckException
    {
        Vector l10nProfiles = null;

        try
        {
            String hql = "select distinct b from BasicL10nProfile b inner join b.workflowTemplates w "
                    + "where b.isActive = 'Y' and b.sourceLocale.id = :sId "
                    + "and b.companyId = :cId and w.targetLocale.id = :tID";
            Map map = new HashMap();
            map.put("sId", p_lp.getSource().getIdAsLong());
            map.put("tID", p_lp.getTarget().getIdAsLong());
            map.put("cId", p_lp.getCompanyId());
            l10nProfiles = new Vector(HibernateUtil.search(hql, map));
        }
        catch (Exception e)
        {
            StringBuffer message = new StringBuffer();

            String src = p_lp.getSource().toString();
            String trg = p_lp.getTarget().toString();

            message.append("Failed to retrieve the Localization Profiles ");
            message.append("associated with Locale Pair ");
            message.append(src);
            message.append("/");
            message.append(trg);

            c_logger.error(message.toString(), e);

            String args[] =
            { src, trg };
            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_L10N_PROFILE_DEPENDENCIES_FOR_LOCALE_PAIR,
                    args, e);
        }

        return l10nProfiles;
    }

    /**
     * Return the Active Jobs that are dependent on this LocalePair.
     */
    private Vector activeJobDependencies(LocalePair p_lp)
            throws DependencyCheckException
    {
        Vector activeJobs = null;

        try
        {
            // Add the comany id into the query parameter to fix multi company
            // issue
            long company_id = p_lp.getCompanyId();

            String sql = JobDescriptorModifier.ACTIVE_JOBS_BY_SOURCE_AND_TARGET_SQL;
            Map map = new HashMap();
            map.put("sourceLocaleId", p_lp.getSource().getIdAsLong());
            map.put("targetLocaleId", p_lp.getTarget().getIdAsLong());
            map.put("companyId", company_id);
            activeJobs = new Vector(HibernateUtil.searchWithSql(sql, map,
                    JobImpl.class));
        }
        catch (Exception e)
        {
            StringBuffer message = new StringBuffer();

            String src = p_lp.getSource().toString();
            String trg = p_lp.getTarget().toString();

            message.append("Failed to retrieve the Active Jobs ");
            message.append("associated with Locale Pair ");
            message.append(src);
            message.append("/");
            message.append(trg);

            c_logger.error(message.toString(), e);

            String args[] =
            { src, trg };
            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_ACTIVE_JOB_DEPENDENCIES_FOR_LOCALE_PAIR,
                    args, e);
        }

        return activeJobs;
    }

    /**
     * Return the WorkflowTemplates that are dependent on this LocalePair.
     */
    private Vector workflowTemplateDependencies(LocalePair p_lp)
            throws DependencyCheckException
    {
        Vector wfTemplates = null;

        try
        {
            // Get all active Workflow Templates that are associated
            // with the specified locale pair.
            Collection templates = getProjectHandler()
                    .getAllWorkflowTemplateInfosByLocalePair(p_lp);
            wfTemplates = new Vector(templates);

        }
        catch (Exception e)
        {
            StringBuffer errorMessage = new StringBuffer(
                    "Failed to query for all active workflow templates associated "
                            + "with locale pair ");
            errorMessage.append(p_lp.toString());

            c_logger.error(errorMessage.toString(), e);

            String[] args =
            { p_lp.getSource().toString(), p_lp.getTarget().toString() };
            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_WORKFLOW_TEMPLATE_DEPENDENCIES_FOR_LOCALE_PAIR,
                    args, e);
        }

        return wfTemplates;
    }

    /**
     * Wraps the code for getting the project handler.
     */
    private ProjectHandler getProjectHandler() throws DependencyCheckException
    {
        ProjectHandler ph = null;

        try
        {
            ph = ServerProxy.getProjectHandler();
        }
        catch (Exception e)
        {
            c_logger.error("Couldn't find the ProjectHandler", e);

            throw new DependencyCheckException(
                    DependencyCheckException.MSG_FAILED_TO_FIND_PROJECT_HANDLER,
                    null, e);
        }

        return ph;
    }

}
