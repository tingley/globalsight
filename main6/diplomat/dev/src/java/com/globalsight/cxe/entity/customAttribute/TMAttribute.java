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

package com.globalsight.cxe.entity.customAttribute;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.ProjectTM;

public class TMAttribute extends PersistentObject
{
    static private final Logger logger = Logger
            .getLogger(TMAttribute.class);

    private static final long serialVersionUID = 2615661666370805250L;
    
    private ProjectTM tm;
    private String attributename;
    private String settype;

    public ProjectTM getTm()
    {
        return tm;
    }

    public void setTm(ProjectTM tm)
    {
        this.tm = tm;
    }

    public String getAttributename()
    {
        return attributename;
    }

    public void setAttributename(String attributename)
    {
        this.attributename = attributename;
    }

    public String getSettype()
    {
        return settype;
    }

    public void setSettype(String settype)
    {
        this.settype = settype;
    }
    
    @Override
    public String toString()
    {
        return attributename + " : " + settype;
    }
}
