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
package com.globalsight.ling.docproc;

import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.XmlWriter;


public class SkeletonElement
    implements DocumentElement
{
    private StringBuffer m_skeleton = null;

    public void toDiplomatString(DiplomatAttribute diplomatAttribute,
      XmlWriter writer)
    {
        writer.element(DiplomatNames.Element.SKELETON,
          null, m_skeleton.toString(), false);
    }

    public int type()
    {
        return SKELETON;
    }

    public void appendSkeleton(String skeleton)
    {
        m_skeleton.append(skeleton);
    }

    public void setSkeleton(String skeleton)
    {
        m_skeleton = new StringBuffer(skeleton);
    }

    public String getSkeleton()
    {
        return m_skeleton.toString();
    }

    /** Print routine for GS-tagged source pages. */
    public String getText()
    {
        return getSkeleton();
    }
}
