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

package com.globalsight.everest.webapp.pagehandler.administration.config.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.action.AttributeAction;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.action.InternationalCostCenterAction;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class AttributeManager
{
    private static final Logger logger = Logger
            .getLogger(AttributeManager.class);

    private static List<AttributeAction> attributeActions = null;

    public static List<?> getAllAttributes()
    {
        String hql = "from Attribute a where a.companyId = 1";
        HashMap<String, Long> map = new HashMap<String, Long>();

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " or a.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return HibernateUtil.search(hql, map);
    }

    public static List<?> getAllAttributeSets()
    {
        String hql = "from AttributeSet a where a.companyId = 1";
        HashMap<String, Long> map = new HashMap<String, Long>();

        String currentId = CompanyThreadLocal.getInstance().getValue();
        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            hql += " or a.companyId = :companyId";
            map.put("companyId", Long.parseLong(currentId));
        }

        return HibernateUtil.search(hql, map);
    }

    public static AttributeSet getAttributeSetByName(String name)
    {
        String currentId = CompanyThreadLocal.getInstance().getValue();
        String hql = "from AttributeSet a where a.name = :name and a.companyId = :companyId";
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("companyId", Long.parseLong(currentId));

        return (AttributeSet) HibernateUtil.getFirst(hql, map);
    }

    public static Attribute getAttributeByDisplayName(String name)
    {
        String currentId = CompanyThreadLocal.getInstance().getValue();
        String hql = "from Attribute a where a.displayName = :name and a.companyId = :companyId";
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("companyId", Long.parseLong(currentId));

        return (Attribute) HibernateUtil.getFirst(hql, map);
    }

    public static Attribute getAttributeByName(String name)
    {
        String hql = "from Attribute a where a.name = :name";
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("name", name);

        return (Attribute) HibernateUtil.getFirst(hql, map);
    }

    public static boolean isExistAttributeName(String name, String id)
    {
        Attribute attribute = getAttributeByName(name);
        return attribute != null
                && (id == null || Long.parseLong(id) != attribute.getId());
    }

    public static boolean isExistAttributeGroupName(String name, String id)
    {
        AttributeSet attributeSet = getAttributeSetByName(name);
        return attributeSet != null
                && (id == null || Long.parseLong(id) != attributeSet.getId());
    }

    public static Set<Attribute> getAttributesByFileProfile(
            FileProfile fileProfile)
    {
        if (fileProfile != null && fileProfile.getId() > 0)
        {
            L10nProfile lp;
            try
            {
                lp = ServerProxy.getProjectHandler().getL10nProfile(
                        fileProfile.getL10nProfileId());
                Project p = lp.getProject();

                /**
                 * Edited by Vincent. 2010-04-16. return
                 * p.getAttributeSet().getAttributes();
                 */
                AttributeSet set = p.getAttributeSet();
                if (set != null)
                    return set.getAttributes();
                else
                    return new HashSet<Attribute>();
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
            }
        }

        return new HashSet<Attribute>();
    }

    public static List<AttributeAction> getAttributeActions()
    {
        if (attributeActions == null)
        {
            attributeActions = new ArrayList<AttributeAction>();
            attributeActions.add(new InternationalCostCenterAction());
        }

        return attributeActions;
    }
}
