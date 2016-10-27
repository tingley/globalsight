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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import jodd.util.StringBand;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Helper class for category
 * 
 * @author VincentYan
 * @since 8.7.2
 */
public class CategoryHelper
{
    private static Logger logger = Logger.getLogger(CategoryHelper.class);
    
    public static String[] SEGMENT_COMMENT = new String[]
    { "lb_conflicts_glossary_guide", "lb_formatting_error", "lb_mistranslated",
            "lb_omission_of_text", "lb_spelling_grammar_punctuation_error" };
    public static String[] SCORECARD = new String[]
    { "lb_spelling_grammar", "lb_consistency", "lb_style", "lb_terminology", };
    public static String[] QAULITY = new String[]
    { "lb_good", "lb_acceptable", "lb_poor" };
    public static String[] MARKET = new String[]
    { "lb_suitable_fluent", "lb_literal_at_times", "lb_unsuitable" };
    public static String[] FLUENCY = new String[]
    { "lb_dqf_fluency_incomprehensible", "lb_dqf_fluency_disfluent",
            "lb_dqf_fluency_good", "lb_dqf_fluency_flawless" };
    public static String[] ADEQUACY = new String[]
    { "lb_none", "lb_dqf_adequacy_little", "lb_dqf_adequacy_most", "lb_dqf_adequacy_everything" };
    public static String[] SEVERITY = new String[]
    { "lb_dqf_severity_critical", "lb_dqf_severity_major", "lb_dqf_severity_minor",
            "lb_dqf_severity_neutral", "lb_dqf_severity_positive", "lb_dqf_severity_invalid" };

    public static List<CommonCategory> getCategories(CategoryType type, boolean isAvailable)
    {
        return isAvailable ? getCategories(type, 1) : getCategories(type, 0);
    }

    public static List<CommonCategory> getCategories(CategoryType type, int status)
    {
        String hql = "from CommonCategory where isActive='Y'";
        if (type != null)
            hql += " and type=" + type.getValue();
        switch (status)
        {
            case 0:
                hql += " and isAvailable='N'";
                break;
            case 1:
                hql += " and isAvailable='Y'";
                break;
            default:
                break;
        }

        return (List<CommonCategory>) HibernateUtil.search(hql);
    }

    public static int addCategory(CommonCategory category)
    {
        try
        {
            HibernateUtil.save(category);
            return 1;
        }
        catch (Exception e)
        {
            logger.error("Error found when adding new category", e);
            return -1;
        }
    }

    public static int addCategories(ArrayList<CommonCategory> categories)
    {
        if (categories == null || categories.size() == 0)
            return 0;

        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            PreparedStatement pstmt = conn
                    .prepareStatement("INSERT INTO Categories (Name,Memo,Type,Company_Id,Is_Available,Is_Active) VALUES (?,?,?,?,?,'Y')");
            for (CommonCategory category : categories)
            {
                pstmt.setString(1, category.getName());
                pstmt.setString(2, category.getMemo());
                pstmt.setInt(3, category.getType());
                pstmt.setLong(4, category.getCompanyId());
                pstmt.setString(5, category.isAvailable() ? "Y" : "N");
                pstmt.addBatch();
            }

            pstmt.executeBatch();

            return 0;
        }
        catch (Exception e)
        {
            logger.error("Error found when adding categories.", e);
            return -1;
        }
        finally
        {
            try
            {
                DbUtil.returnConnection(conn);
            }
            catch (Exception e)
            {
                logger.error("Cannot return database connection successfully.", e);
            }
        }
    }

    public static int updateCategory(CommonCategory category)
    {
        try
        {
            HibernateUtil.update(category);
            return 1;
        }
        catch (Exception e)
        {
            logger.error("Error found when updating category", e);
            return -1;
        }
    }

    public static int updateCategories(ArrayList<CommonCategory> categories)
    {
        if (categories == null || categories.size() == 0)
            return 0;
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            PreparedStatement pstmt = conn
                    .prepareStatement("UPDATE Categories SET Is_Available=? WHERE ID=?");
            for (CommonCategory category : categories)
            {
                pstmt.setString(1, category.isAvailable() ? "Y" : "N");
                pstmt.setLong(2, category.getId());
                pstmt.addBatch();
            }

            pstmt.executeBatch();

            return 0;
        }
        catch (Exception e)
        {
            logger.error("Error found when updating categories.", e);
            return -1;
        }
        finally
        {
            try
            {
                DbUtil.returnConnection(conn);
            }
            catch (Exception e)
            {
                logger.error("Cannot return database connection successfully.", e);
            }
        }

    }

    public static int removeCategory(ArrayList<String> ids)
    {
        if (ids == null || ids.size() == 0)
            return 0;
        try
        {
            int index = 0;
            StringBand sql = new StringBand("update categories set is_Active='N' where id in (");
            for (String id : ids)
            {
                sql.append(id).append(",");
            }
            sql.append("0)");
            return HibernateUtil.executeSql(sql.toString());
        }
        catch (Exception e)
        {
            logger.error("Error found when deleting categories.", e);
            return -1;
        }
    }

    public static void removeCategoryByCompany(long companyId)
    {
        String sql = "update categories set is_active='N' where company_id=" + companyId;
        try
        {
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            logger.error("Error found when deleting categories.", e);
        }
    }
}
