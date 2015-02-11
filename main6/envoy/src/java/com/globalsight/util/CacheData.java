package com.globalsight.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class CacheData
{
    private static HashMap<String, String> LocaleDisplayName = new HashMap<String, String>();
    
    public static String getLocaleDisplayNameById(String id, Locale uiLocale)
    {
        String name = LocaleDisplayName.get(id);
        if (name == null) {
            queryGlobalSightLocale(uiLocale);
        }
        name = LocaleDisplayName.get(id)==null ? "Null Locale" : LocaleDisplayName.get(id);
        return name;
    }

    private static void queryGlobalSightLocale(Locale uiLocale)
    {
        String hql = "from GlobalSightLocale";
        Collection<?> col = HibernateUtil.search(hql);
        for (Iterator<?> iter = col.iterator(); iter.hasNext();)
        {
            GlobalSightLocale locale = (GlobalSightLocale) iter.next();
            LocaleDisplayName.put(Long.toString(locale.getId()), locale.getDisplayName(uiLocale));
        }
    }
}
