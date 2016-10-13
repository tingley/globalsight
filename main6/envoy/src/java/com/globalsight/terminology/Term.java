/**
 *  Copyright 2011 Welocalize, Inc. 
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
package com.globalsight.terminology;

import java.util.Date;

public class Term
{
    private int tbid;
    private int tid;
    private int lid;
    private int cid;
    private String lang_name;
    private String term;
    private String type;
    private String status;
    private String sort_key;
    private String xml;
    private Date created_on;
    private String created_by;
    private Date modified_on;
    private String modified_by;

    public Term()
    {
    }

    public Term(int p_tid, int p_lid, int p_cid)
    {
        tid = p_tid;
        lid = p_lid;
        cid = p_cid;
    }

    public int getTbid()
    {
        return tbid;
    }

    public void setTbid(int tbid)
    {
        this.tbid = tbid;
    }

    public int getTid()
    {
        return tid;
    }

    public void setTid(int tid)
    {
        this.tid = tid;
    }

    public int getLid()
    {
        return lid;
    }

    public void setLid(int lid)
    {
        this.lid = lid;
    }

    public int getCid()
    {
        return cid;
    }

    public void setCid(int cid)
    {
        this.cid = cid;
    }

    public String getLang_name()
    {
        return lang_name;
    }

    public void setLang_name(String langName)
    {
        lang_name = langName;
    }

    public String getTerm()
    {
        return term;
    }

    public void setTerm(String term)
    {
        this.term = term;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getSort_key()
    {
        return sort_key;
    }

    public void setSort_key(String sortKey)
    {
        sort_key = sortKey;
    }

    public String getXml()
    {
        return xml;
    }

    public void setXml(String xml)
    {
        this.xml = xml;
    }

    public Date getCreated_on()
    {
        return created_on;
    }

    public void setCreated_on(Date createdOn)
    {
        created_on = createdOn;
    }

    public String getCreated_by()
    {
        return created_by;
    }

    public void setCreated_by(String createdBy)
    {
        created_by = createdBy;
    }

    public Date getModified_on()
    {
        return modified_on;
    }

    public void setModified_on(Date modifiedOn)
    {
        modified_on = modifiedOn;
    }

    public String getModified_by()
    {
        return modified_by;
    }

    public void setModified_by(String modifiedBy)
    {
        modified_by = modifiedBy;
    }

    public String toString()
    {
        return tbid + "," + tid + "," + lid + "," + cid + "," + lang_name + ","
                + term + ",";
    }

    public boolean isEqual(int p_cid, int p_lid)
    {
        if (cid == p_cid && lid == p_lid)
        {
            return true;
        }
        return false;
    }
}
