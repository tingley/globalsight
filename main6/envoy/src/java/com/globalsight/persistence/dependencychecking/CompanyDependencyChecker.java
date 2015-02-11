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

import com.globalsight.everest.company.Company;

//globalsight classes
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.dependencychecking.DependencyCheckException;
import com.globalsight.persistence.dependencychecking.DependencyChecker;
import java.util.Vector;

public class CompanyDependencyChecker extends DependencyChecker{

    /**
     * Returns the dependencies in a Vector of Objects.  The vector
     * contains all objects that are dependent on the Company object
     * being passed in.
     *
     * @param p_object The object to test if there are dependencies on.
     *
     * @return A vector of all objects that are dependent on p_object.
     * @exception A general exception that wraps the specific
     * exception of why retrieving the dependencies failed.
     */
    protected Vector findDependencies(PersistentObject p_object)
        throws DependencyCheckException
    {
        if (p_object.getClass() != Company.class)
        {
            String args[] = {
                this.getClass().getName(), p_object.getClass().getName()
            };

            throw new DependencyCheckException(
                DependencyCheckException.MSG_INVALID_OBJECT, args, null);
        }

        Company company = (Company)p_object;

        //FIXME By now, this method is not implemented.
        return null;
    }
}
