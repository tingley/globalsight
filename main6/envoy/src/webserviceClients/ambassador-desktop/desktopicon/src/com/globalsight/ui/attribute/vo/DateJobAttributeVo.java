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

package com.globalsight.ui.attribute.vo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.globalsight.util.Assert;

@XmlRootElement
public class DateJobAttributeVo extends JobAttributeVo
{
    private String format = "MM/dd/yyyy HH:mm:ss";
    private Date value;

    public Date getValue()
    {
        return value;
    }

    public void setValue(Date value)
    {
        this.value = value;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }
    
    public String getLabel()
    {
        if (value == null)
            return "";
        
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(value);
    }
    
    public Date convertedToDate(String s)
    {
        if (s == null)
            return null;

        s = s.trim();
        if (s.length() == 0)
            return null;

        Assert.assertIsDate(s, format);
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            return sdf.parse(s);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean isSetted()
    {
        return value != null;
    }
}
