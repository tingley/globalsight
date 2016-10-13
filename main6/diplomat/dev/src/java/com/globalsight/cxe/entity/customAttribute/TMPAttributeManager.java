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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.AttributeManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.SortUtil;

public class TMPAttributeManager
{
    static private final Logger logger = Logger
            .getLogger(TMPAttributeManager.class);

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

    public static List<Attribute> getAvailableAttributes(
            TranslationMemoryProfile tmp)
    {
        List<Attribute> allAtt = (List<Attribute>) AttributeManager
                .getAllAttributes();
        List<Attribute> result = new ArrayList<Attribute>();
        List<TMPAttribute> tmpAts = tmp.getAllTMPAttributes();

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
                if (tmpAts != null)
                {
                    for (TMPAttribute tmpAtt : tmpAts)
                    {
                        if (tmpAtt.getAttributeName().equalsIgnoreCase(
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

    public static List<Attribute> getAvailableAttributes(long tmpid)
    {
        TranslationMemoryProfile tmp = HibernateUtil.get(
                TranslationMemoryProfile.class, tmpid);

        return getAvailableAttributes(tmp);
    }

    public static List<String> getAvailableAttributenames(
            TranslationMemoryProfile tmp)
    {
        List<Attribute> atts = getAvailableAttributes(tmp);
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

    public static String toOne(List<TMPAttribute> tmpas)
    {
        StringBuffer sb = new StringBuffer();

        if (tmpas != null)
        {
            for (TMPAttribute tmpa : tmpas)
            {
                String value = FilterHelper.escape(tmpa.getValueData());

                sb.append(tmpa.getAttributeName());
                sb.append(":").append(tmpa.getOperator());
                sb.append(":").append(tmpa.getValueType());
                sb.append(":").append(value);
                sb.append(":").append(tmpa.getOrder());
                sb.append(":").append(tmpa.getAndOr());
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

    public static void setTMPAttributes(TranslationMemoryProfile tmp,
            String oneStr) throws EnvoyServletException
    {
        // remove first
        try
        {
            List<TMPAttribute> tmpas = tmp.getAllTMPAttributes();
            if (tmpas != null && tmpas.size() > 0)
            {
                for (TMPAttribute tmpAttribute : tmpas)
                {
                    tmp.getAttributes().remove(tmpAttribute);
                    HibernateUtil.delete(tmpAttribute);
                }
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        // add
        Set<TMPAttribute> attSet = new HashSet<TMPAttribute>();

        if (oneStr != null)
        {
            String[] ooo = oneStr.split(",");
            if (ooo != null && ooo.length != 0)
            {
                for (int i = 0; i < ooo.length; i++)
                {
                    String o = ooo[i];
                    String[] temp = o.split(":");

                    if (temp != null && temp.length == 6)
                    {
                        TMPAttribute tmpa = new TMPAttribute();
                        tmpa.setAttributeName(temp[0]);
                        tmpa.setOperator(temp[1]);
                        tmpa.setValueType(temp[2]);
                        tmpa.setValueData(temp[3]);
                        tmpa.setOrder(Integer.parseInt(temp[4]));
                        tmpa.setAndOr(temp[5]);
                        tmpa.setTmprofile(tmp);

                        attSet.add(tmpa);
                    }
                }
            }
        }

        tmp.setAttributes(attSet);
    }

    public static List<String> getAvailableAttributenames(long tmpid)
    {
        TranslationMemoryProfile tmp = HibernateUtil.get(
                TranslationMemoryProfile.class, tmpid);

        return getAvailableAttributenames(tmp);
    }

    public static TMPAttribute getTMPAttribute(TranslationMemoryProfile tmp,
            String attname)
    {
        List<TMPAttribute> all = tmp.getAllTMPAttributes();
        if (all != null)
        {
            for (TMPAttribute tmpa : all)
            {
                if (tmpa.getAttributeName().equalsIgnoreCase(attname))
                {
                    return tmpa;
                }
            }
        }

        return null;
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
}
