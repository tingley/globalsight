package com.globalsight.connector.eloqua.models;

import java.util.ArrayList;
import java.util.List;

public class Alls
{
    private List elements = new ArrayList();
    private int page = 0;
    private int pageSize = 0;
    private int total = 0;
    
    public void addElements(Object e)
    {
        elements.add(e);
    }

    public List getElements()
    {
        return elements;
    }

    public void setElements(List elements)
    {
        this.elements = elements;
    }

    public int getPage()
    {
        return page;
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public int getPageSize()
    {
        return pageSize;
    }

    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

}
