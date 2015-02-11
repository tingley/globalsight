package com.globalsight.dispatcher.dao;

import java.util.List;

import com.globalsight.dispatcher.bo.GlobalSightLocale;
import com.globalsight.dispatcher.bo.MTPLanguage;
import com.globalsight.dispatcher.bo.MachineTranslationProfile;
import com.globalsight.dispatcher.dao.MTPLanguagesDAO;

public class MTPLanguagesDAOTest
{
    static MTPLanguagesDAO langDao;
    static MTProfilesDAO mtpDAP;
    
    public static void main(String[] args)
    {
        mtpDAP = new MTProfilesDAO("E:/Data/temp/000/");
        langDao = new MTPLanguagesDAO("E:/Data/temp/000/");
//        testGetAllGlobalSightLocale();
        testSaveOrUpdateLanguage();
    }

    public static void testSaveOrUpdateLanguage(){
        List<GlobalSightLocale> locales = CommonDAO.getAllGlobalSightLocale();
        GlobalSightLocale locale_en_US = locales.get(36);
        GlobalSightLocale locale_zh_CN = locales.get(22);
        MachineTranslationProfile profile = mtpDAP.getMTProfile(0);
        
        MTPLanguage lang = new MTPLanguage("AA", locale_en_US, locale_zh_CN, profile);
//        langDao.saveOrUpdateLanguage(lang);
        
        lang = langDao.getMTPLanguage(0);
        profile = mtpDAP.getMTProfile(2);
        lang.setMtProfile(profile);
        langDao.saveOrUpdateMTPLanguage(lang);
        
    }
    
    public static void testGetAllGlobalSightLocale()
    {
        List<GlobalSightLocale> locales = CommonDAO.getAllGlobalSightLocale();
        for (GlobalSightLocale gl : locales)
        {
            long id = gl.getId();
            System.out.println((id < 10 ? "0" + id : id) + "   " + gl + "\t" + gl.getDisplayName());
        }
    }
}
