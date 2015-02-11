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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Accounts")
public class Accounts
{
    private AtomicInteger autoIncrement = new AtomicInteger(0);
    private Set<Account> accounts = new CopyOnWriteArraySet<Account>();
    
    public Accounts()
    {
        super();
    }

    public Set<Account> getAccounts()
    {
        return accounts;
    }

    @XmlElement(name = "Account")
    public void setAccounts(Set<Account> p_accounts)
    {
        this.accounts = p_accounts;
    }

    public int getAutoIncrement()
    {
        return autoIncrement.get();
    }

    @XmlAttribute(name="autoIncrement")
    public void setAutoIncrement(int newValue)
    {
        autoIncrement.set(newValue);
    }
    
    public int getAndIncrement()
    {
        return autoIncrement.getAndIncrement();
    }
}
