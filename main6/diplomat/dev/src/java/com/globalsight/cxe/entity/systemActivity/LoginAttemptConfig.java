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

package com.globalsight.cxe.entity.systemActivity;

import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.persistence.PersistentObject;

public class LoginAttemptConfig extends PersistentObject
{
    private static final long serialVersionUID = -8642737920702289919L;

    private boolean enable = false;
    private int maxTime = 10;

    // minutes
    private long blockTime = 60;

    private String examptIps = "";

    public boolean isEnable()
    {
        return enable;
    }

    public void setEnable(boolean enable)
    {
        this.enable = enable;
    }

    public int getMaxTime()
    {
        return maxTime;
    }

    public void setMaxTime(int maxTime)
    {
        this.maxTime = maxTime;
    }

    public long getBlockTime()
    {
        return blockTime;
    }

    public void setBlockTime(long blockTime)
    {
        this.blockTime = blockTime;
    }

    public String getExamptIps()
    {
        return examptIps;
    }
    
    public List<String> getExamptIpAsList()
    {
        List<String> ips = new ArrayList<String>();
        if (examptIps != null && examptIps.length() > 0)
        {
            String[] splitIps = examptIps.split(",");
            for (String ip :splitIps)
            {
                ips.add(ip.trim());
            }
        }
        
        return ips;
    }
    
    public void SetExamptIpsAsList(List<String> ips)
    {
        if (ips == null || ips.size() == 0)
        {
            examptIps = "";
            return;
        }
            
        examptIps = ips.toString();
        examptIps = examptIps.substring(1, examptIps.length() - 1);
    }

    public void setExamptIps(String examptIps)
    {
        this.examptIps = examptIps;
    }
}
