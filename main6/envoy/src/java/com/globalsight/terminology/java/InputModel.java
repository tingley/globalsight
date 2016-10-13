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

package com.globalsight.terminology.java;

import java.io.Serializable;

import com.globalsight.terminology.java.Termbase;

public class InputModel implements Serializable
{
    private static final long serialVersionUID = 4353251018177483392L;
    
    private long id;
    private String name;
    private String userName;
    private Termbase termbase;
    private int type;
    private String isDefault;
    private String value;
    
    public long getId()
    {
        return id;
    }
    
    public void setId(long p_id)
    {
        id = p_id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String p_name) {
        name = p_name;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String p_name) {
        userName = p_name;
    }
    
    public Termbase getTermbase() {
        return termbase;
    }
    
    public void setTermbase(Termbase p_termbase) {
        termbase = p_termbase;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int p_type) {
        type = p_type;
    }
    
    public String getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(String p_isDefault) {
        isDefault = p_isDefault;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String p_value) {
        value = p_value;
    }

}
