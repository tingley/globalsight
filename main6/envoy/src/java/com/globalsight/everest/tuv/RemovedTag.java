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
package com.globalsight.everest.tuv;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.persistence.PersistentObject;

public class RemovedTag extends PersistentObject
{
    private static final long serialVersionUID = 1359657927947785319L;
    private String prefixString;
    private String suffixString;
    private TuImpl tu;
    private long tuId;

    private List<String> orgStrings = new ArrayList<String>();
    private List<String> newStrings = new ArrayList<String>();
    private int tagNum = 0;

    private static String PRESERVE = " xml:space=&quot;preserve&quot;";
    private static String RSIDRPR_REGEX = " w:rsidRPr=&quot;[^&]*&quot;";
    private static String RSIDR_REGEX = " w:rsidR=&quot;[^&]*&quot;";

    public String getPrefixString()
    {
        return prefixString;
    }

    public void setPrefixString(String prefixString)
    {
        this.prefixString = prefixString;
    }

    public String getSuffixString()
    {
        return suffixString;
    }

    public void setSuffixString(String suffixString)
    {
        this.suffixString = suffixString;
    }

    public long getTuId()
    {
        return this.tuId;
    }

    public void setTuId(long tuId)
    {
        this.tuId = tuId;
    }

    // Utility methods
    public void setTu(TuImpl p_tu)
    {
        tuId = p_tu.getTuId();
        tu = p_tu;
        if (tu != null)
        {
            tuId = tu.getId();
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((prefixString == null) ? 0 : prefixString.hashCode());
        result = prime * result
                + ((suffixString == null) ? 0 : suffixString.hashCode());
        result = prime * result + ((tu == null) ? 0 : tu.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RemovedTag other = (RemovedTag) obj;
        if (prefixString == null)
        {
            if (other.prefixString != null)
                return false;
        }
        else if (!prefixString.equals(other.prefixString))
            return false;
        if (suffixString == null)
        {
            if (other.suffixString != null)
                return false;
        }
        else if (!suffixString.equals(other.suffixString))
            return false;
        if (tu == null)
        {
            if (other.tu != null)
                return false;
        }
        else if (!tu.equals(other.tu))
            return false;
        return true;
    }

    public int getTagNum()
    {
        return tagNum;
    }

    public void setTagNum(int tagNum)
    {
        this.tagNum = tagNum;
    }

    public List<String> getOrgStrings()
    {
        return orgStrings;
    }

    public void setOrgStrings(List<String> orgStrings)
    {
        this.orgStrings = orgStrings;
    }

    public List<String> getNewStrings()
    {
        return newStrings;
    }

    public void addOrgString(String s)
    {
        this.orgStrings.add(s);
    }

    public void addNewString(String s)
    {
        this.newStrings.add(s);
    }

    public void mergeString(RemovedTag tag)
    {
        this.orgStrings.addAll(tag.getOrgStrings());
        this.newStrings.addAll(tag.getNewStrings());
    }

    public void setNewStrings(List<String> newStrings)
    {
        this.newStrings = newStrings;
    }

    public boolean sameAs(RemovedTag tag)
    {
        if (getSuffixString().equals(tag.getSuffixString()))
        {
            String preS = getPrefixString();
            String preS2 = tag.getPrefixString();
            if (preS.equals(preS2))
            {
                return true;
            }

            preS = preS.replace(PRESERVE, "");
            preS2 = preS2.replace(PRESERVE, "");

            preS = preS.replaceAll(RSIDRPR_REGEX, "");
            preS2 = preS2.replaceAll(RSIDRPR_REGEX, "");

            preS = preS.replaceAll(RSIDR_REGEX, "");
            preS2 = preS2.replaceAll(RSIDR_REGEX, "");
            if (preS.equals(preS2))
            {
                return true;
            }
        }

        return false;
    }
}
