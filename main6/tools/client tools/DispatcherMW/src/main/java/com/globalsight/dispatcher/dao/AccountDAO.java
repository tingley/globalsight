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
package com.globalsight.dispatcher.dao;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.globalsight.dispatcher.bo.Account;
import com.globalsight.dispatcher.bo.Accounts;
import com.globalsight.dispatcher.bo.AppConstants;

/**
 * Dispatcher Account DAO.
 * 
 * @author Joey
 * 
 */
public class AccountDAO implements AppConstants
{
    protected static final Logger logger = Logger.getLogger(AccountDAO.class);
    protected static final String fileName = "Account.xml";
    protected static String filePath;
    protected Accounts accounts;

    public AccountDAO()
    {
    }

    public static String getFilePath()
    {
        if (filePath == null)
        {
            filePath = CommonDAO.getDataFolderPath() + fileName;
        }

        return filePath;
    }

    protected void saveAccounts(Accounts p_accounts) throws JAXBException
    {
        // create JAXB context and instantiate marshaller
        JAXBContext context = JAXBContext.newInstance(Accounts.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        // Write to File
        m.marshal(p_accounts, new File(getFilePath()));
    }

    public void saveAccount(Account p_account) throws JAXBException
    {
        if (p_account.getId() < 0)
        {
            if (accounts == null)
                getAllAccounts();
            p_account.setId(accounts.getAndIncrement());
        }

        getAllAccounts().add(p_account);
        saveAccounts(accounts);
    }

    public void updateAccount(Account p_account) throws JAXBException
    {
        for (Account account : getAllAccounts())
        {
            if (account.getId() == p_account.getId())
            {
                accounts.getAccounts().remove(account);
                accounts.getAccounts().add(p_account);
                saveAccounts(accounts);
                break;
            }
        }
    }

    public void saveOrUpdateAccount(Account p_account) throws JAXBException
    {
        if (p_account.getId() < 0)
        {
            saveAccount(p_account);
        }
        else
        {
            updateAccount(p_account);
        }
    }

    public void deleteAccount(long p_accountID) throws JAXBException
    {
        for (Account account : getAllAccounts())
        {
            if (account.getId() == p_accountID
                    && !DispatcherDAOFactory.getMTPLanguagesDAO().isUseAccount(account))
            {
                logger.info("Remove Account: " + p_accountID);
                accounts.getAccounts().remove(account);
                saveAccounts(accounts);
                break;
            }
        }
    }
    
    public void deleteAccount(String p_accountIDSArray) throws JAXBException
    {
        Set<Long> accountIDSet = new HashSet<Long>();
        for(String id : p_accountIDSArray.split(","))
        {
            accountIDSet.add(Long.valueOf(id));
        }
        
        for (Account account : getAllAccounts())
        {
            if (accountIDSet.contains(account.getId()) 
                    && !DispatcherDAOFactory.getMTPLanguagesDAO().isUseAccount(account))
            {
                logger.info("Remove Account: [" + account.getId() + ", " + account.getAccountName() + "]");
                accounts.getAccounts().remove(account);
            }
        }
        
        saveAccounts(accounts);
    }

    public Set<Account> getAllAccounts()
    {
        if (accounts == null)
        {
            try
            {
                File file = new File(getFilePath());
                if (!file.exists())
                {
                    file.createNewFile();
                    accounts = new Accounts();
                    return accounts.getAccounts();
                }
                else if (file.length() == 0)
                {
                    accounts = new Accounts();
                    return accounts.getAccounts();
                }

                JAXBContext context = JAXBContext.newInstance(Accounts.class);
                Unmarshaller um = context.createUnmarshaller();
                accounts = (Accounts) um.unmarshal(new FileReader(getFilePath()));
            }
            catch (JAXBException jaxbEx)
            {
                String message = "getAllAccounts --> JAXBException:" + getFilePath();
                logger.error(message, jaxbEx);
                return null;
            }
            catch (IOException ioEx)
            {
                String message = "getAllAccounts --> JAXBException:" + getFilePath();
                logger.error(message, ioEx);
            }
        }

        return accounts.getAccounts();
    }

    public Account getAccount(long p_accountID)
    {
        for (Account account : getAllAccounts())
        {
            if (account.getId() == p_accountID)
                return account;
        }
        return null;
    }
    
    public Account getAccountByAccountName(String p_name)
    {
        for (Account account : getAllAccounts())
        {
            if (account.getAccountName().equals(p_name))
                return account;
        }
        return null;
    }
    
    public Account getAccountBySecurityCode(String p_code)
    {
        for (Account account : getAllAccounts())
        {
            if (account.getSecurityCode().equals(p_code))
                return account;
        }
        return null;
    }
    
    public String getSecurityCode(String p_accountID)
    {
        if(p_accountID == null || p_accountID.trim().length() ==0)
            return null;
        
        long id = Long.valueOf(p_accountID);
        for (Account account : getAllAccounts())
        {
            if (account.getId() == id)
                return account.getSecurityCode();
        }
        return null;
    }
}
