/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.globalsight.restful.version1_0.tm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "segments")
public class FullTextSearchResponse
{
    String sourceLocale;

    String targetLocale;

    List<FullTextSearchResponseObj> segmentList = new ArrayList<FullTextSearchResponseObj>();

    @XmlElement(name = "sourceLocale")
    public String getSourceLocale()
    {
        return sourceLocale;
    }

    public void setSourceLocale(String sourceLocale)
    {
        this.sourceLocale = sourceLocale;
    }

    @XmlElement(name = "targetLocale")
    public String getTargetLocale()
    {
        return targetLocale;
    }

    public void setTargetLocale(String targetLocale)
    {
        this.targetLocale = targetLocale;
    }

    @XmlElement(name = "segment")
    public List<FullTextSearchResponseObj> getSegmentList()
    {
        return segmentList;
    }

    public void setSegmentList(List<FullTextSearchResponseObj> segmentList)
    {
        this.segmentList = segmentList;
    }

    public void addSegment(String sourceSegment, String targetSegment, String sid, long tuId,
            String tmName)
    {
        FullTextSearchResponseObj segment = new FullTextSearchResponseObj(sourceSegment,
                targetSegment, sid, tuId, tmName);
        this.segmentList.add(segment);
    }
}
