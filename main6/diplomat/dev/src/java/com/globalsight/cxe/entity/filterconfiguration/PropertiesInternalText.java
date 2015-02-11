package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.log.GlobalSightCategory;

@XmlRootElement
public class PropertiesInternalText
{
    static private final GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
    .getLogger(JavaPropertiesFilter.class);
    
    private List<Integer> index = new ArrayList<Integer>();
    private List<InternalItem> items = new ArrayList<InternalItem>();

    public static PropertiesInternalText load(String xml)
    {
        if (xml.length() > 0)
        {
            return com.globalsight.cxe.util.XmlUtil.string2Object(
                    PropertiesInternalText.class, xml);
        }

        return null;
    }
    
    public String handleString(String s)
    {
        index.clear();
        index.add(1);
        for (InternalItem item : items)
        {
            s = item.handleString(s, index);
        }
        
        return s;
    }
    
    public String toJson()
    {
        return null;
    }
    
    public void add(JSONObject ob)
    {
        InternalItem item = new InternalItem();
        try
        {
            if (ob.isNull("content"))
            {
                return;
            }
            
            if (!ob.isNull("isSelected") && ob.getBoolean("isSelected"))
            {
                item.setIsSelected(true);
            }
            
            String content = "" + ob.get("content");
            item.setContent(content);
            item.setIsRegex(ob.getBoolean("isRegex"));
            items.add(item);
        }
        catch (JSONException e)
        {
            s_logger.error(e);
        }
        

    }

    public void addRegex(String regex)
    {
        InternalItem item = new InternalItem();
        item.setContent(regex);
        item.setIsRegex(true);
        items.add(item);
    }

    public void addText(String text)
    {
        InternalItem item = new InternalItem();
        item.setContent(text);
        item.setIsRegex(false);
        items.add(item);
    }

    @XmlElement
    public List<InternalItem> getItems()
    {
        return items;
    }

    public void setItems(List<InternalItem> items)
    {
        this.items = items;
    }
}
