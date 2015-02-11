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

package com.globalsight.everest.tm;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.util.comparator.TmComparator;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>
 * A singleton list of TM objects known in System 4.
 * </p>
 */
public class TmList
{
    private static final Logger CATEGORY = Logger.getLogger(TmList.class);

    static private Hashtable s_tms = new Hashtable();

    /** Private constructor: this class is a singleton. */
    private TmList()
    {
    }

    /**
     * Returns a sorted list of TM names. The names are sorted based on the
     * given locale.
     * 
     * @param p_uiLocale
     *            the UI locale to use for sorting
     * @return tm names
     */
    static public ArrayList getNames(Locale p_uiLocale)
    {
        ArrayList result;

        synchronized (s_tms)
        {
            result = new ArrayList(s_tms.keySet());
        }

        StringComparator comp = new StringComparator(p_uiLocale);
        SortUtil.sort(result, comp);

        return result;
    }

    /**
     * Returns a list of TM names. The names are sorted in English.
     */
    static public ArrayList getNames()
    {
        ArrayList result;

        synchronized (s_tms)
        {
            result = new ArrayList(s_tms.keySet());
        }

        StringComparator comp = new StringComparator(Locale.US);
        SortUtil.sort(result, comp);

        return result;
    }

    static public void add(String name, Tm object)
    {
        synchronized (s_tms)
        {
            s_tms.put(name, object);
        }
    }

    static public TmImpl remove(String name)
    {
        TmImpl result;

        synchronized (s_tms)
        {
            result = (TmImpl) s_tms.remove(name);
        }

        return result;
    }

    static public TmImpl get(String name)
    {
        TmImpl result;

        synchronized (s_tms)
        {
            result = (TmImpl) s_tms.get(name);
        }

        return result;
    }

    static public TmImpl get(long id)
    {
        Enumeration it;

        synchronized (s_tms)
        {
            it = s_tms.elements();
        }

        while (it.hasMoreElements())
        {
            TmImpl tm = (TmImpl) it.nextElement();

            if (tm.getId() == id)
            {
                return tm;
            }
        }

        return null;
    }

    /**
     * Returns an Arraylist of all TMs in the system.
     */
    static public ArrayList getAll()
    {
        ArrayList result;

        synchronized (s_tms)
        {
            result = new ArrayList(s_tms.values());
        }

        return result;
    }

    /**
     * Returns a list of TM names and descriptions known to the server sorted
     * based on rules for the given locale.
     * 
     * @param p_uiLocale
     *            -- the UI locale to use for sorting
     * @return an XML string: <tms> <tm> <name>NAME</name> <domain>DESC</domain>
     *         <organization>DESC</organization> <description>DESC</description>
     *         </tm> </tms>
     */
    static public synchronized String getDescriptions(Locale p_uiLocale)
    {
        ArrayList tms;

        synchronized (s_tms)
        {
            tms = new ArrayList(s_tms.values());
        }

        StringBuffer result = new StringBuffer(256);

        TmComparator comp = new TmComparator(TmComparator.NAME, p_uiLocale);
        SortUtil.sort(tms, comp);

        result.append("<tms>");

        for (int i = 0; i < tms.size(); i++)
        {
            TmImpl tm = (TmImpl) tms.get(i);

            result.append(tmAsXml(tm, false));
        }

        result.append("</tms>");

        return result.toString();
    }

    static public String tmAsXml(TmImpl p_tm, boolean p_clone)
    {
        StringBuffer result = new StringBuffer();

        if (!p_clone)
        {
            result.append("<tm id='");
            result.append(p_tm.getId());
            result.append("'>");
        }
        else
        {
            result.append("<tm>");
        }
        result.append("<name>");
        if (!p_clone)
        {
            result.append(EditUtil.encodeXmlEntities(p_tm.getName()));
        }
        result.append("</name>");
        result.append("<domain>");
        result.append(EditUtil.encodeXmlEntities(p_tm.getDomain()));
        result.append("</domain>");
        result.append("<organization>");
        result.append(EditUtil.encodeXmlEntities(p_tm.getOrganization()));
        result.append("</organization>");
        result.append("<description>");
        result.append(EditUtil.encodeXmlEntities(p_tm.getDescription()));
        result.append("</description>");
        result.append("</tm>");

        return result.toString();
    }
}
