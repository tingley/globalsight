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
package com.globalsight.everest.foundation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class SSOUserUtil implements Serializable
{
    public static boolean isCompanyEnableSSO(String companyName)
    {
        if (companyName == null)
        {
            return false;
        }

        Company c = CompanyWrapper.getCompanyByName(companyName);

        if (c != null)
        {
            return c.getEnableSSOLogin();
        }
        else
        {
            return false;
        }
    }

    public static void saveUserMapping(long p_companyId, String p_userId,
            String p_ssoUserId)
    {
        SSOUserMapping result = null;
        SSOUserMapping ori = getUserMapping(p_companyId, p_userId);

        if (ori != null)
        {
            result = ori;
        }
        else
        {
            result = new SSOUserMapping();
        }

        result.setCompanyId(p_companyId);
        result.setUserId(p_userId);
        result.setSsoUserId(p_ssoUserId);

        HibernateUtil.saveOrUpdate(result);
    }

    public static void deleteUserMapping(long p_companyId, String p_userId)
            throws Exception
    {
        SSOUserMapping result = null;
        SSOUserMapping ori = getUserMapping(p_companyId, p_userId);

        if (ori != null)
        {
            HibernateUtil.delete(result);
        }
    }

    public static SSOUserMapping getUserMapping(User p_user)
    {
        SSOUserMapping result = null;

        String companyName = p_user.getCompanyName();
        Company c = CompanyWrapper.getCompanyByName(companyName);
        long companyId = c.getId();
        String userName = p_user.getUserId();

        result = getUserMapping(companyId, userName);

        return result;
    }

    public static SSOUserMapping getUserMapping(long p_companyId,
            String p_userId)
    {
        SSOUserMapping result = null;

        String hql = "from SSOUserMapping ss where ss.companyId = :companyId and lower(ss.userId) = :userId";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("companyId", p_companyId);
        map.put("userId", p_userId.toLowerCase());

        Iterator<?> it = HibernateUtil.search(hql, map).iterator();
        if (it.hasNext())
        {
            result = (SSOUserMapping) it.next();
        }

        return result;
    }

    public static SSOUserMapping getUserMappingBySSOUser(long p_companyId,
            String p_ssoUserId)
    {
        SSOUserMapping result = null;

        if (p_ssoUserId == null || "".equals(p_ssoUserId.trim()))
        {
            return result;
        }

        String hql = "from SSOUserMapping ss where ss.companyId = :companyId and lower(ss.ssoUserId) = :ssoUserId";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("companyId", p_companyId);
        map.put("ssoUserId", p_ssoUserId.toLowerCase());

        Iterator<?> it = HibernateUtil.search(hql, map).iterator();
        if (it.hasNext())
        {
            result = (SSOUserMapping) it.next();
        }

        return result;
    }

    public static List<SSOUserMapping> getUserMappingBySSOUser(
            String p_ssoUserId)
    {
        List<SSOUserMapping> result = new ArrayList<SSOUserMapping>();

        if (p_ssoUserId == null || "".equals(p_ssoUserId.trim()))
        {
            return result;
        }

        String hql = "from SSOUserMapping ss where lower(ss.ssoUserId) = :ssoUserId";
        Map<String, String> map = new HashMap<String, String>();
        map.put("ssoUserId", p_ssoUserId.toLowerCase());

        Iterator<?> it = HibernateUtil.search(hql, map).iterator();
        while (it.hasNext())
        {
            result.add((SSOUserMapping) it.next());
        }

        return result;
    }

    public static List<SSOUserMapping> getSSOUserIdRepeat(long p_companyId,
            String p_ssoUserId)
    {
        List<SSOUserMapping> result = new ArrayList<SSOUserMapping>();

        if (p_ssoUserId == null || "".equals(p_ssoUserId.trim()))
        {
            return result;
        }

        String hql = "from SSOUserMapping ss where ss.companyId = :companyId and lower(ss.ssoUserId) = :ssoUserId";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("companyId", p_companyId);
        map.put("ssoUserId", p_ssoUserId.toLowerCase());

        Iterator<?> it = HibernateUtil.search(hql, map).iterator();
        while (it.hasNext())
        {
            result.add((SSOUserMapping) it.next());
        }

        return result;
    }

    public static Company getCompanyByIdpUrl(String idpUrl)
    {
        Company result = null;

        if (idpUrl == null || "".equals(idpUrl.trim()))
        {
            return result;
        }

        String hql = "from Company c where c.isActive = 'Y' and lower(c.ssoIdpUrl) = :ssoIdpUrl";
        Map<String, String> map = new HashMap<String, String>();
        map.put("ssoIdpUrl", idpUrl.toLowerCase());

        Iterator<?> it = HibernateUtil.search(hql, map).iterator();
        if (it.hasNext())
        {
            result = (Company) it.next();
        }

        return result;
    }
}
