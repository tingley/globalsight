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

package com.globalsight.persistence.hibernate.entity;

import java.io.Serializable;

import com.globalsight.everest.localemgr.CodeSetImpl;
import com.globalsight.util.GlobalSightLocale;

public class LocaleCodeSet implements Serializable
{
    private static final long serialVersionUID = 6813688494053945235L;

    private GlobalSightLocale local;
    private CodeSetImpl codeSet;
    public boolean useActive = false;

    public CodeSetImpl getCodeSet()
    {
        return codeSet;
    }

    public void setCodeSet(CodeSetImpl codeSet)
    {
        this.codeSet = codeSet;
    }

    public GlobalSightLocale getLocal()
    {
        return local;
    }

    public void setLocal(GlobalSightLocale local)
    {
        this.local = local;
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object paramObject)
    {
        return super.equals(paramObject);
    }
}
