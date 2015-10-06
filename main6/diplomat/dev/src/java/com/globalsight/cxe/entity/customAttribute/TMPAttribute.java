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
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;

public class TMPAttribute extends PersistentObject
{

    static private final Logger logger = Logger.getLogger(TMPAttribute.class);

    private static final long serialVersionUID = 4063148292014335592L;

    private TranslationMemoryProfile tmprofile;
    private String attributeName;
    private String operator;
    private String valueType;
    private String valueData;
    private String andOr;
    private int order;

    public String getAttributeName()
    {
        return attributeName;
    }

    public void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
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

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getAttributeName());
        sb.append(":");
        sb.append(getOperator());
        sb.append(":");
        sb.append(getValueType());
        sb.append(":");
        sb.append(getValueData());
        sb.append(":");
        sb.append(getOrder());
        sb.append(":");
        sb.append(getAndOr());
        
        return sb.toString();
    }

	public String getAndOr() {
		return andOr;
	}

	public void setAndOr(String andOr) {
		this.andOr = andOr;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
