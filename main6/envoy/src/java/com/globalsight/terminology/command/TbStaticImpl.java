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

package com.globalsight.terminology.command;

import java.util.ArrayList;
import java.util.Iterator;

import com.globalsight.ling.lucene.Index;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.StatisticsInfo;
import com.globalsight.terminology.TermbaseException;

public class TbStaticImpl implements ITbaseStatic
{
    private Index m_conceptLevelFulltextIndex;
    private ArrayList m_fuzzyIndexes = new ArrayList();
    private ArrayList m_fulltextIndexes = new ArrayList();

    public void setConceptIndex(Index m_conceptLevelFulltextIndex)
    {
        this.m_conceptLevelFulltextIndex = m_conceptLevelFulltextIndex;
    }

    public void setFullTextIndex(ArrayList m_fulltextIndexes)
    {
        this.m_fulltextIndexes = m_fulltextIndexes;
    }

    public void setFuzzyIndex(ArrayList m_fuzzyIndexes)
    {
        this.m_fuzzyIndexes = m_fuzzyIndexes;
    }

    private StatisticsInfo getStatisticsFromDatabase(long m_id)
            throws TermbaseException
    {
        StatisticsInfo result = new StatisticsInfo();
        int count;

        try
        {
            // Number of concepts
            com.globalsight.terminology.java.Termbase tbase = HibernateUtil
                    .get(com.globalsight.terminology.java.Termbase.class, m_id);
            result.setTermbase(tbase.getName());
            String hql = "select count(*) from TbConcept tc where tc.termbase.id="
                    + m_id;
            count = HibernateUtil.count(hql);
            result.setConcepts(count);

            // Total number of terms
            hql = "select count(*) from TbTerm tt where tt.tbLanguage.concept.termbase.id="
                    + m_id;
            count = HibernateUtil.count(hql);
            result.setTerms(count);

            StringBuffer sb = new StringBuffer();
            sb.append("select tt.language, count(tt) from TbTerm tt where");
            sb.append(" tt.tbLanguage.concept.termbase.id=");
            sb.append(m_id);
            sb.append(" group by tt.language");

            hql = sb.toString();
            Iterator it = HibernateUtil.search(hql).iterator();

            while (it.hasNext())
            {
                Object[] pair = (Object[]) it.next();
                String language = (String) pair[0];
                count = ((Long) pair[1]).intValue();
                result.addLanguageInfo(language, count);
            }
        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }

        return result;
    }

    public String getStatisticsNoIndex(long m_id) throws TermbaseException
    {
        return getStatisticsFromDatabase(m_id).asXML();
    }

    @Override
    public String getStatistics(long m_id) throws TermbaseException
    {
        StatisticsInfo result = getStatisticsFromDatabase(m_id);

        int count;

        // After the raw database numbers, add indexing status and
        // fuzzy and fulltext index counts.
        result.setIndexStatus(StatisticsInfo.INDEX_OK);

        if (m_conceptLevelFulltextIndex != null)
        {
            try
            {
                count = m_conceptLevelFulltextIndex.getDocumentCount();
            }
            catch (Exception ex)
            {
                count = -1;
            }

            result.setFulltextCount(count);
        }

        for (int i = 0, max = m_fulltextIndexes.size(); i < max; i++)
        {
            Index idx = (Index) m_fulltextIndexes.get(i);

            try
            {
                count = idx.getDocumentCount();
            }
            catch (Exception ex)
            {
                count = 0;
            }

            result.setFulltextIndexedCount(idx.getName(), count);
        }

        for (int i = 0, max = m_fuzzyIndexes.size(); i < max; i++)
        {
            Index idx = (Index) m_fuzzyIndexes.get(i);

            try
            {
                count = idx.getDocumentCount();
            }
            catch (Exception ex)
            {
                count = 0;
            }

            result.setFuzzyIndexedCount(idx.getName(), count);
        }

        return result.asXML();
    }
}
