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

public class Language
{
    private int tbid;
    private int lid;
    private int cid;
    private String name;
    private String locale;
    private String xml;

    public Language()
    {
    }

    public Language(int lid, int cid, String name, String locale)
    {
        this.lid = lid;
        this.cid = cid;
        this.name = name;
        this.locale = locale;
    }

    public int getTbid()
    {
        return tbid;
    }

    public void setTbid(int tbid)
    {
        this.tbid = tbid;
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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getLocale()
    {
        return locale;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    public String getXml()
    {
        return xml;
    }

    public void setXml(String xml)
    {
        this.xml = xml;
    }

    public boolean isEqual(int p_cid, String p_name, String p_locale)
    {
        if (cid == p_cid && name != null && name.equals(p_name)
                && locale != null && locale.equals(p_locale))
        {
            return true;
        }
        return false;
    }
}
