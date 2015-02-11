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
package com.globalsight.util.system;

import java.io.Serializable;
import java.sql.Timestamp;

public class LogInfo implements Serializable
{
    private static final long serialVersionUID = 3402557834253910169L;
    
    private long id;
    private String eventType;
    private String objectType;
    private String objectId;
    private String operator;
    private Timestamp operateTime;
    private String message;
    private long companyId;

    public LogInfo()
    {
    }

    public LogInfo(String objectType, String eventType, String objectId,
            String operator, Timestamp operateTime, String message,
            long companyId)
    {
        this.objectType = objectType;
        this.eventType = eventType;
        this.objectId = objectId;
        this.operator = operator;
        this.operateTime = operateTime;
        this.message = message;
        this.companyId = companyId;
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getObjectType()
    {
        return objectType;
    }

    public void setObjectType(String objectType)
    {
        this.objectType = objectType;
    }

    public String getObjectId()
    {
        return objectId;
    }

    public void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public long getCompanyId()
    {
        return companyId;
    }

    public void setCompanyId(long companyId)
    {
        this.companyId = companyId;
    }

    public String getOperator()
    {
        return operator;
    }

    public void setOperator(String operator)
    {
        this.operator = operator;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Timestamp getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Timestamp operateTime)
    {
        this.operateTime = operateTime;
    }

}
