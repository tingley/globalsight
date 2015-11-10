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
package com.globalsight.everest.company;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * This class only represent a wrapper object for the company names defined in
 * Envoy database and is used for defining a workflow template (template node
 * names).
 */
public class CompanyWrapper
{
    public static final String SUPER_COMPANY_ID = "1";

    public static final String COMPANY_ATTR = "m_companyId";

    public static final String COPMANY_ID_ARG = "companyIdArg";

    public static final String COPMANY_ID_START_ARG = "companyIdStartArg";

    public static final String COPMANY_ID_END_ARG = "companyIdEndArg";

    public static final String CURRENT_COMPANY_ID = "currentCompanyId";

    private static Company superCompany;
    private static HashMap<String, String> id2NameMap = new HashMap<String, String>();
    private static HashMap<String, String> name2IdMap = new HashMap<String, String>();

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Default Company constructor used ONLY for TopLink.
     */
    public CompanyWrapper()
    {
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Get all the companies' names, ignore isActive checking
     * 
     * @return The companies' names.
     */
    public static String[] getAllCompanyNames() throws PersistenceException
    {
        String[] strCompanyNames = null;
        String hql = "from Company c where c.isActive = 'Y'";
        Collection<?> col = HibernateUtil.search(hql);
        Set<String> companyNames = new TreeSet<String>();
        for (Iterator<?> iter = col.iterator(); iter.hasNext();)
        {
            Company company = (Company) iter.next();
            companyNames.add(company.getCompanyName());
        }
        strCompanyNames = new String[companyNames.size()];
        companyNames.toArray(strCompanyNames);

        return strCompanyNames;
    }

    public static HashMap<String, String> getAllCompanyRefer()
            throws PersistenceException
    {
        String hql = "from Company c where c.isActive = 'Y'";
        id2NameMap = new HashMap<String, String>();

        Collection<?> col = HibernateUtil.search(hql);
        for (Iterator<?> iter = col.iterator(); iter.hasNext();)
        {
            Company company = (Company) iter.next();

            id2NameMap.put(Long.toString(company.getId()),
                    company.getCompanyName());
        }

        return id2NameMap;
    }

    public static Vector<Long> addCompanyIdBoundArgs(Vector<Long> args)
            throws PersistenceException
    {
        String currentId = CompanyThreadLocal.getInstance().getValue();

        if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            Long[] bounds = CompanyWrapper.getCompanyIdBound();
            args.add(bounds[0]);
            args.add(bounds[1]);
        }
        else
        {
            args.add(new Long(currentId));
            args.add(new Long(currentId));
        }

        return args;
    }

    public static HashMap<String, Long> addCompanyIdBoundArgs(String mapKey1,
            String mapKey2) throws PersistenceException
    {
        String currentId = CompanyThreadLocal.getInstance().getValue();
        HashMap<String, Long> map = new HashMap<String, Long>();

        if (CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            Long[] bounds = CompanyWrapper.getCompanyIdBound();
            map.put(mapKey1, bounds[0]);
            map.put(mapKey2, bounds[1]);
        }
        else
        {
            map.put(mapKey1, new Long(currentId));
            map.put(mapKey2, new Long(currentId));
        }
        return map;
    }

    private static Long[] getCompanyIdBound() throws PersistenceException
    {
        Long[] bounds = new Long[2];
        bounds[0] = new Long(-1);
        String maxCompanySql = "select max(id) from COMPANY";
        Number maxId = (Number) HibernateUtil.getFirstWithSql(maxCompanySql);
        bounds[1] = new Long(maxId.longValue());

        return bounds;
    }

    public static String getCompanyNameById(long id)
    {
        return getCompanyNameById(String.valueOf(id));
    }

    public static String getCompanyNameById(String id)
    {
        String name = id2NameMap.get(id);
        if (name == null)
        {
            getAllCompanyRefer();
        }

        name = id2NameMap.get(id);
        if (name == null)
        {
            return "Null Company";
        }
        else
        {
            return name;
        }
    }

    public static String getCompanyIdByName(String strName)
            throws PersistenceException
    {
    	strName = strName.toUpperCase();
    	String id = name2IdMap.get(strName);
    	if (id == null)
    	{
    		getAllCompanyNameIdMap();
    	}

    	return name2IdMap.get(strName);
    }

    private static HashMap<String, String> getAllCompanyNameIdMap()
    {
        String hql = "from Company c where c.isActive = 'Y'";
        name2IdMap = new HashMap<String, String>();

        Collection<?> col = HibernateUtil.search(hql);
        for (Iterator<?> iter = col.iterator(); iter.hasNext();)
        {
            Company company = (Company) iter.next();
			name2IdMap.put(company.getCompanyName().toUpperCase(),
					String.valueOf(company.getId()));
        }

        return name2IdMap;
    }

    /**
     * Get a Company instance with company name
     * 
     * @param strName
     *            not null able
     * @return a Company instance which name is same as {@link strName}, return
     *         null if this company name does not exist or isActive is false for
     *         that company.
     * @throws PersistenceException
     */
    public static Company getCompanyByName(String strName)
            throws PersistenceException
    {
        try
        {
            return ServerProxy.getJobHandler()
                    .getCompany(strName.toUpperCase());
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
    }

    public static Company getCompanyById(long id)
    {
        return getCompanyById(String.valueOf(id));
    }

    public static Company getCompanyById(String id) throws PersistenceException
    {
        Company company = null;

        try
        {
            company = ServerProxy.getJobHandler().getCompanyById(
                    Long.parseLong(id));
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }

        return company;
    }

    public static String getCurrentCompanyName()
    {
        return CompanyWrapper.getCompanyNameById(CompanyWrapper
                .getCurrentCompanyId());
    }

    public static String getCurrentCompanyId()
    {
        return CompanyThreadLocal.getInstance().getValue();
    }

    public static Company getCurrentCompany()
    {
        return getCompanyById(getCurrentCompanyId());
    }

    public static long getCurrentCompanyIdAsLong()
    {
        return Long.parseLong(getCurrentCompanyId());
    }

    public static String getSuperCompanyName()
    {
        if (superCompany == null)
        {
            superCompany = getCompanyById(SUPER_COMPANY_ID);
        }

        return superCompany.getName();
    }

    public static boolean isSuperCompanyName(String companyName)
    {
        return getSuperCompanyName().equalsIgnoreCase(companyName);
    }

    public static boolean isSuperCompany(String id)
    {
        return isSuperCompanyName(getCompanyNameById(id));
    }

    public static void saveCurrentCompanyIdInMap(Map<String, String> map,
            Logger logger)
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (logger != null && logger.isDebugEnabled())
        {
            logger.debug("Current company id get from ThreadLocal is: "
                    + companyId);
        }
        if (map != null)
        {
            map.put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);
        }
    }

    public static List<String> getCompanyCategoryList(String companyId)
    {
        String hql = "select c.category from Category as c where c.isActive = 'Y' and c.companyId = "
                + companyId;
        List<String> categoryList = (List<String>) HibernateUtil.search(hql);

        if (categoryList == null || categoryList.size() == 0)
        {
            String[] keyArray = new String[]
            { "lb_conflicts_glossary_guide", "lb_formatting_error",
                    "lb_mistranslated", "lb_omission_of_text",
                    "lb_spelling_grammar_punctuation_error" };
            categoryList = Arrays.asList(keyArray);
        }

        return categoryList;
    }

    public static List<String> getCompanyScorecardCategoryList(String companyId)
    {
        String hql = "select s.scorecardCategory from ScorecardCategory as s where s.isActive = 'Y' and s.companyId = "
                + companyId;
        List<String> scorecardCategoryList = (List<String>) HibernateUtil
                .search(hql);

        if (scorecardCategoryList == null || scorecardCategoryList.size() == 0)
        {
            String[] keyArray = new String[]
            { "lb_spelling_grammar", "lb_consistency", "lb_style",
                    "lb_terminology", };
            scorecardCategoryList = Arrays.asList(keyArray);
        }

        return scorecardCategoryList;
    }

    public static List<String> getCompanyQualityCategoryList(String companyId)
    {
        String hql = "select q.categoryName from PostReviewCategory as q where q.isActive = 'Y' and q.categoryType ='Q' and q.companyId = "+companyId;
        List<String> qualityCategoryList = (List<String>)HibernateUtil.search(hql);
        if (qualityCategoryList == null || qualityCategoryList.size() == 0)
        {
            String[] keyArray = new String[]
            { "lb_good", "lb_acceptable", "lb_poor",};
            qualityCategoryList = Arrays.asList(keyArray);
        }
        return qualityCategoryList;
    }

    public static List<String> getCompanyMarketCategoryList(String companyId)
    {
        String hql = "select q.categoryName from PostReviewCategory as q where q.isActive = 'Y' and q.categoryType ='M' and q.companyId = "+companyId;
        List<String> marketCategoryList = (List<String>)HibernateUtil.search(hql);
        if (marketCategoryList == null || marketCategoryList.size() == 0)
        {
            String[] keyArray = new String[]
            { "lb_suitable_fluent", "lb_literal_at_times", "lb_unsuitable",};
            marketCategoryList = Arrays.asList(keyArray);
        }
        return marketCategoryList;
    }
    
    public static List<String> getCompanyCategoryAvailList(String companyId)
    {
        String hql = "select c.category from Category as c where c.isActive = 'N' and c.companyId = "
                + companyId;
        List<String> availableCategoryList = (List<String>) HibernateUtil.search(hql);
        
        return availableCategoryList;
    }
    
    public static List<String> getCompanyScorecardCategoryAvailList(String companyId)
    {
        String hql = "select s.scorecardCategory from ScorecardCategory as s where s.isActive = 'N' and s.companyId = "
                + companyId;
        List<String> availableScorecardCategoryList = (List<String>) HibernateUtil
                .search(hql);
        
        
        return availableScorecardCategoryList;
    }
    
    public static List<String> getCompanyQualityCategoryAvailList(String companyId)
    {
        String hql = "select q.categoryName from PostReviewCategory as q where q.isActive = 'N' and q.categoryType ='Q' and q.companyId = "+companyId;
        List<String> availableQualityCategoryList = (List<String>)HibernateUtil.search(hql);
        return availableQualityCategoryList;
    }
    
    public static List<String> getCompanyMarketCategoryAvailList(String companyId)
    {
        String hql = "select q.categoryName from PostReviewCategory as q where q.isActive = 'N' and q.categoryType ='M' and q.companyId = "+companyId;
        List<String> availableMarketCategoryList = (List<String>)HibernateUtil.search(hql);
        return availableMarketCategoryList;
    }

}
