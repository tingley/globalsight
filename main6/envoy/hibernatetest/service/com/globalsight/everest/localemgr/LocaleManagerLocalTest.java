package com.globalsight.everest.localemgr;

import java.rmi.RemoteException;
import java.util.*;

import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.localemgr.CodeSetImpl;

import junit.framework.TestCase;

public class LocaleManagerLocalTest extends TestCase
{

    LocaleManagerLocal local = new LocaleManagerLocal();
    
    public void setUp()
    {
        CompanyThreadLocal tl = CompanyThreadLocal.getInstance();
        tl.setIdValue("1001");
    }
    
    public void testAddLocale() throws LocaleManagerException, RemoteException
    {
        GlobalSightLocale p_locale = new GlobalSightLocale();
        p_locale.setCountry("TW");
        p_locale.setLanguage("vi");
        local.addLocale(p_locale);
        System.out.println(p_locale.getId());
    }
    
    public void testGetAvailableLocales() throws LocaleManagerException, RemoteException
    {
        Vector locales = local.getAvailableLocales();
        System.out.println(locales.size());
    }
    
    public void testGetLocaleById() throws LocaleManagerException, RemoteException
    {
        GlobalSightLocale l = local.getLocaleById(1);
        System.out.println(l.getDisplayName());
    }
    
    public void testGetLocaleByString()throws LocaleManagerException, RemoteException
    {
        String name = "tr_TR";
        GlobalSightLocale l = local.getLocaleByString(name);
        System.out.println(l.getId());
    }
    
    public void testGetAllSourceLocalesByCompanyId() throws LocaleManagerException, RemoteException
    {
        String id = "1001";

        Vector locales = local.getAllSourceLocalesByCompanyId(id);

        for (int i = 0; i < locales.size(); i++)
        {
            GlobalSightLocale locale = (GlobalSightLocale)locales.get(i);
            System.out.println(locale.getDisplayName());
        }
    }
    
    public void testGetAllSourceLocales() throws LocaleManagerException, RemoteException
    {
        Vector locales = local.getAllSourceLocales();

        for (int i = 0; i < locales.size(); i++)
        {
            GlobalSightLocale locale = (GlobalSightLocale)locales.get(i);
            System.out.println(locale.getDisplayName());
        }
    }
    
    public void testGetAllTargetLocales() throws LocaleManagerException, RemoteException
    {
        Vector locales = local.getAllTargetLocales();

        for (int i = 0; i < locales.size(); i++)
        {
            GlobalSightLocale locale = (GlobalSightLocale)locales.get(i);
            System.out.println(locale.getDisplayName());
        }
    }
    
    public void testGetSourceTargetLocalePairs() throws LocaleManagerException, RemoteException
    {
        Vector localePairs = local.getSourceTargetLocalePairs();
        for (int i = 0; i < localePairs.size(); i++)
        {
            LocalePair locale = (LocalePair)localePairs.get(i);
            System.out.println(locale.getId());
        }
        
        System.out.println(localePairs.size());
    }
    
    public void testGetLocalePairById() throws LocaleManagerException, RemoteException
    {
        LocalePair pair = local.getLocalePairById(1001);     
        System.out.println(pair.getTarget().getId());
    }
    
    public void testGetLocalePairBySourceTargetIds() throws LocaleManagerException, RemoteException
    {
        LocalePair pair = local.getLocalePairBySourceTargetIds(32, 41);
        System.out.println(pair.getId());
    }
    
    public void testGetLocalePairBySourceTargetStrings() throws LocaleManagerException, RemoteException
    {
        LocalePair pair = local.getLocalePairBySourceTargetStrings("en_US", "es_ES");
        System.out.println(pair.getId());
    }
    
    public void testGetTargetLocalesByCompanyId() throws LocaleManagerException, RemoteException
    {
        GlobalSightLocale l = new GlobalSightLocale();
        l.setId(32);
        Vector locales = local.getTargetLocalesByCompanyId(l, "1001");
        for (int i = 0; i < locales.size(); i++)
        {
            GlobalSightLocale tl = (GlobalSightLocale)locales.get(i);
            System.out.println(tl.getDisplayName());
        }
    }
    
    public void testGetTargetLocales() throws LocaleManagerException, RemoteException
    {
        GlobalSightLocale l = new GlobalSightLocale();
        l.setId(32);
        Vector locales = local.getTargetLocales(l);
        for (int i = 0; i < locales.size(); i++)
        {
            GlobalSightLocale tl = (GlobalSightLocale)locales.get(i);
            System.out.println(tl.getDisplayName());
        }
    }
    
    public void testAddSourceTargetLocalePair() throws Exception, RemoteException
    {
        GlobalSightLocale sl = new GlobalSightLocale();
        sl.setId(2);
        GlobalSightLocale tl = new GlobalSightLocale();
        tl.setId(2);
        String compayId = "1";
        local.addSourceTargetLocalePair(sl, tl,compayId);
    }
    
    public void testGetAllCodeSets() throws LocaleManagerException, RemoteException
    {
        Vector codeSets = local.getAllCodeSets();
        System.out.println(codeSets.size());
        for (int i = 0; i < codeSets.size(); i++)
        {
            CodeSetImpl code = (CodeSetImpl)codeSets.get(i);
            System.out.println(code.getCodeSet());
        }
    }
    
    public void testgetAllCodeSets() throws LocaleManagerException, RemoteException
    {
        Vector codeSets = local.getAllCodeSets(81);
        System.out.println(codeSets.size());
        for (int i = 0; i < codeSets.size(); i++)
        {
            CodeSetImpl code = (CodeSetImpl)codeSets.get(i);
            System.out.println(code.getCodeSet());
        }
    }
}
