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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class TMAttributeManager
{
    static private final Logger logger = Logger
            .getLogger(TMAttributeManager.class);

    public static List<Attribute> getAvailableAttributes()
    {
        List<Attribute> allAtt = (List<Attribute>) AttributeManager
                .getAllAttributes();
        List<Attribute> result = new ArrayList<Attribute>();

        if (allAtt != null)
        {
            for (Attribute attribute : allAtt)
            {
                // only text and choice list type
                if (!isAvailableType(attribute))
                {
                    continue;
                }

                result.add(attribute);
            }
        }

        return result;
    }

    public static List<String> getAvailableAttributenames()
    {
        List<Attribute> atts = getAvailableAttributes();
        List<String> result = new ArrayList<String>();

        if (atts != null)
        {
            for (Attribute attribute : atts)
            {
                result.add(attribute.getName());
            }
        }

        SortUtil.sort(result);

        return result;
    }

    public static List<Attribute> getAvailableAttributes(ProjectTM tm)
    {
        List<Attribute> allAtt = (List<Attribute>) AttributeManager
                .getAllAttributes();
        List<Attribute> result = new ArrayList<Attribute>();
        List<TMAttribute> tmAts = tm.getAllTMAttributes();

        if (allAtt != null)
        {
            for (Attribute attribute : allAtt)
            {
                // only text and choice list type
                if (!isAvailableType(attribute))
                {
                    continue;
                }

                boolean exists = false;
                // already have this attribute
                if (tmAts != null)
                {
                    for (TMAttribute tmAtt : tmAts)
                    {
                        if (tmAtt.getAttributename().equalsIgnoreCase(
                                attribute.getName()))
                        {
                            exists = true;
                            continue;
                        }
                    }
                }

                if (!exists)
                {
                    result.add(attribute);
                }
            }
        }

        return result;
    }

    private static boolean isAvailableType(Attribute attribute)
    {
        if (Attribute.TYPE_TEXT.equals(attribute.getType())
                || Attribute.TYPE_CHOICE_LIST.equals(attribute.getType()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static List<Attribute> getAvailableAttributes(long tmid)
    {
        ProjectTM tm = HibernateUtil.get(ProjectTM.class, tmid);

        return getAvailableAttributes(tm);
    }

    public static List<String> getAvailableAttributenames(ProjectTM tm)
    {
        List<Attribute> atts = getAvailableAttributes(tm);
        List<String> result = new ArrayList<String>();

        if (atts != null)
        {
            for (Attribute attribute : atts)
            {
                result.add(attribute.getName());
            }
        }

        SortUtil.sort(result);

        return result;
    }

    public static String toOne(List<TMAttribute> tmas)
    {
        StringBuffer sb = new StringBuffer();

        if (tmas != null)
        {
            for (TMAttribute tma : tmas)
            {
                sb.append(tma.getAttributename());
                sb.append(":");
                sb.append(tma.getSettype());
                sb.append(",");
            }

            if (sb.length() > 0)
            {
                sb.delete(sb.length() - 1, sb.length());
            }
        }

        return sb.toString();
    }

    public static String toOneStr(List<String> strs)
    {
        StringBuffer sb = new StringBuffer();

        if (strs != null)
        {
            for (String str : strs)
            {
                sb.append(str).append(",");
            }

            if (sb.length() > 0)
            {
                sb.delete(sb.length() - 1, sb.length());
            }
        }

        return sb.toString();
    }

    public static void setTMAttributes(ProjectTM tm, String oneStr)
    {
        Set<TMAttribute> attSet = new HashSet<TMAttribute>();

        if (oneStr != null)
        {
            String[] ooo = oneStr.split(",");
            if (ooo != null && ooo.length != 0)
            {
                for (int i = 0; i < ooo.length; i++)
                {
                    String o = ooo[i];
                    String[] temp = o.split(":");

                    if (temp != null && temp.length == 2)
                    {
                        TMAttribute tma = new TMAttribute();
                        tma.setAttributename(temp[0]);
                        tma.setSettype(temp[1]);
                        tma.setTm(tm);

                        attSet.add(tma);
                    }
                }
            }
        }

        tm.setAttributes(attSet);
    }

    public static List<String> getAvailableAttributenames(long tmid)
    {
        ProjectTM tm = HibernateUtil.get(ProjectTM.class, tmid);

        return getAvailableAttributenames(tm);
    }

    public static TMAttribute getTMAttribute(ProjectTM tm, String attname)
    {
        List<TMAttribute> all = tm.getAllTMAttributes();
        if (all != null)
        {
            for (TMAttribute tma : all)
            {
                if (tma.getAttributename().equalsIgnoreCase(attname))
                {
                    return tma;
                }
            }
        }

        return null;
    }

    public static String getJobAttributeValue(JobAttribute jobAtt)
    {
        String result = "";

        if (jobAtt != null && jobAtt.getValue() != null)
        {
            Object obj = jobAtt.getValue();

            if (obj instanceof List<?>)
            {
                List<?> l = (List<?>) obj;
                if (l.size() != 0)
                {
                    Object l1 = l.get(0);
                    result = l1.toString();
                }
            }
            else
            {
                result = obj.toString();
            }
        }

        return result;
    }

    public static boolean isTMPAttributeMatched(String tmpAttOp,
            String tuAttValue, Object aValue)
    {
        boolean matched = false;
        String strValue = null;

        if (TMAttributeCons.OP_EQUAL.equals(tmpAttOp))
        {
            if (aValue != null && aValue instanceof List)
            {
                List l = (List) aValue;
                for (Object object : l)
                {
                    strValue = object == null ? "" : object.toString();
                    if (tuAttValue != null && tuAttValue.equals(strValue))
                    {
                        matched = true;
                        break;
                    }
                }
            }
            else
            {
                strValue = aValue == null ? "" : aValue.toString();
                if (tuAttValue != null && tuAttValue.equals(strValue))
                {
                    matched = true;
                }
            }
        }
        else if (TMAttributeCons.OP_NOT_EQUAL.equals(tmpAttOp))
        {
            if (aValue != null && aValue instanceof List)
            {
                List l = (List) aValue;
                for (Object object : l)
                {
                    strValue = object == null ? "" : object.toString();
                    if (tuAttValue != null && !tuAttValue.equals(strValue))
                    {
                        matched = true;
                        break;
                    }

                    if (tuAttValue == null && strValue != null)
                    {
                        matched = true;
                        break;
                    }
                }
            }
            else
            {
                strValue = aValue == null ? "" : aValue.toString();
                if (tuAttValue != null && !tuAttValue.equals(strValue))
                {
                    matched = true;
                }

                if (tuAttValue == null && strValue != null)
                {
                    matched = true;
                }
            }
        }
        else if (TMAttributeCons.OP_MATCH.equals(tmpAttOp))
        {
            if (aValue != null && aValue instanceof List)
            {
                List l = (List) aValue;
                for (Object object : l)
                {
                    strValue = object == null ? "" : object.toString();
                    if (tuAttValue == null || strValue == null)
                    {
                        matched = false;
                        break;
                    }
                    else if (tuAttValue.matches(strValue))
                    {
                        matched = true;
                        break;
                    }
                }
            }
            else
            {
                strValue = aValue == null ? "" : aValue.toString();
                if (tuAttValue == null || strValue == null)
                {
                    matched = false;
                }
                else if (tuAttValue.matches(strValue))
                {
                    matched = true;
                }
            }
        }

        return matched;
    }

    public static Map<String, String> getTUAttributesForPopulator(
            ProjectTM projectTM, Job job)
    {
        Map<String, String> attValue = new HashMap<String, String>();
        if (job != null && projectTM != null)
        {
            List<TMAttribute> tmas = projectTM.getAllTMAttributes();
            List<JobAttribute> jobas = job.getAllJobAttributes();

            if (tmas.size() > 0 && jobas.size() > 0)
            {
                for (TMAttribute tma : tmas)
                {
                    if (tma.getSettype()
                            .equals(TMAttributeCons.SET_FROM_JOBATT))
                    {
                        for (JobAttribute joba : jobas)
                        {
                            if (joba.getAttribute().getName()
                                    .equals(tma.getAttributename()))
                            {
                                String vs = TMAttributeManager
                                        .getJobAttributeValue(joba);
                                if (!"".equals(vs))
                                    attValue.put(tma.getAttributename(), vs);
                            }
                        }
                    }
                }
            }
        }

        return attValue;
    }
}
