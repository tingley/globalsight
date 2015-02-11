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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Attributes
{
    private List<ListJobAttributeVo> listAttributes;
    private List<DateJobAttributeVo> dateAttributes;
    private List<FileJobAttributeVo> fileAttributes;
    private List<TextJobAttributeVo> textAttributes;
    private List<IntJobAttributeVo> intAttributes;
    private List<FloatJobAttributeVo> floatAttributes;
    
    private List attributes = new ArrayList();;

    public List getAttributes()
    {
        return attributes;
    }
    
    public void addAttribute(JobAttributeVo vo)
    {
        attributes.add(vo);
    }

    public void setAttributes(List attributes)
    {
        this.attributes = attributes;
    }

    public List<ListJobAttributeVo> getListAttributes()
    {
        return listAttributes;
    }

    public void setListAttributes(List<ListJobAttributeVo> listAttributes)
    {
        this.listAttributes = listAttributes;
    }

    public List<DateJobAttributeVo> getDateAttributes()
    {
        return dateAttributes;
    }

    public void setDateAttributes(List<DateJobAttributeVo> dateAttributes)
    {
        this.dateAttributes = dateAttributes;
    }

    public List<FileJobAttributeVo> getFileAttributes()
    {
        return fileAttributes;
    }

    public void setFileAttributes(List<FileJobAttributeVo> fileAttributes)
    {
        this.fileAttributes = fileAttributes;
    }

    public List<TextJobAttributeVo> getTextAttributes()
    {
        return textAttributes;
    }

    public void setTextAttributes(List<TextJobAttributeVo> textAttributes)
    {
        this.textAttributes = textAttributes;
    }

    public List<IntJobAttributeVo> getIntAttributes()
    {
        return intAttributes;
    }

    public void setIntAttributes(List<IntJobAttributeVo> intAttributes)
    {
        this.intAttributes = intAttributes;
    }

    public List<FloatJobAttributeVo> getFloatAttributes()
    {
        return floatAttributes;
    }

    public void setFloatAttributes(List<FloatJobAttributeVo> floatAttributes)
    {
        this.floatAttributes = floatAttributes;
    }
}
