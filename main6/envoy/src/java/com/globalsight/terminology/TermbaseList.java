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

package com.globalsight.terminology;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.ProjectTMTBUsers;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.util.comparator.TermbaseComparator;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * A singleton list of termbase objects known in GlobalSight.
 * </p>
 * 
 * <p>
 * This class is mostly package-private, use the official API in
 * {@link ITermbaseManager}.
 * </p>
 */
public class TermbaseList
{
    private static final Logger CATEGORY = Logger.getLogger(TermbaseList.class);

    static private HashMap s_termbases = new HashMap();

    /** Private constructor: this class is a singleton. */
    private TermbaseList()
    {
    }

    /**
     * Returns a sorted list of termbase names. The names are sorted based on
     * the given locale
     * 
     * @param p_uiLocale
     *            the UI locale to use for sorting
     * @return termbase names
     */
    static public ArrayList getNames(Locale p_uiLocale)
    {
        ArrayList result = getAllNames();

        StringComparator comp = new StringComparator(p_uiLocale);
        SortUtil.sort(result, comp);

        return result;
    }

    /*
     * Returns the termbase names.
     */
    static public ArrayList getNames()
    {
        ArrayList result = getAllNames();

        StringComparator comp = new StringComparator(Locale.US);
        SortUtil.sort(result, comp);

        return result;
    }

    static ArrayList getAllNames()
    {
        ArrayList result = null;

        synchronized (s_termbases)
        {
            String companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap companyMap = null;

            result = new ArrayList();

            if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                for (Iterator iter = s_termbases.values().iterator(); iter
                        .hasNext();)
                {
                    companyMap = (HashMap) iter.next();
                    result.addAll(companyMap.keySet());
                }
            }
            else
            {
                companyMap = (HashMap) s_termbases.get(companyId);
                if (companyMap == null)
                {
                    companyMap = new HashMap();
                    s_termbases.put(companyId, companyMap);
                }
                else
                {
                    result.addAll(companyMap.keySet());
                }
            }
        }

        return result;
    }

    static void add(String companyId, String name, Termbase object)
    {
        synchronized (s_termbases)
        {
            HashMap companyMap = (HashMap) s_termbases.get(companyId);
            if (companyMap == null)
            {
                companyMap = new HashMap();
            }
            companyMap.put(name, object);
            s_termbases.put(companyId, companyMap);
        }
    }

    static Termbase remove(String name)
    {
        Termbase result = null;

        synchronized (s_termbases)
        {
            String companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap companyMap = null;
            // Is supper user?
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                Iterator compIter = s_termbases.values().iterator();
                while (compIter.hasNext())
                {
                    companyMap = (HashMap) compIter.next();
                    if (companyMap.containsKey(name))
                    {
                        break;
                    }
                }
            }
            else
            // Is not supper user!
            {
                companyMap = (HashMap) s_termbases.get(companyId);
            }
            if (companyMap == null)
            {
                companyMap = new HashMap();
                s_termbases.put(companyId, companyMap);
            }

            result = (Termbase) companyMap.remove(name);
        }

        return result;
    }

    public static Termbase get(String name)
    {
        Termbase result = null;
        String companyId = null;

        synchronized (s_termbases)
        {
            companyId = CompanyThreadLocal.getInstance().getValue();
            HashMap companyMap = null;
            // Is super user?
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                for (Iterator iter = s_termbases.values().iterator(); iter
                        .hasNext();)
                {
                    companyMap = (HashMap) iter.next();
                    if (companyMap.containsKey(name))
                    {
                        break;
                    }
                }
            }
            else
            // Is not super user!
            {
                companyMap = (HashMap) s_termbases.get(companyId);
            }

            if (companyMap == null)
            {
                companyMap = new HashMap();
                s_termbases.put(companyId, companyMap);
            }

            result = (Termbase) companyMap.get(name);
        }

        return result;
    }

    public static Termbase get(String companyId, String name)
    {
        Termbase result = null;

        synchronized (s_termbases)
        {
            HashMap companyMap = null;
            // Is super user?
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                for (Iterator iter = s_termbases.values().iterator(); iter
                        .hasNext();)
                {
                    companyMap = (HashMap) iter.next();
                    if (companyMap.containsKey(name))
                    {
                        break;
                    }
                }
            }
            else
            // Is not super user!
            {
                companyMap = (HashMap) s_termbases.get(companyId);
            }

            if (companyMap == null)
            {
                companyMap = new HashMap();
                s_termbases.put(companyId, companyMap);
            }

            result = (Termbase) companyMap.get(name);
        }

        return result;
    }

    public static Termbase get(long id)
    {
        String companyId = null;
        HashMap companyMap = null;
        Termbase tb = null;

        synchronized (s_termbases)
        {
            companyId = CompanyThreadLocal.getInstance().getValue();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                loop: for (Iterator iter = s_termbases.values().iterator(); iter
                        .hasNext();)
                {
                    companyMap = (HashMap) iter.next();
                    for (Iterator it = companyMap.values().iterator(); it
                            .hasNext();)
                    {
                        Termbase termbase = (Termbase) it.next();
                        if (termbase.getId() == id)
                        {
                            tb = termbase;
                            break loop;
                        }
                    }
                }
            }
            else
            {
                companyMap = (HashMap) s_termbases.get(companyId);
                if (companyMap == null)
                {
                    companyMap = new HashMap();
                    s_termbases.put(companyId, companyMap);
                }
                for (Iterator iter = companyMap.values().iterator(); iter
                        .hasNext();)
                {
                    Termbase termbase = (Termbase) iter.next();
                    if (termbase.getId() == id)
                    {
                        tb = termbase;
                        break;
                    }
                }
            }
        }
        return tb;
    }

    public static TermbaseInfo getTermbaseInfo(long id)
    {
        String companyId = null;
        HashMap companyMap = null;
        Termbase tb = null;

        synchronized (s_termbases)
        {
            companyId = CompanyThreadLocal.getInstance().getValue();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                loop: for (Iterator iter = s_termbases.values().iterator(); iter
                        .hasNext();)
                {
                    companyMap = (HashMap) iter.next();
                    for (Iterator it = companyMap.values().iterator(); it
                            .hasNext();)
                    {
                        Termbase termbase = (Termbase) it.next();
                        if (termbase.getId() == id)
                        {
                            tb = termbase;
                            break loop;
                        }
                    }
                }
            }
            else
            {
                companyMap = (HashMap) s_termbases.get(companyId);
                if (companyMap == null)
                {
                    companyMap = new HashMap();
                    s_termbases.put(companyId, companyMap);
                }
                for (Iterator iter = companyMap.values().iterator(); iter
                        .hasNext();)
                {
                    Termbase termbase = (Termbase) iter.next();
                    if (termbase.getId() == id)
                    {
                        tb = termbase;
                        break;
                    }
                }
            }
        }
        TermbaseInfo tbi = new TermbaseInfo(tb.getId(), tb.getName(),
                tb.getDescription(), tb.getCompanyId());
        return tbi;
    }

    /**
     * Returns a list of termbase names and descriptions known to the server
     * sorted based on rules for the given locale.
     */
    static ArrayList getTermbases(Locale p_uiLocale)
    {
        ArrayList termbases;
        ArrayList termbaseInfos;

        synchronized (s_termbases)
        {
            termbases = new ArrayList();
            HashMap companyMap = null;

            String companyId = CompanyThreadLocal.getInstance().getValue();
            if (CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                for (Iterator iter = s_termbases.values().iterator(); iter
                        .hasNext();)
                {
                    companyMap = (HashMap) iter.next();
                    termbases.addAll(companyMap.values());
                }
            }
            else
            {
                companyMap = (HashMap) s_termbases.get(companyId);
                if (companyMap == null)
                {
                    companyMap = new HashMap();
                    s_termbases.put(companyId, companyMap);
                }
                termbases.addAll(companyMap.values());
            }
        }

        termbaseInfos = new ArrayList();

        for (int i = 0; i < termbases.size(); i++)
        {
            Termbase tb = (Termbase) termbases.get(i);
            TermbaseInfo tbi = new TermbaseInfo(tb.getId(), tb.getName(),
                    tb.getDescription(), tb.getCompanyId());
            termbaseInfos.add(tbi);
        }
        return termbaseInfos;
    }

    /**
     * Returns a list of termbase names and descriptions known to the server
     * sorted based on rules for the given locale.
     * 
     * @param p_uiLocale
     *            -- the UI locale to use for sorting
     * @return an XML string: <termbases> <termbase> <name>NAME</name>
     *         <description>DESC</description> </termbase> </termbases>
     */
    static String getDescriptions(Locale p_uiLocale, String p_userId,
            String p_companyId)
    {
        ArrayList termbases;

        synchronized (s_termbases)
        {
            termbases = new ArrayList();
            HashMap companyMap = null;

            if (CompanyWrapper.SUPER_COMPANY_ID.equals(p_companyId))
            {
                for (Iterator iter = s_termbases.values().iterator(); iter
                        .hasNext();)
                {
                    companyMap = (HashMap) iter.next();
                    termbases.addAll(companyMap.values());
                }
            }
            else
            {
                companyMap = (HashMap) s_termbases.get(p_companyId);
                if (companyMap == null)
                {
                    companyMap = new HashMap();
                    s_termbases.put(p_companyId, companyMap);
                }
                termbases.addAll(companyMap.values());
            }
        }

        StringBuffer result = new StringBuffer(256);
        StringBuffer temp = new StringBuffer(128);

        TermbaseComparator comp = new TermbaseComparator(
                TermbaseComparator.NAME, p_uiLocale);
        SortUtil.sort(termbases, comp);

        result.append("<termbases>");
        // Fixed for GBS-1688
        Company company = CompanyWrapper.getCompanyById(p_companyId);
        // TB Access Control enable or disable
        boolean enableTBAccessControl = company.getEnableTBAccessControl();
        boolean isAdmin = true;
        boolean isSuperAdmin = true;
        if (p_userId != null)
        {
            isAdmin = UserUtil.isInPermissionGroup(p_userId, "Administrator");
            isSuperAdmin = UserUtil.isSuperAdmin(p_userId);
        }
        ArrayList<Long> tbList = new ArrayList<Long>();
        if (enableTBAccessControl && !isAdmin && !isSuperAdmin)
        { // get the tb list for this user
            ProjectTMTBUsers ptu = new ProjectTMTBUsers();
            Iterator it = ((ArrayList) ptu.getTList(p_userId, "TB")).iterator();
            while (it.hasNext())
            {
                long tmId = ((BigInteger) it.next()).longValue();
                tbList.add(tmId);
            }
        }
        for (int i = 0; i < termbases.size(); i++)
        {
            try
            {
                Termbase tb = (Termbase) termbases.get(i);
                long tbId = tb.getId();
                if (!tb.getCompanyId().equals(p_companyId))
                {
                    continue;
                }
                // TB Access Control is enable
                if (enableTBAccessControl && !isAdmin && !isSuperAdmin
                        && !tbList.contains(tbId))
                {
                    continue;
                }
                temp.setLength(0);

                temp.append("<termbase id='");
                temp.append(tb.getId());
                temp.append("'>");
                temp.append("<name>");
                temp.append(EditUtil.encodeXmlEntities(tb.getName()));
                temp.append("</name>");
                temp.append("<description>");
                temp.append(EditUtil.encodeXmlEntities(tb.getDescription()));
                temp.append("</description>");
                temp.append("</termbase>");

                result.append(temp.toString());
            }
            catch (Exception ignore)
            {
            }
        }

        result.append("</termbases>");

        return result.toString();
    }
}
