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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.XmlWriter;

public class TranslatableElement
    extends Segmentable
    implements DocumentElement
{
    public void addSegment(SegmentNode p_segment)
    {
        if (segments == null)
        {
            segments = new ArrayList();
        }

        segments.add(p_segment);
    }

    public ArrayList getSegments()
    {
        return segments;
    }

    public boolean hasSegments()
    {
        return (segments == null) ? false : true;
    }

    public void toDiplomatString(DiplomatAttribute diplomatAttribute,
        XmlWriter writer)
    {
        toDiplomatString(diplomatAttribute, writer,
            DiplomatNames.Element.TRANSLATABLE);
    }

    public int type()
    {
        return TRANSLATABLE;
    }

    /** Print routine for GS-tagged source pages. */
    public String getText()
    {
        String chunk = getChunk();

        if (chunk != null)
        {
            return chunk;
        }

        StringBuffer result = new StringBuffer();

        for (int i = 0, max = segments.size(); i < max; i++)
        {
            SegmentNode node = (SegmentNode)segments.get(i);

            result.append(node.getSegment());
        }

        return result.toString();
    }
}
