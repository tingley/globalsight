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
package com.globalsight.ling.tm2.leverage;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.PageTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;

/**
 * LeveragedPageTu is a class that represents a Tu of leveraged
 * Page Tm segment.
 */

public class LeveragedPageTu
    extends PageTmTu
    implements LeveragedTu
{
    private GlobalSightLocale m_sourceLocale = null;
    private MatchState m_state = null;
    private float m_score = 0.0f;
    private int m_matchTableType = PAGE_TM;
    
    // It always false;
    private boolean fromWorldServer = false;

    /**
     * Constructor.
     * @param p_id id
     * @param p_tmId tm id
     * @param p_format format name
     * @param p_type type name
     * @param p_translatable set this Tu translatable if this param is true
     * @param p_sourceLocale source locale
     */
    public LeveragedPageTu(long p_id, long p_tmId, String p_format,
        String p_type, boolean p_translatable,
        GlobalSightLocale p_sourceLocale)
    {
        super(p_id, p_tmId, p_format, p_type, p_translatable);
        m_sourceLocale = p_sourceLocale;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }
    

    public void setSourceLocale(GlobalSightLocale p_sourceLocale)
    {
        m_sourceLocale = p_sourceLocale;
    }
    

    public MatchState getMatchState()
    {
        return m_state;
    }
    
    
    public void setMatchState(MatchState p_state)
    {
        m_state = p_state;
    }
    
    
    public float getScore()
    {
        return m_score;
    }
    
    
    public void setScore(float p_score)
    {
        m_score = p_score;
    }
    
    public int getMatchTableType()
    {
        return m_matchTableType;
    }

    public void setMatchTableType(int p_matchTableType)
    {
        m_matchTableType = p_matchTableType;
    }
    
    public BaseTmTuv getSourceTuv()
    {
        GlobalSightLocale source = getSourceLocale();
        return getFirstTuv(source);
    }

    @Override
    public boolean isFromWorldServer()
    {
        return fromWorldServer;
    }

    @Override
    public void setFromWorldServer(boolean fromWorldServer)
    {
        this.fromWorldServer = fromWorldServer;
    }
}
