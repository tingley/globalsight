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
package com.globalsight.ling.inprogresstm.population;

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.ling.inprogresstm.leverage.LeveragedInProgressTuv;
import com.globalsight.ling.inprogresstm.persistence.IndexPersistence;
import com.globalsight.ling.inprogresstm.persistence.TmPersistence;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.indexer.Tokenizer;
import com.globalsight.ling.tm2.indexer.WordTokenizer;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Leverager is responsible for leveraging segments
 */
public class TmPopulater
{
    private static final Logger c_logger =
        Logger.getLogger(
            TmPopulater.class.getName());

    /**
     * Save a segment in the in-progress TM.
     *
     * @param p_sourceSegment source segment
     * @param p_targetSegment target segment
     * @param p_sourcePage source page
     */
    public void saveSegment(BaseTmTuv p_sourceSegment,
        BaseTmTuv p_targetSegment, SourcePage p_sourcePage)
        throws Exception
    {
        long jobId = getJobId(p_sourcePage);

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);
            TmPersistence tmPersistence = new TmPersistence(conn);
            
            // get table lock
            tmPersistence.getLockForSave(p_sourceSegment.isTranslatable());
            
            // find TUs in the TM containing the given source text
            // (based on the exact match key and the type)
            Collection tus = tmPersistence.getTusForPopulation(
                p_sourceSegment.getLocale(), p_targetSegment.getLocale(),
                p_sourceSegment.getExactMatchKey(), jobId,
                p_sourceSegment.getType(), p_sourceSegment.isTranslatable());

            // find a TU that really contains the given source text by
            // comparing the text
            BaseTmTu tu = getTuWithSourceText(tus, p_sourceSegment);
            
            if(tu != null)
            {
                // find a target TUV that has the same job data id as
                // the given TUV (excluding one that has the same text
                // as the given TUV)
                long trgTuvId = getSameJobDataIdTuv(tu, p_targetSegment);

                if(identicalTuvExists(tu, p_targetSegment))
                {
                    // there already is a TUV with the same text so no
                    // need to save the given TUV, but...
                    if(trgTuvId != 0)
                    {
                        // delete a TUV that has the same job data id
                        // and different text
                        tmPersistence.deleteTrgTuv(
                            trgTuvId, p_sourceSegment.isTranslatable());
                    }
                }
                else
                {
                    if(trgTuvId == 0)
                    {
                        // there is no TUV that has the same text nor
                        // the same job data id
                        long srcId = tu.getFirstTuv(
                            p_sourceSegment.getLocale()).getId();
                        tmPersistence.insertTrgSegment(p_targetSegment,
                            srcId, p_targetSegment.getTu().getId());
                    }
                    else
                    {
                        // there is a TUV that has the same job id,
                        // update it
                        tmPersistence.updateTrgSegment(
                            trgTuvId, p_targetSegment);
                    }
                }
            }
            else
            {
                // source text is not in the TM yet. Create the source
                // and target segments
                long populationTmId = getPopulationTmId(p_sourcePage);

                long srcId = tmPersistence.insertSrcSegment(
                    p_sourceSegment, jobId, populationTmId);
                
                tmPersistence.insertTrgSegment(
                    p_targetSegment, srcId, p_targetSegment.getTu().getId());

                // index translatable segment
                if(p_sourceSegment.isTranslatable())
                {
                    Collection tokens
                        = getTokens(p_sourceSegment, srcId, jobId);

                    IndexPersistence indexPersistence
                        = new IndexPersistence(conn);
                    indexPersistence.insertTokens(
                        tokens, populationTmId, p_sourceSegment.getLocale());
                }
            }

            conn.commit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            conn.rollback();
            
            throw e;
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    DbUtil.unlockTables(conn);
                    conn.setAutoCommit(true);
                    DbUtil.returnConnection(conn);
                }
                catch(Exception e)
                {
                    throw e;
                }
            }
        }
    }

    private long getJobId(SourcePage p_sorucePage)
    {
        return p_sorucePage.getJobId();
    }
    

    private long getPopulationTmId(SourcePage p_sorucePage)
        throws Exception
    {
        TranslationMemoryProfile tmProfile = p_sorucePage.getRequest()
            .getL10nProfile().getTranslationMemoryProfile();
        return tmProfile.getProjectTmIdForSave();
    }
    
    // find a TU that really contains the given source text by
    // comparing the text
    //
    // returns the TU or null if not found
    private BaseTmTu getTuWithSourceText(
        Collection p_tus, BaseTmTuv p_sourceSegment)
        throws Exception
    {
        GlobalSightLocale srcLocale = p_sourceSegment.getLocale();
        String srcText = p_sourceSegment.getExactMatchFormat();
        
        BaseTmTu result = null;
        
        for(Iterator it = p_tus.iterator(); it.hasNext();)
        {
            BaseTmTu tu = (BaseTmTu)it.next();
            BaseTmTuv srcTuv = tu.getFirstTuv(srcLocale);
            if(srcText.equals(srcTuv.getExactMatchFormat()))
            {
                result = tu;
                break;
            }
        }
        
        return result;
    }
    
    // find a target TUV that has the same job data id as the given
    // TUV (excluding one that has the same text as the given TUV)
    //
    // returns the TUV id or 0 if a TUV is not found
    private long getSameJobDataIdTuv(BaseTmTu p_tu, BaseTmTuv p_targetSegment)
        throws Exception
    {
        long tuvId = 0;

        Collection tuvs = p_tu.getTuvList(p_targetSegment.getLocale());
        if(tuvs != null)
        {
            String trgText = p_targetSegment.getExactMatchFormat();
        
            for(Iterator it = tuvs.iterator(); it.hasNext();)
            {
                LeveragedInProgressTuv tuv
                    = (LeveragedInProgressTuv)it.next();
                if(tuv.getJobDataTuId() == p_targetSegment.getTu().getId())
                {
                    if(!trgText.equals(tuv.getExactMatchFormat()))
                    {
                        tuvId = tuv.getId();
                    }
                    
                    break;
                }
            }
        }
        
        return tuvId;
    }
    
    // test if there is a TUV in the given TU that has an identical
    // text as the given target segment.
    private boolean identicalTuvExists(
        BaseTmTu p_tu, BaseTmTuv p_targetSegment)
        throws Exception
    {
        boolean result = false;

        Collection tuvs = p_tu.getTuvList(p_targetSegment.getLocale());
        if(tuvs != null)
        {
            String trgText = p_targetSegment.getExactMatchFormat();
        
            for(Iterator it = tuvs.iterator(); it.hasNext();)
            {
                BaseTmTuv tuv = (BaseTmTuv)it.next();
                if(trgText.equals(tuv.getExactMatchFormat()))
                {
                    result = true;
                    break;
                }
            }
        }
        
        return result;
    }
    
    private Collection getTokens(BaseTmTuv p_sourceSegment, long p_srcId,
            long p_jobId) throws Exception
    {
        // Word tokenizer is used in in-progress TM regardless of the
        // user setting
        Tokenizer tokenizer = new WordTokenizer();

        Collection tokens = tokenizer.tokenize(p_sourceSegment
                .getFuzzyIndexFormat(), p_srcId, p_srcId, p_jobId,
                p_sourceSegment.getLocale(), true);

        return tokens;
    }

}
