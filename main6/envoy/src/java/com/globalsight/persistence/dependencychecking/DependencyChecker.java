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

//globalsight classes
import com.globalsight.persistence.dependencychecking.DependencyCheckException;
import com.globalsight.everest.persistence.PersistentObject;

//java core classes
import java.util.Hashtable;
import java.util.Vector;


public abstract class DependencyChecker
{
    private static final Logger c_logger =
        Logger.getLogger(
            DependencyChecker.class);

    /**
     * Returns all the dependency names categorized by their type.
     *
     * @param p_object The object to test if there are dependencies on it.
     *
     * @return A Hashtable that contains the names of the objects
     * dependent on p_object.  The key is the type of the object
     * (i.e. Job, FileProfile, DbProfile) and the value is a Vector of
     * all the names of the objects that are dependent on p_object.
     *
     * @exception A general exception that wraps the specific
     * exception of why retrieving the dependencies failed.
     */
    public Hashtable categorizeDependencies(PersistentObject p_object)
        throws DependencyCheckException
    {
        // find all the dependencies
        Vector deps = findDependencies(p_object);

        // categorize them and only return their names
        Hashtable depNames = new Hashtable();
        for (int i = 0 ;i < deps.size(); i++)
        {
            PersistentObject obj = (PersistentObject)deps.get(i);
            Vector names = null;
            if (depNames.containsKey(obj.getClass().getName()))
            {
                names = (Vector)depNames.get(obj.getClass().getName());
            }
            else
            {
                names = new Vector();
            }

            names.add(obj.getName());

            // add or replace the value with the expanded collection
            depNames.put(obj.getClass().getName(), names);
        }

        return depNames;
    }


    /**
     * Returns the dependencies in a Vector of Objects.  The vector
     * contains all objects that are dependent on the object being
     * passed in.  This needs to be overwritten by a sub-class
     * particular to dependencies on it.
     *
     * @param p_object The object to test if there are dependencies on.
     *
     * @return A vector of all objects that are dependent on p_object.
     * @exception A general exception that wraps the specific
     * exception of why retrieving the dependencies failed.
     */
    abstract protected Vector findDependencies(PersistentObject p_object)
        throws DependencyCheckException;
}
