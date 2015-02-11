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
package com.globalsight.everest.webapp.pagehandler.administration.createJobs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.SelectOption;
import com.globalsight.everest.webapp.pagehandler.administration.imp.SetAttributeHandler;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

public class AttributeExtension
{

    private JobAttribute jobAttribute = null;
    private Attribute attribute = null;

    private String intLabel;
    private String floatLabel;
    private String textLabel;
    private String dateLabel;
    private String listLabel;
    private String fileLabel;
    private String root;
    private String multi = "";
    private List<SelectOption> allOptions;

    // init class
    public AttributeExtension()
    {
        super();
    }

    public AttributeExtension(Attribute attribute)
    {
        this.attribute = attribute;
    }

    public JobAttribute getJobAttribute()
    {
        return jobAttribute;
    }

    public void setJobAttribute(JobAttribute jobAttribute)
    {
        this.jobAttribute = jobAttribute;
    }

    public Attribute getAttribute()
    {
        return attribute;
    }

    public void setAttribute(Attribute attribute)
    {
        this.attribute = attribute;
    }

    public String getIntLabel()
    {
        if (jobAttribute == null || jobAttribute.getIntegerValue() == null)
        {
            return "";
        }
        return jobAttribute.getIntLabel().toString();
    }

    public void setIntLabel(String intLabel)
    {
        this.intLabel = intLabel;
    }

    public String getFloatLabel()
    {
        if (jobAttribute == null || jobAttribute.getFloatValue() == null)
            return "";

        return jobAttribute.getFloatLabel().toString();
    }

    public void setFloatLabel(String floatLabel)
    {
        this.floatLabel = floatLabel;
    }

    public String getTextLabel()
    {
        if (jobAttribute == null || jobAttribute.getStringValue() == null)
            return "";

        return EditUtil.encodeHtmlEntities(jobAttribute.getStringValue());
    }

    public void setTextLabel(String textLabel)
    {
        this.textLabel = textLabel;
    }

    public String getDateLabel()
    {
        if (jobAttribute == null || jobAttribute.getDateValue() == null)
            return "";

        SimpleDateFormat sdf = new SimpleDateFormat(DateCondition.FORMAT);
        String date = sdf.format(jobAttribute.getDateValue());

        return EditUtil.encodeHtmlEntities(date);
    }

    public void setDateLabel(String dateLabel)
    {
        this.dateLabel = dateLabel;
    }

    public String getListLabel()
    {
        StringBuffer label = new StringBuffer();
        List<String> options = getOptionValuesAsStrings();
        for (String option : options)
        {
            if (label.length() > 0)
            {
                label.append("<br>");
            }
            label.append(EditUtil.encodeHtmlEntities(option));
        }

        return label.toString();
    }

    public void setListLabel(String listLabel)
    {
        this.listLabel = listLabel;
    }

    private List<String> getOptionValuesAsStrings()
    {
        List<String> result = new ArrayList<String>();
        if (jobAttribute != null && jobAttribute.getOptionValues() != null)
        {
            for (SelectOption item : jobAttribute.getOptionValues())
            {
                result.add(item.getValue());
            }
        }

        SortUtil.sort(result);
        return result;
    }

    public String getFileLabel()
    {
        return SetAttributeHandler.getFileLabel(root + File.separator
                + attribute.getName());
    }

    public void setFileLabel(String fileLabel)
    {
        this.fileLabel = fileLabel;
    }

    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public List<SelectOption> getAllOptions()
    {
        if (attribute != null
                && attribute.getCondition() instanceof ListCondition)
        {
            ListCondition lc = (ListCondition) attribute.getCondition();
            return lc.getSortedAllOptions();
        }
        return new ArrayList<SelectOption>();
    }

    public void setAllOptions(List<SelectOption> allOptions)
    {
        this.allOptions = allOptions;
    }

    public String getMulti()
    {
        if (attribute != null
                && attribute.getCondition() instanceof ListCondition)
        {
            ListCondition lc = (ListCondition) attribute.getCondition();
            if (lc.isMultiple())
            {
                return "multiple";
            }
            else
            {
                return "";
            }
        }
        return multi;
    }

    public void setMulti(String multi)
    {
        this.multi = multi;
    }

}
