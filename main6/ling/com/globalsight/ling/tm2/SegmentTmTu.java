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
package com.globalsight.ling.tm2;

import com.globalsight.util.GlobalSightLocale;

/**
 * SegmentTmTu represents a Tu in Segment Tm table.
 */

public class SegmentTmTu extends AbstractTmTu
{
    public static final String ROOT = "0";

    private GlobalSightLocale m_sourceLocale = null;

    // subflow id. This attribute is set when a complete gxml is
    // breaked up into a main text and subflows and each forms
    // Tu. This attribute is not persisted. The main text's sub id is
    // "0". Each subflow's id should be set in an original gxml.
    private String m_subId = null;
    private String m_SID = null;

    // For GBS-676. There are some difference between ambassador and worldserver
    // on Placeholders.
    // For example:
    // Source Segment: This is <b>bold</b>.
    // ambassador: <seg>This is <bpt type="bold" i="2"
    // x="1">&lt;B&gt;</bpt>bold<ept i="2">&lt;/B&gt;</ept>. </seg>
    // worldserver: <seg>This is <ph x="1">{1}</ph>bold<ph x="2">{2}</ph>.
    // </seg>
    private boolean fromWorldServer = false;

    /**
     * Default constructor.
     */
    public SegmentTmTu()
    {
        super();
    }

    public String getSID()
    {
        return m_SID;
    }

    public void setSID(String m_sid)
    {
        m_SID = m_sid;
    }

    /**
     * Constructor.
     * 
     * @param p_id
     *            id
     * @param p_tmId
     *            tm id
     * @param p_format
     *            format name
     * @param p_type
     *            type name
     * @param p_translatable
     *            set this Tu translatable if this param is true
     */
    public SegmentTmTu(long p_id, long p_tmId, String p_format, String p_type,
            boolean p_translatable, GlobalSightLocale p_sourceLocale)
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

    public String getSubId()
    {
        return m_subId;
    }

    public void setSubId(String p_subId)
    {
        m_subId = p_subId;
    }

    public BaseTmTuv getSourceTuv()
    {
        GlobalSightLocale source = getSourceLocale();
        return getFirstTuv(source);
    }

    public boolean isFromWorldServer()
    {
        return fromWorldServer;
    }

    public void setFromWorldServer(boolean fromWorldServer)
    {
        this.fromWorldServer = fromWorldServer;
    }

    @Override
    protected String getExtraDebugInfo()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" #subId=").append(getSubId())
          .append(" #fromWS=").append(isFromWorldServer());
        return sb.toString();
    }

}
