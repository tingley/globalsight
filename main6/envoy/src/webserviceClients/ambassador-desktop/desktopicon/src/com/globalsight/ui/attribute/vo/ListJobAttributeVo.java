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

//import com.globalsight.cxe.entity.customAttribute.SelectOption;

@XmlRootElement
public class ListJobAttributeVo extends JobAttributeVo
{
    private boolean multiple;
    private List<String> options;
    private List<String> selectedOptions;

    public List<String> getOptions()
    {
        return options;
    }

    public void setOptions(List<String> options)
    {
        this.options = options;
    }
    
    public void addOptions(String option)
    {
        if (options == null)
        {
            options = new ArrayList<String>();
        }
        
        options.add(option);
    }

    public List<String> getSelectedOptions()
    {
        return selectedOptions;
    }

    public void setSelectedOptions(List<String> selectedOptions)
    {
        this.selectedOptions = selectedOptions;
    }
    
    public void setSelectedOption(String selectedOption)
    {
        selectedOptions = new ArrayList<String>();
        if (selectedOption != null)
        {
            selectedOptions.add(selectedOption);
        }
    }
    
    public void addSelectedOption(String option)
    {
        if (selectedOptions == null)
        {
            selectedOptions = new ArrayList<String>();
        }
        
        selectedOptions.add(option);
    }

    public boolean isMultiple()
    {
        return multiple;
    }

    public void setMultiple(boolean multiple)
    {
        this.multiple = multiple;
    }
    
    public void setSelectedOptions(int[] indexs)
    {
        selectedOptions = new ArrayList<String>();
        for (int i : indexs)
        {
            selectedOptions.add(options.get(i));
        }
    }
    
    public String getLabel()
    {
        List<String> options = getSelectedOptions();
        StringBuffer label = new StringBuffer();
        if (options != null)
        {
            for (String value : options)
            {
                if (label.length() > 0)
                {
                    label.append(", ");
                }
                
                label.append(value);
            }
        }
        
        return label.toString();
    }
    
    public boolean isSetted()
    {
        return selectedOptions != null && selectedOptions.size() > 0;
    }
}
