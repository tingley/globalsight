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

package com.globalsight.terminology;

import com.globalsight.ling.lucene.Index;
import com.globalsight.terminology.java.TbUtil;

public class FuzzySearch extends AbstractTermSearch
{
    public Index getFuzzyIndex(String p_language)
    {
        for (int i = 0, max = indexes.size(); i < max; i++)
        {
            Index index = (Index) indexes.get(i);

            if (index.getName().equals(p_language))
            {
                return index;
            }
        }

        return null;
    }

    public Hitlist getHitListResults(String p_language, String trgLan,
            String p_query, int maxHits, int begin) throws TermbaseException
    {
        Hitlist result = new Hitlist();

        // Lucene indexes have different scores, use lower threshold.
        // I've seen 2% matches that seemed to make some sense, but the
        // 1% matches were garbage.
        // Screw it, the threshold doesn't do it.
        // float threshold = 0.0f;
        float threshold = 0.001f;

        if (p_query.length() == 0)
        {
            return result;
        }

        Index index = getFuzzyIndex(p_language);
        if (index == null)
        {
            return result;
        }

        // Perform the search in a Lucene index and copy the results.

        com.globalsight.ling.lucene.Hits hits = null;

        try
        {
            hits = index.search(p_query, (begin + maxHits), begin, threshold);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Search fuzzy index " + index.getName()
                        + " query=" + p_query + " maxhits=" + maxHits
                        + " threshold=" + threshold + " returned "
                        + hits.size() + " results");
            }

            for (int i = 0, max = hits.size(); i < max; i++)
            {
                // Allow empty or null target language
                if (trgLan == null || "".equals(trgLan.trim())
                        || TbUtil.ConceptIfHasTrcLan(hits.getMainId(i), trgLan))
                {
                    result.add(hits.getText(i), hits.getMainId(i), hits
                            .getSubId(i), (int) (hits.getScore(i) * 100.0), "");
                }
            }
        }
        catch (Exception ex)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("cannot search fuzzy index", ex);
            }

            return result;
        }

        if (index.isNullOfSearch())
        {
            flag = "noResults";
        }
        else if (result.getHits().size() == 0)
        {
            //only use one word search, is also no results.
            if (begin == 0)
            {
                flag = "noResults";
            }
            else
            {
                flag = "isLast";
            }
        }
        else
        {
            flag = "";
        }

        // For fuzzy searches, the index stores a copy of the original
        // term so we have all information at this point.
        // (Not sure what to do for fulltext searches.)

        return result;
    }
}
