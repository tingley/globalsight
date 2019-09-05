package com.globalsight.connector.eloqua.models.form;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Select
{
    private List<String> option = new ArrayList<>();

    public List<String> getOption()
    {
        return option;
    }

    public void setOption(List<String> option)
    {
        this.option = option;
    }
}
