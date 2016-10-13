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
package com.globalsight.reports.util;

import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Money;

/**
* Utility class to provide information about the reports package.
*/
public class ReportsPackage
{
    private static Boolean s_isReportsPackageInstalled = null;
    private static final String INETSOFT_CLASS = "inetsoft.sree.RepletRepository";

    private static final Logger CATEGORY =
    Logger.getLogger(
        ReportsPackage.class.getName());

    public static final Object ZERO_COST  = (Object) new Float(0);

    /**
    * Returns true if the Reports Package is installed,
    * otherwise false.
    * <br>
    * @return true|false
    */
    public static boolean isInstalled()
    {
        if (s_isReportsPackageInstalled == null)
            setIsInstalled();
        return s_isReportsPackageInstalled.booleanValue();
    }

    private static synchronized void setIsInstalled()
    {
        try
        {
            Class.forName(INETSOFT_CLASS);
            s_isReportsPackageInstalled = new Boolean(true);
        }
        catch (Throwable t)
        {
            s_isReportsPackageInstalled = new Boolean(false);
        }
        System.out.println("Reports Package is installed? " + s_isReportsPackageInstalled);
    }

    /**
    * Utility to log errors out to CAP.log
    * <br>
    */
    public static void logError(Object msg)
    {
        if (msg instanceof Exception)
        {
            Exception e = (Exception) msg;
            CATEGORY.error("Reporting Error",e);
        }
        else
            CATEGORY.error(msg);
    }

    /**
    * Utility to log errors out to CAP.log
    * <br>
    */
    public static void logError(Object msg, Throwable t)
    {
        CATEGORY.error(msg,t);
    }

    /**
    * Looks up the key in the resource bundle and return the message.
    * If the resource bundle does not contain the key,
    * then "<p_key>" is returned instead.
    * <br>
    * @param p_bundle -- the resource bundle to use
    * @param p_key -- the message key
    * @return the desired message
    */
    public static String getMessage(ResourceBundle p_bundle, String p_key)
    {
        try {
            return p_bundle.getString(p_key);
        }
        catch (Exception e)
        {
            ReportsPackage.logError("Could not find message key " + p_key, e);
            return "<" + p_key + ">";
        }
    }


    /**
     * Makes getting an actual cost from a Cost
     * object easier
     * 
     * @param p_cost cost
     * @return Object (Float)
     */
    public static Object getActualCost(Cost p_cost)
    {
        if (p_cost == null)
            return ZERO_COST;
        Money m = p_cost.getActualCost();
        if (m==null)
            return ZERO_COST;
        else
            return (Object) new Float(m.getAmount()); 
    }


    /**
     * Makes getting an estimated cost from a Cost
     * object easier
     * 
     * @param p_cost cost
     * @return Object (Float)
     */
    public static Object getEstimatedCost(Cost p_cost)
    {
        if (p_cost == null)
            return ZERO_COST;
        Money m = p_cost.getEstimatedCost();
        if (m==null)
            return ZERO_COST;
        else
            return (Object) new Float(m.getAmount()); 
    }

    public static Object getNoUseEstimatCost(Cost cost){
        if ( cost == null){
            return ZERO_COST;
        }
        Money m= cost.getNoUseEstimatedCost();
        if( m == null ){
            return ZERO_COST;
        } else {
            return (Object) new Float( m.getAmount() );
        }
    }

    /**
     * Makes getting a final cost from a Cost
     * object easier
     * 
     * @param p_cost cost
     * @return Object (Float)
     */
    public static Object getFinalCost(Cost p_cost)
    {
        if (p_cost == null)
            return ZERO_COST;
        Money m = p_cost.getFinalCost();
        if (m==null)
            return ZERO_COST;
        else
            return (Object) new Float(m.getAmount()); 
    }

}

