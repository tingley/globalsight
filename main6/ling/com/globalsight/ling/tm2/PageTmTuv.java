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
 * PageTmTuv is a representation of Tuv in Page Tm. And also it is
 * used to represent job data tuv (transltaion_unit_variant). The
 * structure of the two are almost the same.
 */

public class PageTmTuv
    extends AbstractTmTuv
{
    // This attribute is used to store job data's state (COMPLETE,
    // NOT_LOCALIZED, etc)
    private String m_state = null;


    // This attribute is used to store job data's merge state (MERGE_START,
    // MERGE_END, etc)
    private String m_mergeState = null;
    
    /**
     * Default constructor.
     */
    public PageTmTuv()
    {
        super();
    }
    

    /**
     * Constructor.
     * @param p_id id
     * @param p_segment segment string
     * @param p_locale GlobalSightLocale
     */
    public PageTmTuv(long p_id, String p_segment, GlobalSightLocale p_locale)
    {
        super(p_id, p_segment, p_locale);
    }


    /**
     * Get state.
     * @return state
     */
    public String getState()
    {
        return m_state;
    }
    

    /**
     * Set state.
     * @param p_state state
     */
    public void setState(String p_state)
    {
        m_state = p_state;
    }
    
    /**
     * Get merge state.
     * @return merge state
     */
    public String getMergeState()
    {
        return m_mergeState;
    }
    

    /**
     * Set merge state.
     * @param p_mergeState merge state
     */
    public void setMergeState(String p_mergeState)
    {
        m_mergeState = p_mergeState;
    }
    
}
