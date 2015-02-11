/**
 *  Copyright 2014 Welocalize, Inc. 
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

import javax.xml.bind.annotation.XmlAttribute;

//@XmlType(propOrder = { "id", "accountName", "securityCode", "description"})
public class Account
{
    private long id;
    private String accountName;
    private String description;
    private String securityCode;
    
    public Account()
    {
        this(-1);
    }

    public Account(long id)
    {
        this.id = id;
    }

    public Account(long id, String p_accountName,
            String p_description, String p_securityCode)
    {
        this.id = id;
        this.accountName = p_accountName;
        this.description = p_description;
        this.securityCode = p_securityCode;
    }
    
    public long getId()
    {
        return this.id;
    }

    @XmlAttribute(name="ID")
    public void setId(long id)
    {
        this.id = id;
    }

    public String getAccountName()
    {
        return accountName;
    }

    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getSecurityCode()
    {
        return securityCode;
    }

    public void setSecurityCode(String securityCode)
    {
        this.securityCode = securityCode;
    }
}
