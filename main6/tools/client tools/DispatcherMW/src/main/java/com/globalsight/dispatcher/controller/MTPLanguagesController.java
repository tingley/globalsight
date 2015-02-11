/**
 *  Copyright 2013 Welocalize, Inc. 
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
package com.globalsight.dispatcher.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.globalsight.dispatcher.bo.AppConstants;
import com.globalsight.dispatcher.bo.EngineEnum;
import com.globalsight.dispatcher.bo.GlobalSightLocale;
import com.globalsight.dispatcher.bo.MTPLanguage;
import com.globalsight.dispatcher.bo.MachineTranslationProfile;
import com.globalsight.dispatcher.dao.AccountDAO;
import com.globalsight.dispatcher.dao.CommonDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;
import com.globalsight.dispatcher.dao.MTPLanguagesDAO;
import com.globalsight.dispatcher.dao.MTProfilesDAO;
import com.globalsight.everest.projecthandler.MachineTranslationExtentInfo;
import com.globalsight.machineTranslation.iptranslator.IPTranslatorUtil;

/**
 * Dispatcher Controller for 'Languages' Pages
 * 
 * @author Joey
 *
 */
@Controller
@RequestMapping("/mtpLanguages")
public class MTPLanguagesController implements AppConstants
{
    private static final Logger logger = Logger.getLogger(MTPLanguagesController.class);
    MTPLanguagesDAO mtpLangDAO = DispatcherDAOFactory.getMTPLanguagesDAO();
    MTProfilesDAO mtProfileDAO = DispatcherDAOFactory.getMTPRofileDAO();
    AccountDAO accountDAO = DispatcherDAOFactory.getAccountDAO();
    
    @RequestMapping(value = "/main")
    public String listAllMTProfiles(ModelMap p_model)
    {
        p_model.put("mtpLanguages", mtpLangDAO.getMTPLanguages());
        return "mtpLanguagesMain";
    }

    @RequestMapping(value = "/viewDetail", method = RequestMethod.POST)
    public String viewDetail(@RequestParam Map<String, String> p_reqMap, ModelMap p_model) 
            throws FileNotFoundException, JAXBException
    {
        MTPLanguage mtpLang = new MTPLanguage();
        String idStr = p_reqMap.get("mtpLangID");
        if (idStr != null && idStr.trim().length() > 0)
        {
            long id = Long.valueOf(idStr.trim());
            if (id >= 0)
                mtpLang = mtpLangDAO.getMTPLanguage(id);
        }

        p_model.put("mtpLanguage", mtpLang);
        p_model.put("allGlobalSightLocale", CommonDAO.getAllGlobalSightLocale());
        p_model.put("allMTProfiles", mtProfileDAO.getAllMTProfiles());
        p_model.put("allAccounts", accountDAO.getAllAccounts());        
        return "mtpLanguagesDetail";
    }
    
    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    public String saveOrUpdate(@RequestParam Map<String, String> p_reqMap, ModelMap p_model) 
            throws FileNotFoundException, JAXBException
    {
        String mtpLangIDStr = p_reqMap.get("mtpLangID");
        String mtpLangName = p_reqMap.get("mtpLangName");
        String accountId = p_reqMap.get(JSONPN_ACCOUNT_ID);
        long mtpLangSrcLocaleID = Long.valueOf(p_reqMap.get("mtpLangSrcLocaleID"));
        long mtpLangTrgLocaleID = Long.valueOf(p_reqMap.get("mtpLangTrgLocaleID"));
        long mtProfileID = Long.valueOf(p_reqMap.get("mtProfileID"));
        
        GlobalSightLocale srcLocale = CommonDAO.getGlobalSightLocaleById(mtpLangSrcLocaleID);
        GlobalSightLocale trgLocale = CommonDAO.getGlobalSightLocaleById(mtpLangTrgLocaleID);
        MachineTranslationProfile mtProfile = mtProfileDAO.getMTProfile(mtProfileID);
        MTPLanguage mtpLang = new MTPLanguage(mtpLangName, Long.valueOf(accountId), srcLocale, trgLocale, mtProfile);
        if (mtpLangIDStr != null && mtpLangIDStr.trim().length() > 0)
        {
            mtpLang.setId(Long.valueOf(mtpLangIDStr.trim()));
        }
        
        if (isExistMTPLanguageName(mtpLang))
        {
            p_model.addAttribute("error", MTPLanguage_ERROR_NAMEEXIST);
        }
        else if (isExistLocalePair(mtpLang))
        {
            p_model.addAttribute("error", MTPLanguage_ERROR_LPEXIST);
        }
        else if (!isSupportsLocalePair(mtpLang))
        {
            p_model.addAttribute("error", MTPLanguage_ERROR_UNSUPPORT);
        }
        else
        {
            mtpLangDAO.saveOrUpdateMTPLanguage(mtpLang);
        }        
        
        return "mtpLanguagesDetail";
    }
    
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public String remove(HttpServletRequest p_req) throws FileNotFoundException,
            JAXBException
    {
        String mtpLangIDStr = p_req.getParameter("mtpLangID");        
        mtpLangDAO.deleteMTPLanguage(mtpLangIDStr.split(","));

        logger.info("Remove MTProfile:" + mtpLangIDStr + " BY " + p_req.getRemoteHost());
        return "redirect:main.htm";
    }
    
    @RequestMapping(value = "/getLanguagesByAccountName")
    public void getLanguage(HttpServletRequest p_request, HttpServletResponse p_response) throws JSONException, IOException
    {
        JSONArray jsonArray = new JSONArray();
        String accountName = p_request.getParameter(JSONPN_ACCOUNT_NAME); 
        Set<MTPLanguage> langs = mtpLangDAO.getMTPLanguageByAccount(accountName);
        for(MTPLanguage lang : langs)
        {
            jsonArray.put(getJSONObjec(lang));
        }
        
        p_response.getWriter().write(jsonArray.toString());
    }
    
    public JSONObject getJSONObjec(MTPLanguage p_lang)
    {
        JSONObject obj = new JSONObject();
        try
        {
            obj.put("id", p_lang.getId());
            obj.put("name", p_lang.getName());
            obj.put("accountName", p_lang.getAccountName());
            obj.put("sourceLocale", p_lang.getSrcLocale());
            obj.put("targetLocale", p_lang.getTrgLocale());
            obj.put("MTProfileName", p_lang.getMtProfile().getMtProfileName());            
        }
        catch (Exception e)
        {
        }
        
        return obj;
    }
    
    // Check whether the MTPLanguage Name already exist.
    private boolean isExistMTPLanguageName(MTPLanguage p_mtpLang)
    {
        boolean isExist = false;
        MTPLanguage mtpLang = mtpLangDAO.getMTPLanguage(p_mtpLang.getName());
        if (mtpLang != null && mtpLang.getId() != p_mtpLang.getId())
            isExist = true;

        return isExist;
    } 
    
    // Check whether the MTPLanguage Locale Pair already exist.
    private boolean isExistLocalePair(MTPLanguage p_mtpLang)
    {
        boolean isExist = false;
        MTPLanguage mtpLang = mtpLangDAO.getMTPLanguage(p_mtpLang.getSrcLocale(), p_mtpLang.getTrgLocale(), p_mtpLang.getAccountId());
        if (mtpLang != null && mtpLang.getId() != p_mtpLang.getId())
            isExist = true;

        return isExist;
    }    
    
    public boolean isSupportsLocalePair(MTPLanguage p_mtpLang)
    {
        MachineTranslationExtentInfo result = null;
        MachineTranslationProfile mt = p_mtpLang.getMtProfile();
        Locale sourcelocale = p_mtpLang.getSrcLocale().getLocale();
        Locale targetlocale = p_mtpLang.getTrgLocale().getLocale();

        EngineEnum ee = EngineEnum.getEngine(mt.getMtEngine());
        String lp = "";
        switch (ee)
        {
            case MS_Translator:
            case Safaba:
                return true;
            case ProMT:
                lp = getLanguagePairNameForProMt(sourcelocale, targetlocale);
                break;
            case IPTranslator:
                return IPTranslatorUtil.supportsLocalePair(sourcelocale,
                        targetlocale);
            case Asia_Online:
                lp = getLanguagePairNameForAo(sourcelocale, targetlocale);
                // Currently AO supports zh-CN, not support zh-HK and zh-TW.
                if (sourcelocale.getLanguage().equalsIgnoreCase("ZH")
                        && !sourcelocale.getCountry().equalsIgnoreCase("CN"))
                {
                    return false;
                }
                if (targetlocale.getLanguage().equalsIgnoreCase("ZH")
                        && !targetlocale.getCountry().equalsIgnoreCase("CN"))
                {
                    return false;
                }
                break;
        }
        try
        {

            Set lp2DomainCombinations = mt.getExInfo();
            if (lp2DomainCombinations != null
                    && lp2DomainCombinations.size() > 0)
            {
                Iterator lp2DCIt = lp2DomainCombinations.iterator();
                while (lp2DCIt.hasNext())
                {
                    MachineTranslationExtentInfo aoLP2DC = (MachineTranslationExtentInfo) lp2DCIt
                            .next();
                    String lpName = aoLP2DC.getLanguagePairName();
                    if (lpName != null && lpName.equalsIgnoreCase(lp))
                    {
                        result = aoLP2DC;
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
        }

        return result == null ? false : true;
    }
    
    private String getLanguagePairNameForProMt(Locale p_sourceLocale,
            Locale p_targetLocale)
    {
        if (p_sourceLocale == null || p_targetLocale == null)
        {
            return null;
        }

        String srcLang = p_sourceLocale.getDisplayLanguage(Locale.ENGLISH);
        String srcCountry = p_sourceLocale.getDisplayCountry(Locale.ENGLISH);
        if ("Chinese".equals(srcLang) && "China".equals(srcCountry))
        {
            srcLang = "Chinese (Simplified)";
        }

        String trgLang = p_targetLocale.getDisplayLanguage(Locale.ENGLISH);
        String trgCountry = p_targetLocale.getDisplayCountry(Locale.ENGLISH);
        if ("Chinese".equals(trgLang) && "China".equals(trgCountry))
        {
            trgLang = "Chinese (Simplified)";
        }

        return (srcLang + "-" + trgLang);
    }
    
    private String getLanguagePairNameForAo(Locale sourcelocale,
            Locale targetlocale)
    {
        String srcLang = checkLang(sourcelocale);
        String trgLang = checkLang(targetlocale);
        String lp = srcLang + "-" + trgLang;
        return lp;
    }
    
    private String checkLang(Locale p_locale)
    {
        if (p_locale == null)
        {
            return "";
        }

        String lang = p_locale.getLanguage();
        if ("in".equalsIgnoreCase(lang))
        {
            lang = "id";
        }

        return lang;
    }
}