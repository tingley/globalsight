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

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.l10nprofile.L10nProfileQueryNames;
import com.globalsight.persistence.dependencychecking.DependencyCheckException;
import com.globalsight.persistence.dependencychecking.DependencyChecker;
import java.util.Collection;
import java.util.Vector;

/**
 * Checks for objects that may have dependencies on a particular FileProfile.
 * The class dependencies are hard-coded into this class.
 */
public class FileProfileDependencyChecker extends DependencyChecker
{
    private static final Logger c_logger = Logger
            .getLogger(FileProfileDependencyChecker.class);

    /**
     * Returns the dependencies in a Vector of Objects. The vector contains all
     * objects that are dependent on the FileProfile object being passed in.
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
        // check this is a FileProfile
        if (!p_object.getClass().equals(FileProfile.class))
        {
            String args[] = { this.getClass().getName(),
                    p_object.getClass().getName() };
            throw new DependencyCheckException(
                    DependencyCheckException.MSG_INVALID_OBJECT, args, null);
        }

        FileProfile fp = (FileProfile) p_object;

        return l10nProfileDependencies(fp);
    }

    /**
     * Return the L10nProfiles that are dependent on this FileProfile.
     * 
     * @param p_fp
     * @return
     * @exception DependencyCheckException
     */
    private Vector l10nProfileDependencies(FileProfile p_fp)
            throws DependencyCheckException
    {
        Vector l10nProfiles = null;
        /*
         * try { Vector args = new Vector(); args.add(p_fp); l10nProfiles = new
         * Vector( PersistenceService.getInstance().executeNamedQuery(
         * L10nProfileQueryNames.PROFILES_BY_FILE_PROFILE_ID, args, false)); }
         * catch (Exception e) { StringBuffer message = new StringBuffer();
         * 
         * String trg = p_fp.getTarget().toString();
         * 
         * message.append("Failed to retrieve the Localization Profiles
         * associated with File Profile."); message.append(src);
         * message.append("/"); message.append(trg);
         * 
         * c_logger.error(message.toString());
         * 
         * String args[] = { src, trg }; throw new DependencyCheckException(
         * DependencyCheckException.FAILED_L10N_PROFILE_DEPENDENCIES_FOR_LOCALE_PAIR,
         * args,e); }
         */

        return l10nProfiles;
    }
}
