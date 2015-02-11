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
package com.globalsight.everest.webapp.pagehandler.administration.costing.currency;

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

// java
import java.util.Collection;
import java.util.Vector;
import javax.naming.NamingException;

import java.rmi.RemoteException;

// com.globalsight
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.IsoCurrency;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;




public class CurrencyHandlerHelper
{
    /**
     * Get all Currencies stored in the DB.
     * 
     * @return A collection of Currency objects.
     */
    public static Collection getAllCurrencies()
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getCurrencies();
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }
    
    /**
     * Get Currency based on code.
     */
    public static Currency getCurrency(String p_code)
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getCurrency(p_code);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    /**
     * Get IsoCurrency based on code.
     */
    public static IsoCurrency getIsoCurrency(String p_code)
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getIsoCurrency(p_code);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    /**
     * Add or modify a Currency
     * 
     * @param p_currency Currency object
     * @return added or modified Currency
     */
    public static Currency addOrModifyCurrency(Currency p_currency)
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine()
                .addOrModifyCurrency(p_currency);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }

    /**
     * Get all IsoCurrencies who don't have a conversion factor set up yet.
     * 
     * @return A Vector of IsoCurrency objects.
     */
    public static Vector getAllIsoCurrencies()
        throws EnvoyServletException
    {
	try
	{
            
            return ServerProxy.getCostingEngine().getIsoCurrenciesWithoutConversion();
        }
	catch (GeneralException ge)
	{
	    throw new EnvoyServletException(ge);
	}
	catch (RemoteException re)
	{
	    throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
	}
    }
    

    /**
     * Get pivot currency 
     * 
     * @return pivot currency 
     */
    public static Currency getPivotCurrency()
        throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getPivotCurrency();
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }
}
