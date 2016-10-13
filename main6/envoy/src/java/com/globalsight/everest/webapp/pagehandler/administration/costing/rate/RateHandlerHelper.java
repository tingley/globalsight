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
package com.globalsight.everest.webapp.pagehandler.administration.costing.rate;

// java
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.Vector;

import javax.naming.NamingException;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostCalculator;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.ActivityComparator;
import com.globalsight.everest.util.comparator.LocalePairComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SortUtil;

public class RateHandlerHelper
{
    /**
     * Get all Rates stored in the DB.
     * 
     * @return A collection of Rate objects.
     */
    public static Collection getAllRates() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getRates();
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
     * Get Rate by id.
     * 
     * @return A Rate
     */
    public static Rate getRate(long id) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getRate(id);
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
     * Get a Role associated with a given Rate.
     * 
     * @param p_rate
     *            Rate object
     * @param p_withRates
     *            Specifies 'true' if the collection of rates that are
     *            associated with the role should be populated, or 'false' if
     *            they shouldn't be populated.
     * 
     * @return A Role associated with p_rate
     */
    public static Role getRole(Rate p_rate, boolean p_withRates)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getContainerRole(p_rate,
                    p_withRates);
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
     * Get all Activities
     * 
     * @return A Vector of Activity objects sorted for the given locale.
     */
    public static Vector getAllActivities(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            Collection c = ServerProxy.getJobHandler().getAllActivities();
            if (c == null)
                return new Vector();
            else
            {
                ArrayList al = new ArrayList(c);
                ActivityComparator comp = new ActivityComparator(
                        ActivityComparator.NAME, p_locale);
                SortUtil.sort(al, comp);
                return new Vector(al);
            }
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(GeneralException.EX_NAMING, ne);
        }
    }

    /**
     * Get all LocalePairs
     * 
     * @return A Vector of LocalePair objects.
     */
    public static Vector getAllLocalePairs(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getSourceTargetLocalePairs());
            LocalePairComparator comp = new LocalePairComparator(p_locale);
            SortUtil.sort(al, comp);
            return new Vector(al);
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
     * Get all Currencies
     * 
     * @return A Vector of Currency objects.
     */
    public static Vector getAllCurrencies() throws EnvoyServletException
    {
        try
        {
            Collection c = ServerProxy.getCostingEngine().getCurrencies();
            return c == null ? new Vector() : new Vector(c);
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
     * Persist a new Rate
     * 
     */
    public static Rate addRate(Rate p_rate) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().addRate(p_rate);
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

    public static void removeRate(long p_id) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getCostingEngine().removeRate(p_id);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
    }

    /**
     * Modify a Rate and persist
     * 
     */
    public static void modifyRate(Rate p_rate) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getCostingEngine().modifyRate(p_rate);
            SystemConfiguration sc = SystemConfiguration.getInstance();
            if (sc.getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED) == true)
            {
                List jobs = null;
                Long rateIdLong = new Long(p_rate.getId());
                jobs = new ArrayList(
                        WorkflowHandlerHelper.getJobsByRate(rateIdLong
                                .toString()));
                Iterator Jit = jobs.iterator();
                while (Jit.hasNext())
                {
                    boolean nextJob = false;
                    Job curJob = (Job) Jit.next();
                    Collection wfs = new ArrayList();
                    Iterator it = curJob.getWorkflows().iterator();
                    while (it.hasNext())
                    {
                        Workflow curWF = (Workflow) it.next();
                        long wfId = curWF.getId();
                        Hashtable allTasks = curWF.getTasks();
                        TreeMap sortedTasks = new TreeMap(allTasks);
                        Iterator sortedTaskIterator = sortedTasks.values()
                                .iterator();
                        Task t = null;
                        while (sortedTaskIterator.hasNext())
                        {
                            t = (Task) sortedTaskIterator.next();
                            if (t != null)
                            {
                                Rate curRate = (Rate) t.getExpenseRate();
                                if (curRate != null
                                        && curRate.getName().equals(
                                                p_rate.getName()))
                                {
                                    // Get the pivot currency;
                                    Currency c = ServerProxy.getCostingEngine()
                                            .getPivotCurrency();
                                    String curr = c.getIsoCode();
                                    Currency oCurrency = ServerProxy
                                            .getCostingEngine().getCurrency(
                                                    curr);
                                    CostCalculator calculator = new CostCalculator(
                                            curJob.getId(), oCurrency, true,
                                            Cost.EXPENSE);
                                    calculator.sendToCalculateCost();
                                    nextJob = true;
                                }
                                if (sc.getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED) == true)
                                {
                                    curRate = (Rate) t.getRevenueRate();
                                    if (curRate != null
                                            && curRate.getName().equals(
                                                    p_rate.getName()))
                                    {
                                        // Get the pivot currency;
                                        Currency c = ServerProxy
                                                .getCostingEngine()
                                                .getPivotCurrency();
                                        String curr = c.getIsoCode();
                                        Currency oCurrency = ServerProxy
                                                .getCostingEngine()
                                                .getCurrency(curr);
                                        CostCalculator calculator = new CostCalculator(
                                                curJob.getId(), oCurrency,
                                                true, Cost.REVENUE);
                                        calculator.sendToCalculateCost();
                                        nextJob = true;
                                    }
                                }
                            }
                        }
                        if (nextJob)
                        {
                            break;
                        }
                    }
                }
            }
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, e);
        }
    }
}
