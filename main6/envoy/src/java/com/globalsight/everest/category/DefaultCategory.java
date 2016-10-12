/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.category;

import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jodd.util.StringBand;

public class DefaultCategory
{
    private ArrayList<CommonCategory> availableSegmentCommentCategories = new ArrayList<CommonCategory>();
    private ArrayList<CommonCategory> availableScorecardCategories = new ArrayList<CommonCategory>();
    private ArrayList<CommonCategory> availableQualityCategories = new ArrayList<CommonCategory>();
    private ArrayList<CommonCategory> availableMarketCategories = new ArrayList<CommonCategory>();
    private ArrayList<CommonCategory> availableFluencyCategories = new ArrayList<CommonCategory>();
    private ArrayList<CommonCategory> availableAdequacyCategories = new ArrayList<CommonCategory>();
    private ArrayList<CommonCategory> availableSeverityCategories = new ArrayList<CommonCategory>();

    private ResourceBundle bundle = null;

    public DefaultCategory(ResourceBundle bundle)
    {
        this.bundle = bundle;
    }

    public void init()
    {
        setAvailableSegmentCommentCategories(getDefaultCategories(bundle,
                CategoryType.SegmentComment));
        setAvailableScorecardCategories(getDefaultCategories(bundle, CategoryType.ScoreCard));
        setAvailableQualityCategories(getDefaultCategories(bundle, CategoryType.Quality));
        setAvailableMarketCategories(getDefaultCategories(bundle, CategoryType.Market));
        setAvailableFluencyCategories(getDefaultCategories(bundle, CategoryType.Fluency));
        setAvailableAdequacyCategories(getDefaultCategories(bundle, CategoryType.Adequacy));
        setAvailableSeverityCategories(getDefaultCategories(bundle, CategoryType.Severity));
    }

    public ArrayList<CommonCategory> getAvailableScorecardCategories()
    {
        return availableScorecardCategories;
    }

    public void setAvailableScorecardCategories(
            ArrayList<CommonCategory> availableScorecardCategories)
    {
        this.availableScorecardCategories = availableScorecardCategories;
    }

    public ArrayList<CommonCategory> getAvailableQualityCategories()
    {
        return availableQualityCategories;
    }

    public void setAvailableQualityCategories(ArrayList<CommonCategory> availableQualityCategories)
    {
        this.availableQualityCategories = availableQualityCategories;
    }

    public ArrayList<CommonCategory> getAvailableMarketCategories()
    {
        return availableMarketCategories;
    }

    public void setAvailableMarketCategories(ArrayList<CommonCategory> availableMarketCategories)
    {
        this.availableMarketCategories = availableMarketCategories;
    }

    public ArrayList<CommonCategory> getAvailableFluencyCategories()
    {
        return availableFluencyCategories;
    }

    public void setAvailableFluencyCategories(ArrayList<CommonCategory> availableFluencyCategories)
    {
        this.availableFluencyCategories = availableFluencyCategories;
    }

    public ArrayList<CommonCategory> getAvailableAdequacyCategories()
    {
        return availableAdequacyCategories;
    }

    public void setAvailableAdequacyCategories(ArrayList<CommonCategory> availableAdequacyCategories)
    {
        this.availableAdequacyCategories = availableAdequacyCategories;
    }

    public ArrayList<CommonCategory> getAvailableSegmentCommentCategories()
    {
        return availableSegmentCommentCategories;
    }

    public void setAvailableSegmentCommentCategories(
            ArrayList<CommonCategory> availableSegmentCommentCategories)
    {
        this.availableSegmentCommentCategories = availableSegmentCommentCategories;
    }

    public ArrayList<CommonCategory> getAvailableSeverityCategories()
    {
        return availableSeverityCategories;
    }

    public void setAvailableSeverityCategories(ArrayList<CommonCategory> availableSeverityCategories)
    {
        this.availableSeverityCategories = availableSeverityCategories;
    }

    public ArrayList<CommonCategory> getDefaultCategories(ResourceBundle bundle,
            CategoryType type)
    {
        if (bundle == null)
            return null;
        ArrayList<CommonCategory> categories = null;
        CommonCategory category = null;
        String value = null;
        switch (type)
        {
            case ScoreCard:
                categories = generateList(CategoryHelper.SCORECARD, bundle, type);
                break;
            case Quality:
                categories = generateList(CategoryHelper.QAULITY, bundle, type);
                break;
            case Market:
                categories = generateList(CategoryHelper.MARKET, bundle, type);
                break;
            case Fluency:
                categories = generateList(CategoryHelper.FLUENCY, bundle, type);
                break;
            case Adequacy:
                categories = generateList(CategoryHelper.ADEQUACY, bundle, type);
                break;
            case SegmentComment:
                categories = generateList(CategoryHelper.SEGMENT_COMMENT, bundle, type);
                break;
            case Severity:
                categories = generateList(CategoryHelper.SEVERITY, bundle, type);
                break;
            default:
                categories = new ArrayList<CommonCategory>();
                break;
        }
        return categories;
    }

    public String getDefaultCategoriesAsString(CategoryType type)
    {
        StringBand tmp = new StringBand();
        String[] tmpArray = null;
        switch (type)
        {
            case ScoreCard:
                tmpArray = CategoryHelper.SCORECARD;
                break;
            case Quality:
                tmpArray = CategoryHelper.QAULITY;
                break;
            case Market:
                tmpArray = CategoryHelper.MARKET;
                break;
            case Fluency:
                tmpArray = CategoryHelper.FLUENCY;
                break;
            case Adequacy:
                tmpArray = CategoryHelper.ADEQUACY;
                break;
            case SegmentComment:
                tmpArray = CategoryHelper.SEGMENT_COMMENT;
                break;
            case Severity:
                tmpArray = CategoryHelper.SEVERITY;
                break;
            default:
                tmpArray = CategoryHelper.SCORECARD;
                break;
        }
        for (String string : tmpArray)
        {
            tmp.append(string).append(",");
        }
        return tmp.toString();
    }

    private static ArrayList<CommonCategory> generateList(String[] names, ResourceBundle bundle,
            CategoryType type)
    {
        ArrayList<CommonCategory> categories = new ArrayList<CommonCategory>();
        if (names == null || names.length == 0)
            return categories;

        CommonCategory category;
        String value;
        for (String name : names)
        {
            try
            {
                value = bundle.getString(name);
            }
            catch (MissingResourceException e)
            {
                value = "";
            }
            category = new CommonCategory();
            category.setName(value);
            category.setMemo(name);
            category.setType(type.getValue());
            categories.add(category);
        }
        return categories;
    }
}
