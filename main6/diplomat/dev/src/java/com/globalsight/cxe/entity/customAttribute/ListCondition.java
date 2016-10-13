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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class ListCondition extends PersistentObject implements Condition
{
    private static final long serialVersionUID = -6760477237259424850L;
    private boolean multiple;
    private Set<SelectOption> allOptions;
    public static final String NOT_SET = "not set";

    public Set<SelectOption> getAllOptions()
    {
        return allOptions;
    }

    public List<SelectOption> getSortedAllOptions()
    {
        List<SelectOption> options = new ArrayList<SelectOption>();
        if (allOptions != null)
        {
            options.addAll(allOptions);
        }

        SortUtil.sort(options, new Comparator<SelectOption>()
        {
            @Override
            public int compare(SelectOption o1, SelectOption o2)
            {
                String name1 = o1.getValue();
                String name2 = o2.getValue();

                if (NOT_SET.equalsIgnoreCase(name1))
                    return -1;

                if (NOT_SET.equalsIgnoreCase(name2))
                    return 1;

                return name1.compareTo(name2);
            }
        });

        return options;
    }

    public void setAllOptions(Set<SelectOption> allOptions)
    {
        this.allOptions = allOptions;
    }

    public void addOption(String option)
    {
        if (allOptions == null)
        {
            allOptions = new HashSet<SelectOption>();
        }

        SelectOption o = new SelectOption();
        o.setValue(option);
        o.setListCondition(this);

        allOptions.add(o);
    }

    public List<String> getOptions()
    {
        List<String> options = new ArrayList<String>();

        if (allOptions != null)
        {
            for (SelectOption option : allOptions)
            {
                options.add(option.getValue());
            }
        }

        boolean removed = options.remove(NOT_SET);

        SortUtil.sort(options);

        if (removed)
        {
            options.add(0, NOT_SET);
        }

        return options;
    }

    @Override
    public String getType()
    {
        return Attribute.TYPE_CHOICE_LIST;
    }

    public boolean isMultiple()
    {
        return multiple;
    }

    public void setMultiple(boolean multiple)
    {
        this.multiple = multiple;
    }

    @Override
    public void updateCondition(HttpServletRequest request, Attribute attribute)
    {
        Condition condition = attribute.getCondition();
        ListCondition lCondition;
        if (condition != null && condition instanceof ListCondition)
        {
            lCondition = (ListCondition) condition;
        }
        else
        {
            lCondition = new ListCondition();
            attribute.setCondition(lCondition);
        }

        String[] items = request.getParameterValues("allItems");
        ArrayList<String> uiItems = new ArrayList<String>();

        if (items != null)
        {
            for (String item : items)
            {
                uiItems.add(item.trim());
            }
        }

        Set<SelectOption> allOption = lCondition.getAllOptions();
        Set<SelectOption> deleteOptions = new HashSet<SelectOption>();
        if (allOption != null)
        {
            for (SelectOption option : allOption)
            {
                if (uiItems.contains(option.getValue()))
                {
                    uiItems.remove(option.getValue());
                }
                else
                {
                    deleteOptions.add(option);
                }
            }

            allOption.removeAll(deleteOptions);
        }

        try
        {
            for (SelectOption option : deleteOptions)
            {
                HibernateUtil.delete(option);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (String uiItem : uiItems)
        {
            lCondition.addOption(uiItem);
        }

        lCondition.setMultiple(request.getParameter("multipleChoice") != null);
    }

    @Override
    public Condition getCloneCondition()
    {
        ListCondition condition = new ListCondition();
        condition.setMultiple(multiple);

        if (allOptions != null)
        {
            for (SelectOption option : getAllOptions())
            {
                condition.addOption(option.getValue());
            }
        }

        return condition;
    }
}
