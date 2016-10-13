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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.l10nprofile.L10nProfileDescriptorModifier;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Checks for objects that may have dependencies
 * on a particular Workflow Template.  The class dependencies are
 * hard-coded into this class.
 */
public class WorkflowTemplateDependencyChecker
    extends DependencyChecker
{
    private static final Logger c_logger =
        Logger.getLogger(
            WorkflowTemplateDependencyChecker.class);

    /**
     * Returns the dependencies in a Vector of Objects.  The vector
     * contains all objects that are dependent on the Workflow Template
     * object being passed in.
     *
     * @param p_object The object to test if there are dependencies on.
     * @return A vector of all objects that are dependent on p_object.
     * @exception A general exception that wraps the specific
     * exception of why retrieving the dependencies failed.
     */
    protected Vector findDependencies(PersistentObject p_object)
        throws DependencyCheckException
    {
        // check this is a Workflow Template
        if (p_object.getClass() != WorkflowTemplateInfo.class)
        {
            String args[] = 
            {
                this.getClass().getName(), p_object.getClass().getName()
            };
            throw new DependencyCheckException(
                DependencyCheckException.MSG_INVALID_OBJECT, args, null);
        }

        WorkflowTemplateInfo wfti = (WorkflowTemplateInfo)p_object;
        Vector deps = findL10nProfileDependencies(wfti);

        return deps;
    }

    /**
     * Return the L10nProfiles that are dependent on this Workflow Template.
     */
    private Vector findL10nProfileDependencies(WorkflowTemplateInfo wfti)
        throws DependencyCheckException
    {
        Vector l10nProfiles = null;

        try
        {
            String sql = L10nProfileDescriptorModifier.L10N_PROFILE_BY_WORKFLOW_TEMPLATE_ID;
            Map map = new HashMap();
            map.put("workflowTemplateId", wfti.getIdAsLong());
            
            l10nProfiles = new Vector(HibernateUtil.searchWithSql(sql, map,
                    BasicL10nProfile.class));
            
        }
        catch (Exception e)
        {
            StringBuffer message = new StringBuffer();

            String wfName = wfti.getName();

            message.append("Failed to retrieve the Localization Profiles associated with Workflow Template.");
            
            /*
            message.append(src);
            message.append("/");
            message.append(trg);
            */

            c_logger.error(message.toString(), e);

            String args[] = { wfName };
            throw new DependencyCheckException(
                DependencyCheckException.FAILED_TO_GET_L10N_PROFILES_BY_WORKLFLOW_TEMPLATE_IDS,
                args, e);
        }

        return l10nProfiles;
    }
    
}
