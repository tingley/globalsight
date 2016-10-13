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

package com.globalsight.everest.comment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;

/**
 *  Utility class for getting all valid states and priorities of an Issue
 *  and converting them appropriately.
 */
public class IssueOptions
{
    private static ArrayList c_allStatus = new ArrayList(3);
    private static HashMap c_allPriorities = new HashMap(4);
    // add categories to comments
    private static List c_allCategories = new ArrayList(15);
    private static Map<String, String> c_lb_allCategories = new HashMap<String, String>(15);

    static
    {
        c_allStatus.add(Issue.STATUS_OPEN);
        c_allStatus.add(Issue.STATUS_QUERY);
        c_allStatus.add(Issue.STATUS_CLOSED);
        c_allStatus.add(Issue.STATUS_REJECTED);

        c_allPriorities.put(new Integer(1), Issue.PRI_URGENT);
        c_allPriorities.put(new Integer(2), Issue.PRI_HIGH);
        c_allPriorities.put(new Integer(3), Issue.PRI_MEDIUM);
        c_allPriorities.put(new Integer(4), Issue.PRI_LOW);
        
        c_allCategories.add(Issue.CATEGORY_CONFILICTS);
        c_allCategories.add(Issue.CATEGORY_FORMATTING);
        c_allCategories.add(Issue.CATEGORY_MISTRSLATED);
        c_allCategories.add(Issue.CATEGORY_OMISSION);
        c_allCategories.add(Issue.CATEGORY_SPELLING);
        
        c_allCategories.add(Issue.CATEGORY_TYPE01);
        c_allCategories.add(Issue.CATEGORY_TYPE02);
        c_allCategories.add(Issue.CATEGORY_TYPE03);
        c_allCategories.add(Issue.CATEGORY_TYPE04);
        c_allCategories.add(Issue.CATEGORY_TYPE05);
        c_allCategories.add(Issue.CATEGORY_TYPE06);
        c_allCategories.add(Issue.CATEGORY_TYPE07);
        c_allCategories.add(Issue.CATEGORY_TYPE08);
        c_allCategories.add(Issue.CATEGORY_TYPE09);
        c_allCategories.add(Issue.CATEGORY_TYPE10);
        
        c_lb_allCategories.put(Issue.CATEGORY_CONFILICTS, "lb_conflicts_glossary_guide");
        c_lb_allCategories.put(Issue.CATEGORY_FORMATTING, "lb_formatting_error");
        c_lb_allCategories.put(Issue.CATEGORY_MISTRSLATED, "lb_mistranslated");
        c_lb_allCategories.put(Issue.CATEGORY_OMISSION, "lb_omission_of_text");
        c_lb_allCategories.put(Issue.CATEGORY_SPELLING, "lb_spelling_grammar_punctuation_error");
        
        c_lb_allCategories.put(Issue.CATEGORY_TYPE01, "issue.category.type01");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE02, "issue.category.type02");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE03, "issue.category.type03");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE04, "issue.category.type04");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE05, "issue.category.type05");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE06, "issue.category.type06");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE07, "issue.category.type07");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE08, "issue.category.type08");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE09, "issue.category.type09");
        c_lb_allCategories.put(Issue.CATEGORY_TYPE10, "issue.category.type10");
        
    }

    public static String getDisplayCategory(String category, ResourceBundle bundle)
    {
        String key = c_lb_allCategories.get(category);
        if (key == null)
            return category;
        
        return bundle.getString(key);
    }
    
    /**
     * Gets all the categories.
     * 
     * @return the list of categories
     */
    public static List getAllCategories()
    {
        return c_allCategories;
    }
    
    public static List getAllCategories(ResourceBundle bundle, String currentCompanyId)
    {
        List<String> categoryList = CompanyWrapper
                .getCompanyCategoryList(currentCompanyId);
        List<Select> list = new ArrayList<Select>();
        for (String key : categoryList)
        {
            String valueOfSelect = "";
            try
            {
                valueOfSelect = bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                valueOfSelect = key;
            }
            // we should put value both at key and value places
            Select option = new Select(valueOfSelect, valueOfSelect);
            list.add(option);
        }
        return list;
    }
    
    public static List getAllQualityCategories(ResourceBundle bundle, String currentCompanyId)
    {
        List<String> qualityCategoryList = CompanyWrapper
                .getCompanyQualityCategoryList(currentCompanyId);
        List<Select> list = new ArrayList<Select>();
        for (String key : qualityCategoryList)
        {
            String valueOfSelect = "";
            try
            {
                valueOfSelect = bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                valueOfSelect = key;
            }
            // we should put value both at key and value places
            Select option = new Select(valueOfSelect, valueOfSelect);
            list.add(option);
        }
        return list;
    }
    
    public static List getAllMarketCategories(ResourceBundle bundle, String currentCompanyId)
    {
        List<String> marketCategoryList = CompanyWrapper
                .getCompanyMarketCategoryList(currentCompanyId);
        List<Select> list = new ArrayList<Select>();
        for (String key : marketCategoryList)
        {
            String valueOfSelect = "";
            try
            {
                valueOfSelect = bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                valueOfSelect = key;
            }
            // we should put value both at key and value places
            Select option = new Select(valueOfSelect, valueOfSelect);
            list.add(option);
        }
        return list;
    }

    public static List getAllStatus()
    {
        return c_allStatus;
    }

    public static Map getAllPriorities()
    {
       return c_allPriorities;
    }

    public static String getPriority(int p_orderNum)
    {
        // if the order isn't bigger than the size
        // return the right priority name
        if (p_orderNum <= c_allPriorities.size()) 
        {
            return (String)c_allPriorities.get(new Integer(p_orderNum));
        }
        else
        {
            return "unknown";
        }             
    }   
}
