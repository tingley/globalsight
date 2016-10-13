package com.globalsight.cxe.entity.filterconfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.globalsight.ling.docproc.extractor.javaprop.JPTmxEncoder;

@XmlRootElement
public class PropertiesInternalText
{
    static private final Logger s_logger = Logger
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
        
        return handleString (s, index);
    }
    
    private String handleString(String s, List<Integer> index)
    {
        if (s.length() == 0)
            return s;
        
        String first = null;
        String internalText = null;
        String end = null;
        
        for (InternalItem item : items)
        {
            if (!item.getIsSelected())
                continue;
            
            if (!item.getIsRegex())
            {
                JPTmxEncoder tmx = new JPTmxEncoder();
                internalText = tmx.encode(item.getContent());
                int i = s.indexOf(internalText);
                if (i > -1)
                {
                    first = s.substring(0, i);
                    end = s.substring(i + internalText.length());
                    break;
                }
            }
            else
            {
                try
                {
                    Pattern p = Pattern.compile(item.getContent());
                    Matcher m = p.matcher(s);
                    if (m.find())
                    {
                        internalText = m.group();
                        int i = s.indexOf(internalText);
                        first = s.substring(0, i);
                        end = s.substring(i + internalText.length());
                        break;
                    }
                }
                catch (Exception e)
                {
                    s_logger.error(e.getMessage(), e);
                }
            }
        }
        
        if (first != null)
        {
            int n = index.remove(0);
            index.add(n + 1);
            s = handleString(first, index) + "<bpt internal=\"yes\" i=\"" + n
                    + "\"></bpt>" + internalText + "<ept i=\"" + n
                    + "\"></ept>" + handleString(end, index);
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
            s_logger.error(e.getMessage(), e);
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
