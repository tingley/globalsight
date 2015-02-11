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
package com.globalsight.everest.page;

import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.persistence.PersistentObject;

import com.globalsight.util.edit.EditUtil;

/**
 * The TemplatePart is a unit of PageTemplate which contains a non-translation
 * contextual information a translation unit id and its order in the
 * PageTemplate.
 */
public class TemplatePart extends PersistentObject
{
    private static final long serialVersionUID = -5915760601263219407L;

    public static final String ORDER = "m_order";

    /**
     * <p>
     * Value returned by getTuId() if a TemplatePart does not have a tu id (-1).
     * </p>
     */
    public static final long INVALID_TUID = -1;
    public static final Long INVALID_TUID_LONG = new Long(INVALID_TUID);

    private static final int CLOB_THRESHOLD = 4000;

    private String m_skeletonClob;
    private String m_skeletonString;
    private Tu m_tu = null;
    private int m_order;
    private PageTemplate m_pageTemplate = null;

    // //////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // //////////////////////////////////////////////////////////////////
    /**
     * Constructor used by TopLink ONLY.
     */
    public TemplatePart()
    {
    }

    public TemplatePart(PageTemplate p_pageTemplate, String p_skeleton,
            Tu p_tu, int p_order)
    {
        m_pageTemplate = p_pageTemplate;
        m_tu = p_tu;
        m_order = p_order;
        setSkeleton(p_skeleton);
    }

    // //////////////////////////////////////////////////////////////////
    // End: Constructor
    // //////////////////////////////////////////////////////////////////
    public Tu getTu()
    {
        return m_tu;
    }

    public void setTu(Tu tu)
    {
        m_tu = tu;
    }

    public long getTemplateId()
    {
        return m_pageTemplate.getId();
    }

    public String getSkeletonClob()
    {
        return m_skeletonClob;
    }

    public void setSkeletonClob(String skeletonClob)
    {
        m_skeletonClob = skeletonClob;
    }

    public String getSkeletonString()
    {
        return m_skeletonString;
    }

    public void setSkeletonString(String skeletonString)
    {
        m_skeletonString = skeletonString;
    }

    public String getSkeleton()
    {
        String s = m_skeletonString;
        if (s == null)
        {
            s = m_skeletonClob;
        }
        return s == null ? "" : s;
    }

    public long getTuId()
    {
        return m_tu == null ? INVALID_TUID : m_tu.getId();
    }

    public Long getTuIdAsLong()
    {
        return m_tu == null ? INVALID_TUID_LONG : m_tu.getIdAsLong();
    }

    public int getOrder()
    {
        return m_order;
    }

    public void setOrder(int order)
    {
        m_order = order;
    }

    /**
     * Display the string representation of this object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        String s = getSkeleton();
        sb.append(super.toString());
        sb.append(" order=");
        sb.append(m_order);
        sb.append(" skeleton=");
        sb.append(s.length() > 0 ? s : "<empty>");
        sb.append(" Tu=");
        sb.append(m_tu);
        return sb.toString();
    }

    // package level method that is called by PageTemplate.
    // This may return NULL. Not every template part has a
    // TUV - some are just skeletons.
    Tuv getTuv(long p_localeId)
    {
        if (m_tu != null)
            return m_tu.getTuv(p_localeId);
        else
            return null;
    }

    public void setSkeleton(String p_skeleton)
    {
        m_skeletonClob = null;
        m_skeletonString = null;
        if (p_skeleton != null)
        {
            if (EditUtil.getUTF8Len(p_skeleton) > CLOB_THRESHOLD)
            {
                m_skeletonClob = p_skeleton;
            }
            else
            {
                m_skeletonString = p_skeleton;
            }
        }
    }

    public PageTemplate getPageTemplate()
    {
        return m_pageTemplate;
    }

    public void setPageTemplate(PageTemplate template)
    {
        m_pageTemplate = template;
    }
}
