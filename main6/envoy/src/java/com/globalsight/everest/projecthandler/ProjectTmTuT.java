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

package com.globalsight.everest.projecthandler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.tm.exporter.ExportUtil;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.util.GlobalSightLocale;

public class ProjectTmTuT extends PersistentObject
{
    private static final long serialVersionUID = -4634594633148769874L;

    private String format;
    private String type;
    private String sourceTmName;
    private GlobalSightLocale sourceLocale;
    private ProjectTM projectTm;
    private Set<ProjectTmTuvT> tuvs;
    private Set<ProjectTmTuTProp> props;
    private String sid;
    
    // For GBS-676. There are some difference between globalsight and worldserver
    // on Placeholders.
    // For example:
    // Source Segment: This is <b>bold</b>.
    // ambassador: <seg>This is <bpt type="bold" i="2"
    // x="1">&lt;B&gt;</bpt>bold<ept i="2">&lt;/B&gt;</ept>. </seg>
    // worldserver: <seg>This is <ph x="1">{1}</ph>bold<ph x="2">{2}</ph>.
    // </seg>
    private boolean fromWorldServer = false;

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getSourceTmName()
    {
        return sourceTmName;
    }

    public void setSourceTmName(String sourceTmName)
    {
        this.sourceTmName = sourceTmName;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return sourceLocale;
    }

    public void setSourceLocale(GlobalSightLocale sourceLocale)
    {
        this.sourceLocale = sourceLocale;
    }

    public ProjectTM getProjectTm()
    {
        return projectTm;
    }

    public void setProjectTm(ProjectTM projectTm)
    {
        this.projectTm = projectTm;
    }

    public Set<ProjectTmTuvT> getTuvs()
    {
        return tuvs;
    }

    public void addTuv(ProjectTmTuvT tuv)
    {
        if (tuvs == null)
        {
            tuvs = new HashSet<ProjectTmTuvT>();
        }

        tuvs.add(tuv);
    }

    public void removeTuv(ProjectTmTuvT tuv)
    {
        if (tuvs != null)
        {
            tuvs.remove(tuv);
        }
    }

    public void setTuvs(Set<ProjectTmTuvT> tuvs)
    {
        this.tuvs = tuvs;
    }
    
    public Set<ProjectTmTuTProp> getProps()
    {
        return props;
    }

    public void addProp(ProjectTmTuTProp prop)
    {
        if (props == null)
        {
            props = new HashSet<ProjectTmTuTProp>();
        }

        props.add(prop);
    }

    public void removeProp(ProjectTmTuTProp prop)
    {
        if (props != null)
        {
            props.remove(prop);
        }
    }

    public void setProps(Set<ProjectTmTuTProp> props)
    {
        this.props = props;
    }

    public String convertToTmx()
    {
        return convertToTmx(null);
    }

    /**
     * Converts a GlobalSight TU/TUV group to a TMX TU. Differences: - TU
     * segment type (text, string, css-*) is output as prop. - TU type (T or L)
     * is output as prop.
     * 
     */
    public String convertToTmx(List<GlobalSightLocale> targetLocales)
    {
        StringBuffer result = new StringBuffer();

        Tmx.Prop prop;
        GlobalSightLocale srcLocale = getSourceLocale();
        String srcLang = ExportUtil.getLocaleString(srcLocale);

        result.append("<tu");

        // Remember valid TU IDs
        if (getId() > 0)
        {
            result.append(" ");
            result.append(Tmx.TUID);
            result.append("=\"");
            result.append(getId());
            result.append("\"");
        }

        // Default datatype is HTML, mark different TUs.
        if (!"html".equalsIgnoreCase(getFormat()))
        {
            result.append(" ");
            result.append(Tmx.DATATYPE);
            result.append("=\"");
            result.append(getFormat());
            result.append("\"");
        }

        // Default srclang is en_US, mark different TUs.
        if (!"en_US".equalsIgnoreCase(this.getSourceLocale().toString()))
        {
            result.append(" ");
            result.append(Tmx.SRCLANG);
            result.append("=\"");
            result.append(srcLang);
            result.append("\"");
        }

        result.append(">\r\n");

        // Property for TU type (text, string), default "text"
        if (!"text".equalsIgnoreCase(getType()))
        {
            prop = new Tmx.Prop(Tmx.PROP_SEGMENTTYPE, getType());
            result.append(prop.asXML());
        }

        // Property for TU's source TM name.
        String temp = getSourceTmName();
        if (temp != null && temp.length() > 0)
        {
            prop = new Tmx.Prop(Tmx.PROP_SOURCE_TM_NAME, temp);
            result.append(prop.asXML());
        }
        
        // attribute properties
        if (props != null)
        {
            for (ProjectTmTuTProp pr : props)
            {
                result.append(pr.convertToTmx());
            }
        }

        ProjectTmTuvT sTuv = getSourceTuv();
        if (sTuv == null)
        {
            throw new IllegalStateException("Can not find source tuv. "
                    + "The tu id is: " + this.getId());
        }

        if (sTuv.getSid() != null)
        {
            prop = new Tmx.Prop(Tmx.PROP_TM_UDA_SID, sTuv.getSid());
            result.append(prop.asXML());
        }

        result.append(sTuv.convertToTmx());

        for (ProjectTmTuvT tuv : getTuvs())
        {
            if (!getSourceLocale().equals(tuv.getLocale()))
            {
                if (targetLocales == null
                        || targetLocales.contains(tuv.getLocale()))
                {
                    result.append(tuv.convertToTmx());
                }
            }
        }

        result.append("</tu>\r\n");

        return result.toString();
    }

    public ProjectTmTuvT getSourceTuv()
    {
        for (ProjectTmTuvT tuv : getTuvs())
        {
            if (getSourceLocale().equals(tuv.getLocale()))
            {
                return tuv;
            }
        }

        return null;
    }

    public String getSid()
    {
        return sid;
    }

    public void setSid(String sid)
    {
        this.sid = sid;
    }

    /**
     * Returns true if the tu is imported from worldserver and havn't been repaired.
     * @return
     */
    public boolean isFromWorldServer()
    {
        return fromWorldServer;
    }

    /**
     * Sets the value of fromWorldServer
     * @param fromWorldServer
     */
    public void setFromWorldServer(boolean fromWorldServer)
    {
        this.fromWorldServer = fromWorldServer;
    }
}
