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
package com.globalsight.ling.tm3.integration.segmenttm;

import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FORMAT;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.FROM_WORLDSERVER;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.SID;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TRANSLATABLE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.TYPE;
import static com.globalsight.ling.tm3.integration.segmenttm.SegmentTmAttribute.UPDATED_BY_PROJECT;

import java.util.Iterator;
import java.util.Map;

import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.SegmentResultSet;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.leverage.LeveragedSegmentTuv;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.core.TM3Tuv;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.util.GlobalSightLocale;

class Tm3SegmentResultSet implements SegmentResultSet
{
    private Tm tm;
    private TM3Tm<GSTuvData> tm3tm;
    private Iterator<TM3Tu<GSTuvData>> inner;

    private TM3Attribute typeAttr;
    private TM3Attribute formatAttr;
    private TM3Attribute sidAttr;
    private TM3Attribute translatableAttr;
    private TM3Attribute fromWsAttr;
    private TM3Attribute projectAttr;

    Tm3SegmentResultSet(Tm tm, TM3Tm<GSTuvData> tm3tm,
            Iterator<TM3Tu<GSTuvData>> inner)
    {
        this.tm = tm;
        this.tm3tm = tm3tm;
        this.inner = inner;

        // Load attributes
        typeAttr = TM3Util.getAttr(tm3tm, TYPE);
        formatAttr = TM3Util.getAttr(tm3tm, FORMAT);
        sidAttr = TM3Util.getAttr(tm3tm, SID);
        translatableAttr = TM3Util.getAttr(tm3tm, TRANSLATABLE);
        fromWsAttr = TM3Util.getAttr(tm3tm, FROM_WORLDSERVER);
        projectAttr = TM3Util.getAttr(tm3tm, UPDATED_BY_PROJECT);
    }

    @Override
    public void finish()
    {
    }

    @Override
    public boolean hasNext()
    {
        return inner.hasNext();
    }

    @Override
    public SegmentTmTu next()
    {
        if (!inner.hasNext())
        {
            return null;
        }
        return convertTu(inner.next());
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private SegmentTmTu convertTu(TM3Tu<GSTuvData> tm3tu)
    {
        SegmentTmTu tu = new SegmentTmTu(tm3tu.getId(), tm.getId(),
                (String) tm3tu.getAttribute(formatAttr),
                (String) tm3tu.getAttribute(typeAttr), true,
                (GlobalSightLocale) tm3tu.getSourceTuv().getLocale());
        String sid = (String) tm3tu.getAttribute(sidAttr);
        tu.setSID(sid);
        tu.setFromWorldServer((Boolean) tm3tu.getAttribute(fromWsAttr));
        if ((Boolean) tm3tu.getAttribute(translatableAttr))
        {
            tu.setTranslatable();
        }
        else
        {
            tu.setLocalizable();
        }

        // convert TM3 attributes to TU properties
        Map<TM3Attribute, Object> tmAts = tm3tu.getAttributes();
        if (tmAts != null && !tmAts.isEmpty())
        {
            for (Map.Entry<TM3Attribute, Object> tmAt : tmAts.entrySet())
            {
                ProjectTmTuTProp prop = TM3Util.toProjectTmTuTProp(
                        tmAt.getKey(), tmAt.getValue());

                if (prop != null)
                {
                    tu.addProp(prop);
                }
            }
        }

        // XXX: Is it possible to cleanly refactor with TM3Leverager?
        for (TM3Tuv<GSTuvData> tuv : tm3tu.getAllTuv())
        {
            SegmentTmTuv stuv = new LeveragedSegmentTuv(tuv.getId(), tuv
                    .getContent().getData(),
                    (GlobalSightLocale) tuv.getLocale());
            stuv.setTu(tu);
            stuv.setSid(sid);

            TM3Event latestEvent = tuv.getLatestEvent();
            TM3Event firstEvent = tuv.getFirstEvent();
            stuv.setModifyDate(TM3Util.toTimestamp(latestEvent));
            stuv.setModifyUser(latestEvent.getUsername());
            stuv.setCreationDate(TM3Util.toTimestamp(firstEvent));
            stuv.setCreationUser(firstEvent.getUsername());
            stuv.setUpdatedProject((String) tm3tu.getAttribute(projectAttr));

            // I'm leaving some fields blank, like
            // - setExactMatchKey
            // - setOrgSegment
            // TODO: figure out which of these are important
            tu.addTuv(stuv);
        }
        return tu;
    }
}
