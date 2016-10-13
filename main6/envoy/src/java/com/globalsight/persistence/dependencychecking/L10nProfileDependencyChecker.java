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

// globalsight classes
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Checks for objects that may have dependencies on a particular L10nProfile.
 * The class dependencies are hard-coded into this class.
 */
public class L10nProfileDependencyChecker extends DependencyChecker
{
    private static final Logger c_logger = Logger
            .getLogger(L10nProfileDependencyChecker.class);

    /**
     * Returns the dependencies in an Vector of Objects. The Vector contains all
     * objects that are dependent on the object being passed in. This needs to
     * be overwritten by a sub-class particular to dependencies on it.
     * 
     * @param p_object
     *            The object to test if there are dependencies on.
     * 
     * @return A Vector of all objects that are dependent on p_object.
     */
    protected Vector findDependencies(PersistentObject p_object)
            throws DependencyCheckException
    {
        if (p_object.getClass() != BasicL10nProfile.class)
        {
            String args[] = { this.getClass().getName(),
                    p_object.getClass().getName() };
            throw new DependencyCheckException(
                    DependencyCheckException.MSG_INVALID_OBJECT, args, null);
        }

        BasicL10nProfile prof = (BasicL10nProfile) p_object;

        Vector deps = fileProfileDependencies(prof);
        deps.addAll(dbProfileDependencies(prof));

        return deps;
    }

    /**
     * Return the FileProfiles that are dependent on this L10nProfile.
     */
    private Vector fileProfileDependencies(BasicL10nProfile p_profile)
            throws DependencyCheckException
    {
        Vector fileProfiles = new Vector();

        try
        {
            String hql = "from FileProfileImpl f "
                + "where f.isActive='Y' and f.l10nProfileId = :fId order by f.name";
            Map map = new HashMap();
            map.put("fId", p_profile.getIdAsLong());
            
            fileProfiles = new Vector(HibernateUtil.search(hql, map));
        }
        catch (Exception e)
        {
            StringBuffer message = new StringBuffer();

            message.append("Failed to retrieve the File Profiles associated ");
            message.append("with Localization Profile ");
            message.append(p_profile.getId());
            message.append(": ");
            message.append(p_profile.getName());

            c_logger.error(message.toString(), e);

            String args[] = { Long.toString(p_profile.getId()),
                    p_profile.getName() };
            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_FILE_PROFILE_DEPENDENCIES_FOR_L10N_PROFILE,
                    args, e);
        }

        return fileProfiles;
    }

    /**
     * Return the FileProfiles that are dependent on this L10nProfile.
     */
    private Vector dbProfileDependencies(BasicL10nProfile p_profile)
            throws DependencyCheckException
    {
        Vector dbProfiles = new Vector();

        try
        {
            // get all the database profiles associated with this l10nProfile
            String hql = "from DatabaseProfileImpl d "
                    + "where d.l10nProfileId = :lId";
            Map map = new HashMap();
            map.put("lId", p_profile.getIdAsLong());

            dbProfiles = new Vector(HibernateUtil.search(hql, map));
        }
        catch (Exception e)
        {
            StringBuffer message = new StringBuffer();

            message.append("Failed to retrieve the Db Profiles associated ");
            message.append("with Localization Profile ");
            message.append(p_profile.getId());
            message.append(": ");
            message.append(p_profile.getName());

            c_logger.error(message.toString(), e);

            String args[] = { Long.toString(p_profile.getId()),
                    p_profile.getName() };
            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_DB_PROFILE_DEPENDENCIES_FOR_L10N_PROFILE,
                    args, e);
        }

        return dbProfiles;
    }
}
