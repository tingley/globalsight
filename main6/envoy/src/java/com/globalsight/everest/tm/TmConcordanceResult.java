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

package com.globalsight.everest.tm;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.util.GlobalSightLocale;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

public class TmConcordanceResult
    implements Serializable
{
    private GlobalSightLocale m_sourceLocale;
    private ArrayList m_targetLocaleList = new ArrayList();
    private ArrayList m_tuList = new ArrayList();


    public TmConcordanceResult(GlobalSightLocale p_sourceLocale,
        Collection p_targetLocaleList)
    {
        m_sourceLocale = p_sourceLocale;
        m_targetLocaleList.addAll(p_targetLocaleList);
    }

    public void addTu(Tu p_tu)
    {
        m_tuList.add(p_tu);
    }


    /**
     * returns a collection of Tu
     */
    public ArrayList getTuList()
    {
        return m_tuList;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    public ArrayList getTargetLocaleList()
    {
        return m_targetLocaleList;
    }
}
