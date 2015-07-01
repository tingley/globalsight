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
package com.globalsight.everest.webapp.pagehandler.login;

import java.util.Date;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Login Attempt object.
 */
public class LoginAttempt extends PersistentObject
{
    private static final long serialVersionUID = 8591662988007152637L;
    private String ip = "";
    private int count = 0;
    private Date blockTime;
    
    public LoginAttempt()
    {
        super();
    }

    public LoginAttempt(String ip)
    {
        super();
        this.ip = ip;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public Date getBlockTime()
    {
        return blockTime;
    }

    public void setBlockTime(Date blockTime)
    {
        this.blockTime = blockTime;
    }

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }
}
