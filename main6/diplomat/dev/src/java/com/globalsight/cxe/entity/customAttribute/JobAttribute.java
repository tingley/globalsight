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

package com.globalsight.cxe.entity.customAttribute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.ValidateException;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.webapp.pagehandler.administration.jobAttribute.JobAttributeFileManager;
import com.globalsight.util.Assert;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

public class JobAttribute extends PersistentObject
{
    static private final Logger logger = Logger.getLogger(JobAttribute.class);

    private static final long serialVersionUID = 4341436923688472768L;
    private JobImpl job;
    private AttributeClone attribute;
    private String stringValue;
    private Integer integerValue;
    private Float floatValue;
    private Date dateValue;
    private Set<FileValueItem> fileValues = new HashSet<FileValueItem>();
    private Set<SelectOption> optionValues = new HashSet<SelectOption>();

    public boolean setValue(Object ob)
    {
        if (ob == null)
        {
            stringValue = null;
            integerValue = null;
            floatValue = null;
            dateValue = null;
            optionValues = new HashSet<SelectOption>();
            fileValues = new HashSet<FileValueItem>();
            return true;
        }
        else
        {
            return setValue(ob, true);
        }
    }

    public boolean setValue(Object ob, boolean byId)
    {
        Assert.assertNotNull(attribute, "Attribute");

        String type = attribute.getType();
        if (Attribute.TYPE_TEXT.equals(type))
        {
            stringValue = ob.toString();
            return true;
        }
        else if (Attribute.TYPE_FLOAT.equals(type))
        {
            if (ob instanceof String)
            {
                floatValue = Float.parseFloat((String) ob);
            }
            else
            {
                floatValue = (Float) ob;
            }
            return true;
        }
        else if (Attribute.TYPE_INTEGER.equals(type))
        {
            if (ob instanceof String)
            {
                integerValue = Integer.parseInt((String) ob);
            }
            else
            {
                integerValue = (Integer) ob;
            }
            return true;
        }
        else if (Attribute.TYPE_DATE.equals(type))
        {
            if (ob instanceof String)
            {
                dateValue = new Date(Long.parseLong((String) ob));
            }
            else
            {
                dateValue = (Date) ob;
            }
            return true;
        }
        else if (Attribute.TYPE_CHOICE_LIST.equals(type))
        {
            ListCondition listCondition = (ListCondition) attribute
                    .getCondition();
            Set<SelectOption> allOption = listCondition.getAllOptions();

            optionValues = new HashSet<SelectOption>();
            if (ob instanceof Long)
            {
                SelectOption option = getSelectOption(ob.toString(), allOption,
                        byId);
                optionValues.add(option);
            }
            else if (ob instanceof String)
            {
                SelectOption option = getSelectOption((String) ob, allOption,
                        byId);
                optionValues.add(option);
            }
            else if (ob instanceof Collection)
            {
                Collection<Object> values = (Collection<Object>) ob;
                for (Object value : values)
                {
                    SelectOption option = getSelectOption(value.toString(),
                            allOption, byId);
                    optionValues.add(option);
                }
            }

            return true;
        }
        else if (Attribute.TYPE_FILE.equals(type))
        {
            // May be it is files
            if (ob instanceof Map)
            {
                Map files = (Map) ob;
                Set<String> keys = files.keySet();
                for (String key : keys)
                {
                    writeFile(key, (byte[]) files.get(key));
                }

                return true;
            }
        }

        return false;
    }

    public Integer convertedToInteger(String s)
    {
        if (s == null)
            return null;

        s = s.trim();
        if (s.length() == 0)
            return null;

        Assert.assertIsInteger(s);
        Integer value = Integer.parseInt(s);

        if (attribute != null)
        {
            Condition condition = attribute.getCondition();
            if (condition != null
                    && Attribute.TYPE_INTEGER.equals(condition.getType()))
            {
                IntCondition intCondition = (IntCondition) condition;
                Integer max = intCondition.getMax();
                Integer min = intCondition.getMin();
                Assert.assertIntBetween(value, min, max);
            }
        }

        return value;
    }

    public String convertedToText(String s)
    {
        if (s == null)
            return null;

        s = s.trim();
        if (s.length() == 0)
            return null;

        if (attribute != null)
        {
            Condition condition = attribute.getCondition();
            if (condition != null
                    && Attribute.TYPE_TEXT.equals(condition.getType()))
            {
                TextCondition textCondition = (TextCondition) condition;
                Integer length = textCondition.getLength();
                Assert.assertTextNotTooLong(s, length);
            }
        }

        return s;
    }

    public Date convertedToDate(String s)
    {
        if (s == null)
            return null;

        s = s.trim();
        if (s.length() == 0)
            return null;

        Assert.assertIsDate(s, DateCondition.FORMAT);
        SimpleDateFormat sdf = new SimpleDateFormat(DateCondition.FORMAT);

        try
        {
            return sdf.parse(s);
        }
        catch (ParseException e)
        {
            ValidateException e1 = new ValidateException(s.trim()
                    + " can not be converted into a date",
                    "msg_validate_not_date");
            e1.addValue(s.trim());
            throw e1;
        }
    }

    public Float convertedToFloat(String s)
    {
        if (s == null)
            return null;

        s = s.trim();
        if (s.length() == 0)
            return null;

        Assert.assertIsFloat(s);
        Float f = Float.parseFloat(s);

        if (attribute != null)
        {
            Condition condition = attribute.getCondition();
            if (condition != null
                    && Attribute.TYPE_FLOAT.equals(condition.getType()))
            {
                FloatCondition floatCondition = (FloatCondition) condition;
                Float max = floatCondition.getMax();
                Float min = floatCondition.getMin();
                Assert.assertFloatBetween(f, min, max);
            }
        }

        return f;
    }

    private SelectOption getSelectOption(String ob, Set<SelectOption> options,
            boolean byId)
    {
        if (byId)
        {
            long id = Long.parseLong(ob);

            for (SelectOption option : options)
            {
                if (option.getId() == id)
                {
                    return option;
                }
            }
        }
        else
        {
            for (SelectOption option : options)
            {
                if (option.getValue().equals(ob))
                {
                    return option;
                }
            }
        }

        throw new IllegalArgumentException(
                "Can not find the select option with: " + ob);
    }

    public String getType()
    {
        if (attribute == null)
            return Attribute.TYPE_TEXT;

        return attribute.getType();
    }

    public Object getValue()
    {
        Assert.assertNotNull(attribute, "Attribute");

        String type = attribute.getType();
        if (Attribute.TYPE_TEXT.equals(type))
        {
            return stringValue;
        }

        if (Attribute.TYPE_FLOAT.equals(type))
        {
            return floatValue;
        }

        if (Attribute.TYPE_INTEGER.equals(type))
        {
            return integerValue;
        }

        if (Attribute.TYPE_DATE.equals(type))
        {
            return dateValue;
        }

        if (Attribute.TYPE_CHOICE_LIST.equals(type))
        {
            return getOptionValuesAsStrings();
        }

        if (Attribute.TYPE_FILE.equals(type))
        {
            return getFileValuesAsStrings();
        }

        return null;
    }

    public JobImpl getJob()
    {
        return job;
    }

    public void setJob(JobImpl job)
    {
        this.job = job;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    public void setStringValue(String stringValue)
    {
        this.stringValue = stringValue;
    }

    public Integer getIntegerValue()
    {
        return integerValue;
    }

    public void setIntegerValue(Integer integerValue)
    {
        this.integerValue = integerValue;
    }

    public Float getFloatValue()
    {
        return floatValue;
    }

    public void setFloatValue(Float floatValue)
    {
        this.floatValue = floatValue;
    }

    public Date getDateValue()
    {
        return dateValue;
    }

    public void setDateValue(Date dateValue)
    {
        this.dateValue = dateValue;
    }

    public AttributeClone getAttribute()
    {
        return attribute;
    }

    public void setAttribute(AttributeClone attribute)
    {
        this.attribute = attribute;
    }

    public List<String> getFileValuesAsStrings()
    {
        return JobAttributeFileManager.getAllFilesAsString2(getId(),
                String.valueOf(getJob().getCompanyId()));
    }

    public Set<FileValueItem> getFileValues()
    {
        return fileValues;
    }

    public void setFileValues(Set<FileValueItem> fileValues)
    {
        this.fileValues = fileValues;
    }

    public List<File> getFiles()
    {
        if (getId() < 1)
        {
            return new ArrayList<File>();
        }

        return JobAttributeFileManager.getAllFiles(getId());
    }

    public void writeFile(String fileName, byte[] bytes)
    {
        if (getId() < 1)
        {
            return;
        }

        String path = JobAttributeFileManager.getStorePath(getId());
        File root = new File(path);
        if (!root.exists())
        {
            root.mkdirs();
        }

        File newFile = new File(path + "/" + fileName);
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(newFile, false);
            fos.write(bytes);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void addFile(File file)
    {
        if (getId() < 1)
        {
            return;
        }

        String path = JobAttributeFileManager.getStorePath(getId());
        File root = new File(path);
        if (!root.exists())
        {
            root.mkdirs();
        }

        File target = new File(path + "/" + file.getName());
        if (!file.renameTo(target))
        {
            try
            {
                FileUtil.copyFile(file, target);
                file.delete();
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public String getFilesLabel()
    {
        StringBuffer label = new StringBuffer();
        List<String> files = getDisplayFiles();
        for (String file : files)
        {
            if (label.length() > 0)
            {
                label.append("<br>");
            }
            label.append(EditUtil.encodeHtmlEntities(file));
        }

        return label.toString();
    }

    public String getFloatLabel()
    {
        if (floatValue == null)
            return "";

        return floatValue.toString();
    }

    public String getIntLabel()
    {
        if (integerValue == null)
            return "";

        return integerValue.toString();
    }

    public String getTextLabel()
    {
        if (stringValue == null)
            return "";

        return EditUtil.encodeHtmlEntities(stringValue);
    }

    public String getDateLabel()
    {
        if (dateValue == null)
            return "";

        SimpleDateFormat sdf = new SimpleDateFormat(DateCondition.FORMAT);
        String date = sdf.format(dateValue);

        return EditUtil.encodeHtmlEntities(date);
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

    public List<String> getDisplayFiles()
    {
        List<File> files = getFiles();
        List<String> displayFiles = new ArrayList<String>();

        for (File file : files)
        {
            displayFiles.add(JobAttributeFileManager.getDisplayPath(getId(),
                    file));
        }

        return displayFiles;
    }

    public Set<SelectOption> getOptionValues()
    {
        return optionValues;
    }

    public List<String> getOptionValuesAsStrings()
    {
        List<String> result = new ArrayList<String>();
        if (optionValues != null)
        {
            for (SelectOption item : optionValues)
            {
                result.add(item.getValue());
            }
        }

        SortUtil.sort(result);
        return result;
    }

    public void setOptionValues(Set<SelectOption> optionValues)
    {
        this.optionValues = optionValues;
    }

    public static JobAttribute newInstence(JobImpl job, Attribute attribute)
    {
        JobAttribute jobAttribute = new JobAttribute();
        jobAttribute.setJob(job);
        jobAttribute.setAttribute(attribute.getCloneAttribute());

        return jobAttribute;
    }

    public boolean isSet()
    {
        String type = attribute.getType();
        if (Attribute.TYPE_TEXT.equals(type))
        {
            return stringValue != null && stringValue.length() > 0;
        }
        else if (Attribute.TYPE_FLOAT.equals(type))
        {
            return floatValue != null;
        }
        else if (Attribute.TYPE_INTEGER.equals(type))
        {
            return integerValue != null;
        }
        else if (Attribute.TYPE_DATE.equals(type))
        {
            return dateValue != null;
        }
        else if (Attribute.TYPE_CHOICE_LIST.equals(type))
        {
            return optionValues != null && optionValues.size() > 0;
        }
        else if (Attribute.TYPE_FILE.equals(type))
        {
            return getFiles().size() > 0;
        }

        return false;
    }
}
