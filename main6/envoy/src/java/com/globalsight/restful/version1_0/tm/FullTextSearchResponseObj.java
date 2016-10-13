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

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

public class FullTextSearchResponseObj implements Serializable
{
    private static final long serialVersionUID = 1L;

    String sourceSegment;
    String targetSegment;
    String sid;
    long tuId;
    String tmName;

    FullTextSearchResponseObj(String sourceSegment, String targetSegment, String sid, long tuId,
            String tmName)
    {
        this.sourceSegment = sourceSegment;
        this.targetSegment = targetSegment;
        this.sid = sid;
        this.tuId = tuId;
        this.tmName = tmName;
    }

    @XmlElement(name = "sourceSegment")
    public String getSourceSegment()
    {
        return sourceSegment;
    }

    public void setSourceSegment(String sourceSegment)
    {
        this.sourceSegment = sourceSegment;
    }

    @XmlElement(name = "targetSegment")
    public String getTargetSegment()
    {
        return targetSegment;
    }

    public void setTargetSegment(String targetSegment)
    {
        this.targetSegment = targetSegment;
    }

    @XmlElement(name = "sid")
    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    @XmlElement(name = "tuId")
    public long getTuId()
    {
        return tuId;
    }

    public void setTuId(long tuId)
    {
        this.tuId = tuId;
    }

    @XmlElement(name = "tmName")
    public String getTmName()
    {
        return tmName;
    }

    public void setTmName(String tmName)
    {
        this.tmName = tmName;
    }

    public String toString()
    {
        return sourceSegment + ":" + targetSegment + ":" + sid + ":" + tuId + ":" + tmName;
    }
}
