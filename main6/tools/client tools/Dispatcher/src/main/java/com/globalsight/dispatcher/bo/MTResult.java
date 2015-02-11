/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.dispatcher.bo;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Business Object for Machine Translation(MT) Result 
 */
@XmlRootElement(name = "mTResult")
public class MTResult
{
    private String status;                  // MT Status: success/fail
    private String mtName;                  // MT Name 
    private String mtSourceLan;             // MT Source Language
    private String mtTargetLan;             // MT Target Language
    private List<String> mtSourceList;      // MT Source Text List
    private List<String> mtTargetList;      // MT Target Text List
    private String user;                    // MT User Name
    
    public MTResult() {}
    
    public MTResult(String status, String mtName, String mtSourceLan, String mtTargetLan, String operator,
            List<String> mtSourceList, List<String> mtTargetList)
    {
        this.status = status;
        this.mtName = mtName;
        this.mtSourceLan = mtSourceLan;
        this.mtTargetLan = mtTargetLan;        
        this.mtSourceList = mtSourceList;
        this.mtTargetList = mtTargetList;
        this.user = operator;
    }
    
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getMtName()
    {
        return mtName;
    }

    public void setMtName(String mtName)
    {
        this.mtName = mtName;
    }

    public String getMtSourceLan()
    {
        return mtSourceLan;
    }

    public void setMtSourceLan(String mtSourceLan)
    {
        this.mtSourceLan = mtSourceLan;
    }

    public String getMtTargetLan()
    {
        return mtTargetLan;
    }

    public void setMtTargetLan(String mtTargetLan)
    {
        this.mtTargetLan = mtTargetLan;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String p_user)
    {
        this.user = p_user;
    }

    public List<String> getMtSourceList()
    {
        return mtSourceList;
    }

    public void setMtSourceList(List<String> mtSourceList)
    {
        this.mtSourceList = mtSourceList;
    }

    public List<String> getMtTargetList()
    {
        return mtTargetList;
    }

    public void setMtTargetList(List<String> mtTargetList)
    {
        this.mtTargetList = mtTargetList;
    }
    
}
