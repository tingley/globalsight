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
import com.globalsight.util.StringUtil;

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
            // "choiceList" and "file" type
            if (obj instanceof List<?>)
            {
                List<?> l = (List<?>) obj;
                if (l.size() != 0)
                {
                    for (int i = 0; i < l.size(); i++)
                    {
                        Object l1 = l.get(i);
                        result += l1.toString();
                        if (i != l.size() - 1)
                        {
                            result += ",";
                        }
                    }
                }
            }
            else
            {
                result = obj.toString();
            }
        }

        return result;
    }

    /**
	 * Decide if need disregard or penalize TM matches via checking TU
	 * attributes and job attributes. If not matched, need disregard or penalize
	 * TM matches.
	 * 
	 */
    @SuppressWarnings("rawtypes")
	public static boolean isTMPAttributeMatched(String tmpAttOp,
            String tuAttValue, Object jobAttValue)
    {
    	if (isEmptyJobAttribute(jobAttValue) && StringUtil.isEmpty(tuAttValue))
    	{
    		return true;
    	}

    	boolean matched = false;
        String strValue = null;
        if (TMAttributeCons.OP_EQUAL.equals(tmpAttOp))
        {
            if (jobAttValue != null && jobAttValue instanceof List)
            {
                if (isAttributeExactEquals(jobAttValue, tuAttValue))
                {
                    matched = true;
                }
            }
            else
            {
                strValue = jobAttValue == null ? "" : jobAttValue.toString();
                if (tuAttValue != null && tuAttValue.equals(strValue))
                {
                    matched = true;
                }
            }
        }
        else if (TMAttributeCons.OP_CONTAIN.equals(tmpAttOp))
        {
        	if (jobAttValue == null)
        	{
        		matched = true;
        	}
        	else if (jobAttValue instanceof List)
        	{
        		HashSet<String> options = split(tuAttValue);
        		HashSet<String> jobValues = toStringList((List) jobAttValue);

                matched = options.containsAll(jobValues);
        	}
        	else
        	{
        		strValue = jobAttValue.toString();
        		if (tuAttValue != null && tuAttValue.indexOf(strValue) > -1)
        		{
        			matched = true;
        		}
        	}
        }
        else if (TMAttributeCons.OP_NOT_CONTAIN.equals(tmpAttOp))
        {
        	if (jobAttValue == null)
        	{
        		matched = true;
        	}
        	else if (jobAttValue instanceof List)
        	{
        		HashSet<String> options = split(tuAttValue);
        		HashSet<String> jobValues = toStringList((List) jobAttValue);

        		matched = !options.containsAll(jobValues);
        	}
        	else
        	{
        		strValue = jobAttValue.toString();
        		if (tuAttValue != null && tuAttValue.indexOf(strValue) == -1)
        		{
        			matched = true;
        		}
        	}
        }

        return matched;
    }

    @SuppressWarnings("rawtypes")
	private static boolean isEmptyJobAttribute(Object jobAttValue)
    {
        if (jobAttValue == null)
        {
        	return true;
        }
        else if (jobAttValue instanceof String)
		{
			if (StringUtil.isEmpty((String) jobAttValue))
			{
				return true;
			}
		}
		else if (jobAttValue instanceof List)
		{
	        if (isEmptyList((List) jobAttValue))
			{
				return true;
			}
		}

        return false;
    }

    @SuppressWarnings("rawtypes")
	private static boolean isEmptyList(List list)
    {
    	if (list == null || list.size() == 0) return true;

    	String strValue = null;
    	for (Object object : list)
    	{
    		strValue = object == null ? "" : object.toString();
    		if (StringUtil.isNotEmpty(strValue))
    		{
    			return false;
    		}
    	}

    	return true;
    }

    @SuppressWarnings("rawtypes")
	private static HashSet<String> toStringList(List list)
    {
    	HashSet<String> items = new HashSet<String>();
    	String strValue = null;
        for (Object object : list)
        {
            strValue = object == null ? "" : object.toString();
            if (strValue != null && strValue.trim().length() > 0)
            {
                items.add(strValue.trim());
            }
        }

        return items;
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

    /**
     * Return true only when both have exact same sub items.
     * 
     * @param jobAttValue
     *            Object
     * @param tuAttValue
     *            String comma separated
     * @return
     */
    @SuppressWarnings("rawtypes")
    private static boolean isAttributeExactEquals(Object jobAttValue,
            String tuAttValue)
    {
        HashSet<String> tuAttValueItems = split(tuAttValue);
        HashSet<String> jobAttValueItems = new HashSet<String>();
        if (jobAttValue != null && jobAttValue instanceof List)
        {
			jobAttValueItems = toStringList((List) jobAttValue);
        }

        if (tuAttValueItems.size() != jobAttValueItems.size()
                || tuAttValueItems.size() == 0)
        {
            return false;
        }

        // assume true first
        boolean isEquals = true;
        // Return true only when both have exact same sub items.
        for (String jobAtt : jobAttValueItems)
        {
            if (!tuAttValueItems.contains(jobAtt))
            {
                isEquals = false;
                break;
            }
        }

        return isEquals;
    }

    private static HashSet<String> split(String attValues)
    {
        HashSet<String> set = new HashSet<String>();
        if (attValues != null && attValues.trim().length() > 0)
        {
            String[] strs = attValues.split(",");
            for (String str : strs)
            {
            	if (str != null && str.trim().length() > 0)
            		set.add(str.trim());
            }
        }
        return set;
    }
}
