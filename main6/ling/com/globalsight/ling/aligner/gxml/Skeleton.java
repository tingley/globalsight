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
package com.globalsight.ling.aligner.gxml;

import com.globalsight.ling.tm2.BaseTmTuv;

import java.util.List;
import java.util.ArrayList;

/**
 * Skeleton represents skeleton element in GXML.
 */

public class Skeleton
{
    private Integer m_id;
    private String m_skeletonString;


    public Skeleton(int p_id, String p_skeletonString)
    {
        m_id = new Integer(p_id);
        m_skeletonString = p_skeletonString;
    }
    

    public Integer getId()
    {
        return m_id;
    }
    

    public String getSkeletonString()
    {
        return m_skeletonString;
    }

}
