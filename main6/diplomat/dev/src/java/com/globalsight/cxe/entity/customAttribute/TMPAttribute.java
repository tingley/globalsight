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
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;

public class TMPAttribute extends PersistentObject
{

    static private final Logger logger = Logger.getLogger(TMPAttribute.class);

    private static final long serialVersionUID = 4063148292014335592L;

    private TranslationMemoryProfile tmprofile;
    private String attributename;
    private String operator;
    private String valueType;
    private String valueData;
    private int penalty;

    public String getAttributename()
    {
        return attributename;
    }

    public void setAttributename(String attributename)
    {
        this.attributename = attributename;
    }

    public TranslationMemoryProfile getTmprofile()
    {
        return tmprofile;
    }

    public void setTmprofile(TranslationMemoryProfile tmprofile)
    {
        this.tmprofile = tmprofile;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public String getValueType()
    {
        return valueType;
    }

    public void setValueType(String valueType)
    {
        this.valueType = valueType;
    }

    public String getValueData()
    {
        return valueData;
    }

    public void setValueData(String valueData)
    {
        this.valueData = valueData;
    }

    public int getPenalty()
    {
        return penalty;
    }

    public void setPenalty(int penalty)
    {
        this.penalty = penalty;
    }
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getAttributename());
        sb.append(":");
        sb.append(getOperator());
        sb.append(":");
        sb.append(getValueType());
        sb.append(":");
        sb.append(getValueData());
        sb.append(":");
        sb.append(getPenalty());
        
        return sb.toString();
    }
}
