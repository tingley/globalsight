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

/**
 * Dispatcher DAO Factory
 * 
 * @author Joey
 *
 */
public class DispatcherDAOFactory
{
    static MTProfilesDAO mtProfileDAO;
    static MTPLanguagesDAO mtpLangDAO;
    static AccountDAO accountDAO;
    
    public static MTProfilesDAO getMTPRofileDAO()
    {
        if (mtProfileDAO == null)
        {
            mtProfileDAO = new MTProfilesDAO();
        }
        return mtProfileDAO;
    }

    public static MTPLanguagesDAO getMTPLanguagesDAO()
    {
        if (mtpLangDAO == null)
        {
            mtpLangDAO = new MTPLanguagesDAO();
        }
        return mtpLangDAO;
    }
    
    public static AccountDAO getAccountDAO()
    {
        if (accountDAO == null)
        {
            accountDAO = new AccountDAO();
        }
        return accountDAO;
    }
}
