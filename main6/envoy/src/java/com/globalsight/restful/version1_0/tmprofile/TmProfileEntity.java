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

package com.globalsight.restful.version1_0.tmprofile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlList;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "TMProfile")
public class TmProfileEntity implements Serializable
{
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "id")
    long id;

    @XmlElement(name = "name")
    String name;

    @XmlElement(name = "description")
    String description;

    @XmlElement(name= "storageTMName")
    String storageTMName;

    @XmlList
    List<ReferenceTM> referenceTMGrp = new ArrayList<ReferenceTM>();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getStorageTMName()
    {
        return storageTMName;
    }

    public void setStorageTMName(String storageTMName)
    {
        this.storageTMName = storageTMName;
    }

    public List<ReferenceTM> getReferenceTMGrp()
    {
        return referenceTMGrp;
    }

    public void setReferenceTMGrp(List<ReferenceTM> referenceTMGrp)
    {
        this.referenceTMGrp = referenceTMGrp;
    }

    public void addReferenceTM(long id, String refTmName)
    {
        ReferenceTM tm = new ReferenceTM();
        tm.setId(id);
        tm.setReferenceTM(refTmName);
        referenceTMGrp.add(tm);
    }

    private class ReferenceTM implements Serializable
    {
        private static final long serialVersionUID = 1L;

        @XmlAttribute(name = "id")
        long id;

        @XmlElement(name = "referenceTM")
        String referenceTM;

        public long getId()
        {
            return id;
        }

        public void setId(long id)
        {
            this.id = id;
        }

        public String getReferenceTM()
        {
            return referenceTM;
        }

        public void setReferenceTM(String referenceTM)
        {
            this.referenceTM = referenceTM;
        }
    }
}
