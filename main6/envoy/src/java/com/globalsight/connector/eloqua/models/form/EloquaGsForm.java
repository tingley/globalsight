package com.globalsight.connector.eloqua.models.form;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EloquaGsForm
{
    private String id;
    private List<String> div = new ArrayList<>();
    private List<Select> select = new ArrayList<>();
    private String newId = "-1";

    @XmlAttribute
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
    
    public List<String> getDiv()
    {
        return div;
    }

    public void setDiv(List<String> div)
    {
        this.div = div;
    }

    public List<Select> getSelect()
    {
        return select;
    }

    public void setSelect(List<Select> select)
    {
        this.select = select;
    }

    @XmlAttribute
    public String getNewId()
    {
        return newId;
    }

    public void setNewId(String newId)
    {
        this.newId = newId;
    }
}
