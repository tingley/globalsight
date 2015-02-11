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

import com.globalsight.cxe.entity.fileextension.FileExtensionImpl;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class FileExtensionDependencyChecker extends DependencyChecker
{

    @Override
    protected Vector findDependencies(PersistentObject p_object)
            throws DependencyCheckException
    {
        // check this is a FileExtension
        if (p_object.getClass() != FileExtensionImpl.class)
        {
            String args[] =
            { this.getClass().getName(), p_object.getClass().getName() };
            throw new DependencyCheckException(
                    DependencyCheckException.MSG_INVALID_OBJECT, args, null);
        }
        
        FileExtensionImpl fe = (FileExtensionImpl)p_object;
        Vector deps = fileProfileDependencies(fe);
        return deps;
    }

    private Vector fileProfileDependencies(FileExtensionImpl fe)
    {
        Vector fileProfiles = null;
        String hql = "select distinct fp from FileProfileImpl fp inner join fp.extensionIds fpe "
                + "where fp.isActive = 'Y' and fpe.id = :sId ";
        Map map = new HashMap();
        map.put("sId", fe.getId());
        fileProfiles = new Vector(HibernateUtil.search(hql, map));

        return fileProfiles;
    }

}
