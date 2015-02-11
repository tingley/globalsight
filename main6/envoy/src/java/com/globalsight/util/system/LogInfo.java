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
