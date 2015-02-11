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

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.PageTmTuv;
import com.globalsight.util.GlobalSightLocale;

/**
 * LeveragedPageTuv is a class that represents a Tuv of leveraged Page Tm
 * segment.
 */

public class LeveragedPageTuv extends PageTmTuv implements LeveragedTuv
{
    private MatchState m_state = null;
    private float m_score = 0.0f;
    private int m_order = 0;
    private String orgSid = null;

    /**
     * Constructor.
     * 
     * @param p_id
     *            id
     * @param p_segment
     *            segment string
     * @param p_locale
     *            GlobalSightLocale
     */
    public LeveragedPageTuv(long p_id, String p_segment,
            GlobalSightLocale p_locale)
    {
        super(p_id, p_segment, p_locale);
    }

    public BaseTmTuv getSourceTuv()
    {
        GlobalSightLocale source = ((LeveragedTu) getTu()).getSourceLocale();
        return getTu().getFirstTuv(source);
    }

    public MatchState getMatchState()
    {
        return m_state == null ? ((LeveragedTu) getTu()).getMatchState()
                : m_state;
    }

    public void setMatchState(MatchState p_state)
    {
        m_state = p_state;
    }

    public float getScore()
    {
        return m_score == 0.0 ? ((LeveragedTu) getTu()).getScore() : m_score;
    }

    public void setScore(float p_score)
    {
        m_score = p_score;
    }

    public int getOrder()
    {
        return m_order;
    }

    public void setOrder(int p_order)
    {
        m_order = p_order;
    }

    @Override
    public String getOrgSid(long jobId)
    {
        return orgSid;
    }

    public void setOrgSid(String orgSid)
    {
        this.orgSid = orgSid;
    }

    @Override
    public String getOrgSid()
    {
        return getOrgSid(-1);
    }
}
