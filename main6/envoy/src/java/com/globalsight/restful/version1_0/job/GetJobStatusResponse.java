package com.globalsight.restful.version1_0.job;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "job")
public class GetJobStatusResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    long id;
    String name;
    String status;

    @XmlElement(name = "id")
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @XmlElement(name = "name")
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @XmlElement(name = "status")
    public String getStatus()
    {
        return status;
    }

    public void setstatus(String status)
    {
        this.status = status;
    }
}
