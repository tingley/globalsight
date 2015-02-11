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

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.globalsight.dispatcher.bo.Account;
import com.globalsight.dispatcher.bo.AppConstants;
import com.globalsight.dispatcher.bo.GlobalSightLocale;
import com.globalsight.dispatcher.bo.MTPLanguage;
import com.globalsight.dispatcher.dao.AccountDAO;
import com.globalsight.dispatcher.dao.CommonDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;
import com.globalsight.dispatcher.dao.MTPLanguagesDAO;
import com.globalsight.dispatcher.util.TranslateUtil;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;

/**
 * Dispatcher Controller for 'RESTful WebService'.
 * 
 * @author Joey
 *
 */
@Controller
@RequestMapping("/translate")
public class TranslateController implements AppConstants {
    
    AccountDAO accountDAO = DispatcherDAOFactory.getAccountDAO();
    
    @RequestMapping("/")
    public String doTranslate(@RequestParam Map<String, String> map, ModelMap model) {

        String sourceLanguage = map.get(JSONPN_SOURCE_LANGUAGE);
        String targetLanguage = map.get(JSONPN_TARGET_LANGUAGE);
        String oriSourceText = map.get(JSONPN_SOURCE_TEXT);
        String securityCode = map.get(JSONPN_SECURITY_CODE);
        Account account = accountDAO.getAccountBySecurityCode(securityCode);
        
        Map<String, String> result =  
                getTranslatedText(sourceLanguage, targetLanguage, oriSourceText, account);
        
        model.addAttribute(JSONPN_SOURCE_LANGUAGE, sourceLanguage);
        model.addAttribute(JSONPN_TARGET_LANGUAGE, targetLanguage);
        model.addAttribute(JSONPN_SOURCE_TEXT, oriSourceText);
        model.addAttribute(JSONPN_TARGET_TEXT, result.get(JSONPN_TARGET_TEXT));
        model.addAttribute(JSONPN_STATUS, result.get(JSONPN_STATUS));
        model.addAttribute(JSONPN_ERROR_MESSAGE, result.get(JSONPN_ERROR_MESSAGE));
        
        return "translate";
    }
    
    /**
     * Translate the source text, return translate result Map<String, String>.
     * 
     * @param p_srcLocale
     *            Source Locale
     * @param p_trgLocale
     *            Target Locale
     * @param p_srcText
     *            Source Text
     */
    private Map<String, String> getTranslatedText(String p_sourceLanguage, 
            String p_targetLanguage, String p_srcText, Account p_account)
    {
        Map<String, String> result = new HashMap<String, String>();
        MTPLanguagesDAO mtpLangDAO = DispatcherDAOFactory.getMTPLanguagesDAO();
        GlobalSightLocale srcLocale = CommonDAO.getGlobalSightLocaleByShortName(p_sourceLanguage);
        GlobalSightLocale trgLocale = CommonDAO.getGlobalSightLocaleByShortName(p_targetLanguage);
        if (srcLocale == null || trgLocale == null)
        {
            result.put(JSONPN_STATUS, STATUS_FAILED);
            result.put(JSONPN_ERROR_MESSAGE, ERROR_NO_LOCALE + p_sourceLanguage + ", " + p_targetLanguage);
            return result;
        }
        MTPLanguage mtpLang = mtpLangDAO.getMTPLanguage(srcLocale, trgLocale, p_account.getId());
        if (mtpLang == null)
        {
            result.put(JSONPN_STATUS, STATUS_FAILED);
            result.put(JSONPN_ERROR_MESSAGE, ERROR_NO_MTPROFILE);
            return result;
        }
        
        try
        {
            MachineTranslator translator = TranslateUtil.getMachineTranslator(mtpLang.getMtProfile());
            String[] target = translator.translateBatchSegments(srcLocale.getLocale(), trgLocale.getLocale(), new String[]{p_srcText}, true, false);
            if (target == null || target.length == 0 || target[0].trim().length() == 0)
            {
                result.put(JSONPN_STATUS, STATUS_FAILED);
                result.put(JSONPN_ERROR_MESSAGE, ERROR_NO_RESULT);
            }
            else
            {
                result.put(JSONPN_STATUS, STATUS_SUCCESS);
                result.put(JSONPN_TARGET_TEXT, target[0]);
            }
        }
        catch (MachineTranslationException e)
        {
            result.put(JSONPN_STATUS, STATUS_FAILED);
            result.put(JSONPN_ERROR_MESSAGE, ERROR_NO_RESULT);
        }
        
        return result;
    }
    
}