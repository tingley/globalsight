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
package com.globalsight.ling.tm2.population;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;


/**
 * A repository of segments. This class is responsible for maintaining
 * uniqueness of source and target segment in this repository. This
 * class also maintains a list of identical segments to build a corpus
 * tm.
 */

public class UniqueSegmentRepositoryForCorpus
    extends UniqueSegmentRepository
{
    // a repository of Tuvs. This map maintains lists of identical segments.
    // key: BaseTmTuv
    // value: a list of identical BaseTmTuv in the page
    private Map<BaseTmTuv, List<BaseTmTuv>> m_identicalTuvs;
    
    /**
     * Constructor.
     * @param p_sourceLocale source locale
     */
    public UniqueSegmentRepositoryForCorpus(GlobalSightLocale p_sourceLocale)
    {
        super(p_sourceLocale);
        m_identicalTuvs = new HashMap<BaseTmTuv, List<BaseTmTuv>>();
    }


    /**
     * Add Tu (and Tuvs that are owned by the Tu). Only unique
     * segments are added. This method assumes the parameter is
     * SegmentTmTu.
     *
     * @param p_tu Tu to be added
     */
    public void addTu(BaseTmTu p_tu)
    {
        // only translatable and root segments are maintained in the list
        if(p_tu.isTranslatable() &&
            ((SegmentTmTu)p_tu).getSubId().equals(SegmentTmTu.ROOT))
        {
            for (BaseTmTuv tuv : p_tu.getTuvs())
            {
                List<BaseTmTuv> identicalTuvs = m_identicalTuvs.get(tuv);
                if(identicalTuvs == null)
                {
                    identicalTuvs = new ArrayList<BaseTmTuv>();
                    m_identicalTuvs.put(tuv, identicalTuvs);
                }
                identicalTuvs.add(tuv);
            }
        }
        
        // add the tu to the super class object
        super.addTu(p_tu);
    }


    // returns a list of identical segment as the given tuv in the page.
    public List<BaseTmTuv> getIdenticalSegment(BaseTmTuv p_tuv)
    {
        List<BaseTmTuv> tuvs = m_identicalTuvs.get(p_tuv);
        if (tuvs == null) {
            tuvs = Collections.emptyList();
        }
        return tuvs;
    }


    public String toDebugString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("UniqueSegmentRepositoryForCorpus  {\n");
        
        for (Map.Entry<BaseTmTuv, List<BaseTmTuv>> e : m_identicalTuvs.entrySet())
        {
            sb.append("  Key: ").append(e.getKey().toDebugString());
            sb.append("  value: ").append(e.getValue().toString());
        }
        
        sb.append("}\n");
        return sb.toString();
    }
}
