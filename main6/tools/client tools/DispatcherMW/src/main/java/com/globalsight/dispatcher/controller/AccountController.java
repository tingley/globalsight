/**
 *  Copyright 2014 Welocalize, Inc. 
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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.globalsight.dispatcher.bo.Account;
import com.globalsight.dispatcher.bo.AppConstants;
import com.globalsight.dispatcher.dao.AccountDAO;
import com.globalsight.dispatcher.dao.DispatcherDAOFactory;
import com.globalsight.dispatcher.dao.MTPLanguagesDAO;

/**
 * Dispatcher Controller for 'Languages' Pages
 * 
 * @author Joey
 *
 */
@Controller
@RequestMapping("/account")
public class AccountController implements AppConstants
{
    private static final Logger logger = Logger.getLogger(AccountController.class);  
    final int securityCodeLength = 20;
    AccountDAO accountDAO = DispatcherDAOFactory.getAccountDAO();
    MTPLanguagesDAO langDAO = DispatcherDAOFactory.getMTPLanguagesDAO();
    
    @RequestMapping(value = "/main")
    public String listAll(ModelMap p_model)
    {
        p_model.put("allAccounts", accountDAO.getAllAccounts());
        return "accountMain";
    }

    @RequestMapping(value = "/viewDetail", method = RequestMethod.POST)
    public String viewDetail(@RequestParam Map<String, String> p_reqMap, ModelMap p_model) 
            throws FileNotFoundException, JAXBException
    {
        Account data = null;
        String idStr = p_reqMap.get("selectedID");
        if (idStr != null)
        {
            long id = Long.valueOf(idStr);
            if (id >= 0)
                data = accountDAO.getAccount(id);
        }
        
        if (data == null)
        {
            data = new Account();
            data.setSecurityCode(RandomStringUtils.randomAlphabetic(securityCodeLength));
        }

        p_model.put("account", data);
        return "accountDetail";
    }
    
    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
    public String saveOrUpdate(@RequestParam Map<String, String> p_reqMap, ModelMap p_model) 
            throws FileNotFoundException, JAXBException
    {        
        Account account = null;
        String accountIDStr = p_reqMap.get("accountId");
        String accountName = p_reqMap.get("accountName");
        String description = p_reqMap.get("description");
        String securityCode = p_reqMap.get(JSONPN_SECURITY_CODE);
        if(accountIDStr == null || accountIDStr.equals("-1"))
        {
            account = new Account();
        }
        else
        {
            account = new Account(accountDAO.getAccount(Long.valueOf(accountIDStr)));
        }
        account.setAccountName(accountName);
        account.setDescription(description);
        account.setSecurityCode(securityCode);
        
        if (isExistAccountName(account))
        {
            p_model.addAttribute("error", "The Account Name already exists.");
        }
        else if (isExistSecurityCode(account))
        {
            p_model.addAttribute("error", "The Security Code already exists.");
        }
        else
        {
            accountDAO.saveOrUpdateAccount(account);
        }  
              
        
        return "accountDetail";
    }
    
    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    public String remove(HttpServletRequest p_request, ModelMap p_model) throws FileNotFoundException,
            JAXBException
    {        
        String SelectedIDArr = (String) p_request.getParameter("selectedIDS");
        for(String idStr : SelectedIDArr.split(","))
        {
            long accountID = Long.valueOf(idStr);
            if (accountID < 0)
                continue;
            Account account = accountDAO.getAccount(accountID);
            if (langDAO.isUseAccount(account))
            {
                p_model.addAttribute("error",
                        "The Account is used, please remove the assiciation with languages firstly.");
                return "";
            }

            accountDAO.deleteAccount(accountID);
        }

        return "main.htm";
    }
    
    @RequestMapping(value = "/getRandom", method = RequestMethod.GET)
    public void getRandom(HttpServletRequest p_request, HttpServletResponse p_response) throws IOException, JSONException
    { 
        JSONObject obj = new JSONObject();
        String securityCode = RandomStringUtils.randomAlphabetic(securityCodeLength);        
        obj.put("securityCode", securityCode);
        obj.put("length", securityCodeLength);
        logger.info("Generate Security Code: " + obj.toString());
        p_response.getWriter().write(obj.toString());
    }
    
    private boolean isExistAccountName(Account p_account)
    {
        Account account = accountDAO.getAccountByAccountName(p_account.getAccountName());        
        if(account != null && account.getId() != p_account.getId())
            return true;
        
        return false;
    }
    
    private boolean isExistSecurityCode(Account p_account)
    {
        Account account = accountDAO.getAccountBySecurityCode(p_account.getSecurityCode());        
        if(account != null && account.getId() != p_account.getId())
            return true;
        
        return false;
    }
}