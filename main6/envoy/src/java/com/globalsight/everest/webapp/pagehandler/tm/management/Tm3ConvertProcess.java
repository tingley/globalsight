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

package com.globalsight.everest.webapp.pagehandler.tm.management;

/**
 * @author Vincent
 *
 */
public class Tm3ConvertProcess
{
    private static Tm3ConvertProcess convertProcess = null;
    
    private long tm2Id = -1l;
    private long tm3Id = -1l;
    private String tm2Name = "";
    private String tm3Name = "";
    private long lastTUId = -1l;
    private int convertedRate = 0;
    private String status = "";
    private Tm3ConvertHelper convertHelper = null;
    
    private Tm3ConvertProcess()
    {
    }
    
    public static Tm3ConvertProcess getInstance() {
        if (convertProcess == null)
            convertProcess =  new Tm3ConvertProcess();
        
        return convertProcess;
    }

    public long getTm2Id()
    {
        return tm2Id;
    }

    public void setTm2Id(long tm2Id)
    {
        this.tm2Id = tm2Id;
    }

    public long getTm3Id()
    {
        return tm3Id;
    }

    public void setTm3Id(long tm3Id)
    {
        this.tm3Id = tm3Id;
    }

    public String getTm2Name()
    {
        return tm2Name;
    }

    public void setTm2Name(String tm2Name)
    {
        this.tm2Name = tm2Name;
    }

    public String getTm3Name()
    {
        return tm3Name;
    }

    public void setTm3Name(String tm3Name)
    {
        this.tm3Name = tm3Name;
    }

    public long getLastTUId()
    {
        return lastTUId;
    }

    public void setLastTUId(long lastTUId)
    {
        this.lastTUId = lastTUId;
    }

    public int getConvertedRate()
    {
        return convertedRate;
    }

    public void setConvertedRate(int convertedRate)
    {
        this.convertedRate = convertedRate;
    }

    public Tm3ConvertHelper getConvertHelper()
    {
        return convertHelper;
    }

    public void setConvertHelper(Tm3ConvertHelper convertHelper)
    {
        this.convertHelper = convertHelper;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    
}
