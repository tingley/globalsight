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

package com.globalsight.everest.foundation;

public interface UserRole extends java.io.Serializable
{
    public static final String ROLE_TYPE_VALUE = "U";

    public String getUser();

    public void setUser(String p_user);

    public String getUserName();

    public void setUserName(String p_userName);

    public String getCost();

    public void setCost(String p_cost);

    public String getRate();

    public void setRate(String p_rate);

}