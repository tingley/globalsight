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

package com.globalsight.everest.costing;

// globalsight
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.config.SystemParameterEntityException;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserRole;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.costing.CostDescriptorModifier;
import com.globalsight.everest.persistence.costing.IsoCurrencyDescriptorModifier;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowServer;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * This class is used to calculate the cost of various objects within System4.
 * These objects include TargetPages, Tasks, Workflows and Jobs. The cost can be
 * calculated in the currency specified or in the pivot currency if not
 * specified.
 */
public class CostingEngineLocal implements CostingEngine
{
    // for logging purposes
    private static final Logger c_logger = Logger
            .getLogger(CostingEngineLocal.class.getName());

    // holds the collection of iso currency codes
    private Vector m_isoCurrencies = new Vector(180);
    
    private static final Object LOCK = new Object();

    /**
     * @see CostingEngine.getIsoCurrencies
     */
    public Vector getIsoCurrencies() throws RemoteException, CostingException
    {
        // load from DB if not loaded yet
        if (m_isoCurrencies.size() <= 0)
        {
            try
            {
                // Collection isoCurrencies = PersistenceService
                // .getInstance()
                // .executeNamedQuery(
                // IsoCurrencyQueryNames.ALL_ISO_CURRENCIES, false);
                String hql = "from IsoCurrency ic order by ic.name asc";
                Collection isoCurrencies = HibernateUtil.search(hql);
                m_isoCurrencies = new Vector(isoCurrencies);
            }
            catch (PersistenceException pe)
            {
                c_logger.error(
                        "PersistenceException while retrieving iso currencies.",
                        pe);
                throw new CostingException(
                        CostingException.MSG_FAILED_TO_RETRIEVE_ISO_CURRENCIES,
                        null, pe);
            }
        }
        return m_isoCurrencies;
    }

    /**
     * @see CostingEngine.getIsoCurrenciesWithoutConversion
     */
    public Vector getIsoCurrenciesWithoutConversion() throws RemoteException,
            CostingException
    {
        Vector isoWithoutConversion = new Vector();
        try
        {
            // Collection isoCurrencies = PersistenceService
            // .getInstance()
            // .executeNamedQuery(
            // IsoCurrencyQueryNames.ISO_CURRENCIES_WITHOUT_CONVERSION,
            // CompanyWrapper.addCompanyIdBoundArgs(new Vector()),
            // false);
            String sql = IsoCurrencyDescriptorModifier.ISO_CURRENCIES_WITHOUT_CONVERSION_SQL;
            HashMap map = CompanyWrapper.addCompanyIdBoundArgs(
                    CompanyWrapper.COPMANY_ID_START_ARG,
                    CompanyWrapper.COPMANY_ID_END_ARG);
            Collection isoCurrencies = HibernateUtil.searchWithSql(sql, map,
                    IsoCurrency.class);

            isoWithoutConversion = new Vector(isoCurrencies);
        }
        catch (PersistenceException pe)
        {
            c_logger.error(
                    "PersistenceException while retrieving iso currencies.", pe);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_ISO_CURRENCIES,
                    null, pe);
        }
        return isoWithoutConversion;
    }

    /**
     * @see CostingEngine.getIsoCurrency(String)
     */
    public IsoCurrency getIsoCurrency(String p_isoCode) throws RemoteException,
            CostingException
    {
        IsoCurrency cur = null;
        try
        {
            // Vector args = new Vector();
            // args.add(p_isoCode);
            // Collection currs = PersistenceService.getInstance()
            // .executeNamedQuery(IsoCurrencyQueryNames.CURRENCY_BY_CODE,
            // args, false);
            String hql = "from IsoCurrency ic where ic.code=:currencyCode";
            HashMap map = new HashMap();
            map.put("currencyCode", p_isoCode);
            Collection currs = HibernateUtil.search(hql, map);
            Iterator i = currs.iterator();
            cur = i.hasNext() ? (IsoCurrency) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when getting iso currency "
                    + p_isoCode, pe);
            String args[] =
            { p_isoCode };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_ISO_CURRENCY, args,
                    pe);
        }
        return cur;
    }

    /**
     * @see CostingEngine.addOrModifyCurrency
     */
    public Currency addOrModifyCurrency(Currency p_currency)
            throws RemoteException, CostingException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            Currency oldCurr = getCurrency(p_currency.getIsoCode(),
                    p_currency.getCompanyId());

            if (oldCurr != null)
            {
                Currency curClone = (Currency) session.get(Currency.class,
                        oldCurr.getIdAsLong());
                curClone.setConversionFactor(p_currency.getConversionFactor());
                session.update(curClone);
                // will have the ID now
                p_currency = curClone;
            }
            else
            {
                session.save(p_currency);
            }
            transaction.commit();
        }
        catch (PersistenceException pe)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            c_logger.error(
                    "Persistence Exception while adding/modifying a currency.",
                    pe);
            String args[] =
            { p_currency.getIsoCode() };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_ADDORMODIFY_CURRENCY, args,
                    pe);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
        return p_currency;
    }

    /**
     * @see CostingEngine.getCurrencies
     */
    public Collection getCurrencies() throws RemoteException, CostingException
    {
        // always load from DB
        Collection currencies = null;
        try
        {
            String hql = "from Currency c";
            HashMap map = null;
            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " where c.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }

            currencies = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error("PersistenceException while retrieving currencies.",
                    pe);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_CURRENCIES, null,
                    pe);
        }
        return currencies;
    }

    /**
     * @see CostingEngine.getCurrency(String)
     */
    public Currency getCurrency(String p_isoCode, long p_companyId)
            throws RemoteException, CostingException
    {
        Currency cur = null;
        try
        {
            String hql = "from Currency c where c.isoCurrency.code = :code and c.companyId = :cId";
            Map map = new HashMap();
            map.put("code", p_isoCode);
            map.put("cId", p_companyId);
            Collection currencies = HibernateUtil.search(hql, map);
            Iterator i = currencies.iterator();
            cur = i.hasNext() ? (Currency) i.next() : null;
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when retrieving currency "
                    + p_isoCode, pe);
            String args[] =
            { p_isoCode };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_CURRENCY, args, pe);
        }
        return cur;
    }

    public Currency getCurrency(String p_isoCode) throws RemoteException,
            CostingException
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        return getCurrency(p_isoCode, Long.parseLong(companyId));
    }

    /**
     * @see CostingEngine.getPivotCurrency()
     */
    public Currency getPivotCurrency() throws RemoteException, CostingException
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        Currency c = (Currency) Currency.getPivotCurrencies().get(
                Long.parseLong(companyId));
        if (c == null)
        {
            c = HibernateUtil.get(Currency.class, 1);
        }
        return c;
    }

    /**
     * @see CostingEngine.changePivotCurrency(Currency)
     */
    public void changePivotCurrency(Currency p_newPivotCurrency)
            throws RemoteException, CostingException
    {
        // This will be a parameter of this method after the
        // system parameter be company sensetive
        String companyId = "companyId";
        Currency oldPivot = getPivotCurrency();

        // if they are the same, leave alone - no need to do any conversions
        if (p_newPivotCurrency.equals(oldPivot))
        {
            return;
        }

        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Collection currencies = getCurrencies();
            String hql = "from Currency c";

            String currentId = CompanyThreadLocal.getInstance().getValue();
            boolean isSuper = CompanyWrapper.SUPER_COMPANY_ID.equals(currentId);
            Query query;
            if (!isSuper)
            {
                hql += " where c.companyId = :companyId";
                query = session.createQuery(hql).setLong(companyId,
                        Long.parseLong(currentId));
            }
            else
            {
                query = session.createQuery(hql);
            }

            currencies = query.list();

            float newConversionOldPivot = 1 / p_newPivotCurrency
                    .getConversionFactor();

            for (Iterator iCur = currencies.iterator(); iCur.hasNext();)
            {
                Currency cur = (Currency) iCur.next();
                // if it doesn't equal the old or new pivot
                // convert its value
                if (!cur.equals(p_newPivotCurrency) && !cur.equals(oldPivot))
                {
                    // Currency cloneCur = (Currency)uow.registerObject(cur);
                    // The areas where calculations are done (multiply, add,
                    // subtract)
                    // should be changed to use BigDecimal for the actual
                    // calculation.
                    float newConversion = BigDecimalHelper.multiply(
                            cur.getConversionFactor(), newConversionOldPivot);
                    cur.setConversionFactor(newConversion);
                    session.update(cur);
                }
            }

            Currency cloneOldPivot = (Currency) session.get(Currency.class,
                    oldPivot.getIdAsLong());
            cloneOldPivot.setConversionFactor(newConversionOldPivot);
            session.update(cloneOldPivot);

            Currency cloneNewPivot = (Currency) session.get(Currency.class,
                    p_newPivotCurrency.getIdAsLong());
            cloneNewPivot.setConversionFactor(1);
            session.update(cloneNewPivot);
            transaction.commit();

            // change the static pivot currency that is already set.
            Map pivotCurrencies = Currency.getPivotCurrencies();
            pivotCurrencies.put(Long.parseLong(currentId), cloneNewPivot);
            Currency.setPivotCurrencies(pivotCurrencies);
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            c_logger.error("Failed to change the pivot currency to "
                    + p_newPivotCurrency.getName(), e);
            String args[] =
            { p_newPivotCurrency.getName() };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_CHANGE_PIVOT_CURRENCY, args,
                    e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Implement the SystemParameterChangeListener.listen(String, String) method
     * to handle any changes to certain system parameters that the CostingEngine
     * has registered to listen to.
     */
    public void listen(String p_systemParameterName,
            String p_systemParameterValue) throws RemoteException,
            SystemParameterEntityException
    {
        if (p_systemParameterName.equals(SystemConfigParamNames.PIVOT_CURRENCY))
        {
            try
            {
                Collection companies = null;
                try
                {
                    companies = ServerProxy.getJobHandler().getAllCompanies();
                }
                catch (GeneralException e)
                {
                    throw new RuntimeException(
                            "Need to be modified after made system parameter company sensitive");
                }
                catch (NamingException e)
                {
                    throw new RuntimeException(
                            "Need to be modified after made system parameter company sensitive");
                }
                for (Iterator iter = companies.iterator(); iter.hasNext();)
                {
                    long companyId = ((Company) iter.next()).getId();
                    Currency cur = getCurrency(p_systemParameterValue,
                            companyId);
                    if (cur != null)
                    {
                        changePivotCurrency(cur);
                    }
                }
            }
            catch (CostingException ce)
            {
                String args[] =
                { SERVICE_NAME, p_systemParameterName, p_systemParameterValue };
                throw new SystemParameterEntityException(
                        SystemParameterEntityException.MSG_LISTENER_FAILED,
                        args, ce);
            }
        }
    }

    /**
     * @see CostingEngine.loadCurrencies.
     */
    public void loadCurrencies() throws RemoteException, CostingException
    {
        try
        {
            Collection companies = null;
            try
            {
                companies = ServerProxy.getJobHandler().getAllCompanies();
            }
            catch (NamingException e)
            {
                throw new GeneralException(e);
            }
            String companyId = null;
            for (Iterator iter = companies.iterator(); iter.hasNext();)
            {
                companyId = Long.toString(((Company) iter.next()).getId());
                addOrModifyPivotCurrency(companyId);
            }
        }
        catch (GeneralException ge)
        {
            c_logger.error("Failed to retrieve and set the pivot currency.", ge);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_GET_PIVOT_CURRENCY, null, ge);
        }
    }

    public Currency addOrModifyPivotCurrency(String p_companyId)
            throws RemoteException, CostingException, GeneralException
    {
        // get the pivot currency
        SystemConfiguration sc = SystemConfiguration.getInstance();
        String pivotCurCode = sc
                .getStringParameter(SystemConfigParamNames.PIVOT_CURRENCY);

        IsoCurrency iso = getIsoCurrency(pivotCurCode);
        long companyId = Long.parseLong(p_companyId);
        Currency pivot = new Currency(iso, 1, companyId);
        pivot = addOrModifyCurrency(pivot);
        Currency.getPivotCurrencies().put(companyId, pivot);

        return pivot;
    }

    /**
     * @see CostingEngine.addRate(Rate)
     */
    public Rate addRate(Rate p_rate) throws RemoteException, CostingException
    {
        LocalePair lp = p_rate.getLocalePair();
        Activity act = p_rate.getActivity();

        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            p_rate.setId(-1L);
            session.save(p_rate);
            // call UserManager to add the association between
            // the role and rate
            getUserManager().addRateToRole(p_rate, act,
                    lp.getSource().toString(), lp.getTarget().toString());
            transaction.commit();
        }
        catch (PersistenceException pe)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            // rollback the rate added to role within userManager
            try
            {
                c_logger.error("Persistence Exception when adding rate "
                        + p_rate.toString(), pe);
                getUserManager().removeRateFromRole(p_rate, act,
                        lp.getSource().toString(), lp.getTarget().toString());
            }
            catch (UserManagerException ume)
            {
                // just let continue
                // the error is logged and exception will be thrown
            }
            String args[] =
            { p_rate.getName() };
            throw new CostingException(CostingException.MSG_FAILED_TO_ADD_RATE,
                    args, pe);
        }
        catch (UserManagerException ume)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            StringBuffer sb = new StringBuffer(
                    "UserManagerException when adding a rate ");
            sb.append(p_rate.toString());
            sb.append(" to role with activity ");
            sb.append(act.getName());
            sb.append(" and source locale ");
            sb.append(lp.getSource().getDisplayName());
            sb.append(" and target locale ");
            sb.append(lp.getTarget().getDisplayName());
            c_logger.error(sb.toString(), ume);
            String args[] =
            { p_rate.getName(), act.getName(), lp.getSource().getDisplayName(),
                    lp.getTarget().getDisplayName() };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_ADD_RATE_TO_ROLE, args, ume);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
        return p_rate;
    }

    /**
     * @see CostingEngine.modifyRate(Rate)
     */
    public void modifyRate(Rate p_rate) throws RemoteException,
            CostingException
    {
        try
        {
            // get the existing rate and update it
            Rate oldRate = getRate(p_rate.getId());

            // if the name has changed - need to verify it isn't a duplicate
            // same name with the same activity and locale pair
            if (!oldRate.getName().equals(p_rate.getName()))
            {
                Role role = getUserManager().getContainerRole(oldRate, true);
                if (getUserManager()
                        .isDuplicateRateName(p_rate.getName(), role))
                {
                    // the name is duplicated
                    String args[] =
                    { Long.toString(oldRate.getId()) };
                    throw new CostingException(
                            CostingException.MSG_FAILED_TO_MODIFY_RATE, args,
                            null);

                }
            }

            Rate rateClone = oldRate;
            rateClone.setName(p_rate.getName());
            // set the type and rate values in case they have changed
            rateClone.setRateType(p_rate.getRateType());
            if (rateClone.getRateType().equals(Rate.UnitOfWork.WORD_COUNT_BY))
            {
                rateClone.setInContextMatchRate(p_rate.getInContextMatchRate());
                rateClone.setContextMatchRate(p_rate.getContextMatchRate());
                rateClone.setSegmentTmRate(p_rate.getSegmentTmRate());
                rateClone.setLowFuzzyMatchRate(p_rate.getLowFuzzyMatchRate());
                rateClone.setMedFuzzyMatchRate(p_rate.getMedFuzzyMatchRate());
                rateClone.setMedHiFuzzyMatchRate(p_rate
                        .getMedHiFuzzyMatchRate());
                rateClone.setHiFuzzyMatchRate(p_rate.getHiFuzzyMatchRate());
                rateClone.setNoMatchRate(p_rate.getNoMatchRate());
                rateClone.setRepetitionRate(p_rate.getRepetitionRate());

                rateClone.setInContextMatchRatePer(p_rate
                        .getInContextMatchRatePer());
                rateClone.setContextMatchRatePer(p_rate
                        .getContextMatchRatePer());
                rateClone.setSegmentTmRatePer(p_rate.getSegmentTmRatePer());
                rateClone.setLowFuzzyMatchRatePer(p_rate
                        .getLowFuzzyMatchRatePer());
                rateClone.setMedFuzzyMatchRatePer(p_rate
                        .getMedFuzzyMatchRatePer());
                rateClone.setMedHiFuzzyMatchRatePer(p_rate
                        .getMedHiFuzzyMatchRatePer());
                rateClone.setHiFuzzyMatchRatePer(p_rate
                        .getHiFuzzyMatchRatePer());
                rateClone.setRepetitionRatePer(p_rate.getRepetitionRatePer());
            }
            else if (rateClone.getRateType().equals(Rate.UnitOfWork.WORD_COUNT))
            {
                rateClone.setInContextMatchRate(p_rate.getInContextMatchRate());
                rateClone.setContextMatchRate(p_rate.getContextMatchRate());
                rateClone.setSegmentTmRate(p_rate.getSegmentTmRate());
                rateClone.setLowFuzzyMatchRate(p_rate.getLowFuzzyMatchRate());
                rateClone.setMedFuzzyMatchRate(p_rate.getMedFuzzyMatchRate());
                rateClone.setMedHiFuzzyMatchRate(p_rate
                        .getMedHiFuzzyMatchRate());
                rateClone.setHiFuzzyMatchRate(p_rate.getHiFuzzyMatchRate());
                rateClone.setNoMatchRate(p_rate.getNoMatchRate());
                rateClone.setRepetitionRate(p_rate.getRepetitionRate());
            }
            else
            {
                rateClone.setUnitRate(p_rate.getUnitRate());
            }

            // if the currency changed
            if (!oldRate.getCurrency().equals(p_rate.getCurrency()))
            {
                rateClone.setCurrency(p_rate.getCurrency());
            }

            HibernateUtil.update(rateClone);
        }
        catch (UserManagerException ume)
        {
            c_logger.error("UserManagerException when modifying a rate "
                    + "and checking for a duplicate name.", ume);
            String args[] =
            { Long.toString(p_rate.getId()) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_MODIFY_RATE, args, ume);
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception while modifying rate "
                    + p_rate.getId(), pe);
            String args[] =
            { Long.toString(p_rate.getId()) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_MODIFY_RATE, args, pe);
        }
    }

    /**
     * @see CostingEngine.deleteRatesOnRole(Role)
     */
    public void deleteRatesOnRole(Role p_role) throws CostingException,
            RemoteException
    {
        try
        {
            Collection rates = p_role.getRates();
            // PersistenceService.getInstance().deleteObjects(rates);
            HibernateUtil.delete(rates);
        }
        catch (Exception pe)
        {
            c_logger.error("Exception when deleting all the rates on role "
                    + p_role.getName(), pe);
            String msgArgs[] =
            { p_role.getName() };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_DELETE_RATES, msgArgs, pe);
        }
    }

    /**
     * @see CostingEngine.getRates
     */
    public Collection getRates() throws RemoteException, CostingException
    {
        Collection rates = null;
        try
        {
            // rates = PersistenceService.getInstance().executeNamedQuery(
            // RateQueryNames.ALL_RATES,
            // CompanyWrapper.addCompanyIdBoundArgs(new Vector()), false);
            String hql = "from Rate r where r.isActive='Y'";
            String currentId = CompanyThreadLocal.getInstance().getValue();
            HashMap map = new HashMap();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                hql += " and r.activity.companyId = :companyId";
                map = new HashMap();
                map.put("companyId", Long.parseLong(currentId));
            }
            rates = HibernateUtil.search(hql, map);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "Persistence Exception while querying or all active rates.",
                    pe);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_RATES, null, pe);
        }
        return rates;
    }

    public void removeRate(long p_id)
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            Rate rate = getRate(p_id);
            if (rate != null)
            {
                rate.setIsActive(false);
                session.save(rate);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
                c_logger.error(e.getMessage(), e);
            }
            catch (Exception e2)
            {
            }
        }
    }

    /**
     * @see CostingEngine.getRate(long)
     */
    public Rate getRate(long p_id) throws RemoteException, CostingException
    {

        Rate rate = null;
        try
        {
            rate = (Rate) HibernateUtil.get(Rate.class, p_id);
        }
        catch (Exception pe)
        {
            c_logger.error("Persistence Exception when getting rate " + p_id,
                    pe);
            String args[] =
            { Long.toString(p_id) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_RATE, args, pe);
        }
        return rate;
    }

    /**
     * @see CostingEngine.getRates(Activity, GlobalSightLocale,
     *      GlobalSightLocale)
     */
    public Collection getRates(Activity p_activity,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
            throws RemoteException, CostingException
    {
        Role cr = null;

        try
        {
            // query user manager for the specific role
            cr = getUserManager().getContainerRole(p_activity,
                    p_sourceLocale.toString(), p_targetLocale.toString());
        }
        catch (UserManagerException ume)
        {
            StringBuffer sb = new StringBuffer(
                    "User Manager Exception when retrieving rates on");
            sb.append(" roles with activity ");
            sb.append(p_activity.getName());
            sb.append(" and source locale ");
            sb.append(p_sourceLocale.getDisplayName());
            sb.append(" and target locale ");
            sb.append(p_targetLocale.getDisplayName());
            c_logger.error(sb.toString(), ume);
            String args[] =
            { p_activity.getName(), p_sourceLocale.getDisplayName(),
                    p_targetLocale.getDisplayName() };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_RATES_ON_ROLE,
                    args, ume);
        }

        
        if (cr == null)
        {
        	c_logger.error("Can not find the role for activity: " + p_activity.getActivityName() + " with locale " +  p_targetLocale.toString());
        }
        
        // use the rate ids to
        // retrieve the rates.
        return cr.getRates();
    }

    /**
     * @see CostingEngine.getRates(GlobalSightLocale, GlobalSightLocale)
     */
    public Hashtable getRates(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale) throws RemoteException,
            CostingException
    {
        Hashtable ratesOnActivity = new Hashtable();
        Collection roles = null;

        try
        {
            // query user manager for the specific role
            roles = getUserManager().getContainerRoles(
                    p_sourceLocale.toString(), p_targetLocale.toString());
        }
        catch (UserManagerException ume)
        {
            StringBuffer sb = new StringBuffer(
                    "User Manager Exception when retrieving rates on");
            sb.append(" roles with source locale ");
            sb.append(p_sourceLocale.getDisplayName());
            sb.append(" and target locale ");
            sb.append(p_targetLocale.getDisplayName());
            c_logger.error(sb.toString(), ume);
            String args[] =
            { p_sourceLocale.getDisplayName(), p_targetLocale.getDisplayName() };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_RATES_ON_LOCALES,
                    args, ume);
        }
        if (roles != null && roles.size() > 0)
        {
            // get the rates and place in hashtable appropriately
            for (Iterator i = roles.iterator(); i.hasNext();)
            {
                Role r = (Role) i.next();
                Activity a = r.getActivity();
                
                if (a == null)
                {
                	c_logger.error("Can not find the activity for role: " + r.getName());
                	continue;
                }
                
                Collection<Rate> rs = r.getRates();
                List<Rate> rRs = new ArrayList<Rate>();
                for (Rate r1 : rs)
                {
                	if (!r1.isActive())
                	{
                		rRs.add(r1);
                	}
                }
                rs.removeAll(rRs);
                
                if (ratesOnActivity.containsKey(a.getActivityName()))
                {
                    Vector rates = (Vector) ratesOnActivity.get(a
                            .getActivityName());
                    rates.addAll(rs);
                    // override what was previously there with the new
                    // collection
                    ratesOnActivity.put(a.getActivityName(), rates);
                }
                else
                // activity not in hashtable yet
                {
                    Vector rates = new Vector(rs);
                    ratesOnActivity.put(a.getActivityName(), rates);
                }
            }

        }
        return ratesOnActivity;
    }

    /**
     * @see CostingEngine.getRates(GlobalSightLocale, GlobalSightLocale)
     */
    public Hashtable getRates(GlobalSightLocale p_sourceLocale)
            throws RemoteException, CostingException
    {
        // get all the target locales that are associated with this source
        Vector targetLocales = null;
        try
        {
            targetLocales = getLocaleManager().getTargetLocales(p_sourceLocale);
        }
        catch (LocaleManagerException lme)
        {
            c_logger.error(
                    "Couldn't retrieve all the target locales for source "
                            + p_sourceLocale.getDisplayName()
                            + " when getting rates.", lme);
            String args[] =
            { p_sourceLocale.getDisplayName() };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_GET_TARGET_LOCALES_TO_FIND_RATES,
                    args, lme);
        }

        Hashtable allRates = new Hashtable(targetLocales.size());

        // get rates for each combination and add to the hashtable
        for (int i = 0; i < targetLocales.size(); i++)
        {
            // get the rates for the source/target locale
            Hashtable rates = getRates(p_sourceLocale,
                    (GlobalSightLocale) targetLocales.get(i));
            if (rates == null)
            {
                rates = new Hashtable();
            }
            allRates.put(
                    ((GlobalSightLocale) targetLocales.get(i)).getIdAsLong(),
                    rates);
        }
        return allRates;
    }

    /**
     * @see CostingEngine.overrideCost(WorkObject, float)
     */
    public Cost overrideCost(WorkObject p_obj, float p_overrideCost,
            int p_costType) throws RemoteException, CostingException
    {
        Cost c = null;
        try
        {
            c = getCost(p_obj, p_costType);
            c.setOverrideCost(p_overrideCost);
            HibernateUtil.saveOrUpdate(c);
        }
        catch (Exception e)
        {
            c_logger.error("Failed to override the cost to " + p_overrideCost,
                    e);

            String args[] =
            { Float.toString(p_overrideCost) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_OVERRIDE_COST, args, e);
        }

        return c;
    }

    /**
     * @see CostingEngine.clearOverrideCost(WorkObject)
     */
    public Cost clearOverrideCost(WorkObject p_obj, int p_costType)
            throws RemoteException, CostingException
    {
        Cost c = null;
        try
        {
            c = getCost(p_obj, p_costType);
            c.clearOverrideCost();
            HibernateUtil.saveOrUpdate(c);
        }
        catch (Exception e)
        {
            c_logger.error("Failed to clear the override the cost.", e);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_CLEAR_OVERRIDE_COST, null, e);
        }
        return c;
    }

    /**
     * @see CostingEngine.addSurcharge(long, Surcharge)
     */
    public Cost addSurcharge(long p_costId, Surcharge p_newSurcharge,
            int p_costType) throws RemoteException, CostingException
    {
        Cost c = null;
        try
        {
            // find the cost
            c = (Cost) HibernateUtil.get(Cost.class, p_costId);
            c.addSurcharge(p_newSurcharge);
            HibernateUtil.saveOrUpdate(c);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "Failed to add surcharge " + p_newSurcharge.getName()
                            + " to the cost " + p_costId, pe);
            String args[] =
            { p_newSurcharge.getName(), Long.toString(p_costId) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_ADD_SURCHARGE, args, pe);
        }
        return c;
    }

    /**
     * @see CostingEngine.modifySurcharge(long, String, Surcharge, int)
     */
    public Cost modifySurcharge(long p_costId, String p_surchargeOldName,
            Surcharge p_modifiedSurcharge, int p_costType)
            throws RemoteException, CostingException
    {
        Cost c = null;
        try
        {
            // find the cost
            c = (Cost) HibernateUtil.get(Cost.class, p_costId);
            c.modifySurcharge(p_surchargeOldName, p_modifiedSurcharge);
            HibernateUtil.update(c);
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "Failed to modifiy surcharge "
                            + p_modifiedSurcharge.getName() + " to the cost "
                            + p_costId, pe);
            String args[] =
            { p_modifiedSurcharge.getName(), Long.toString(p_costId) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_ADD_SURCHARGE, args, pe);
        }
        return c;
    }

    /**
     * @see CostingEngine.removeSurcharge(long, String, int)
     */

    public Cost removeSurcharge(long p_costId, String p_surchargeName,
            int p_costType) throws RemoteException, CostingException
    {
        Cost c = null;
        try
        {
            // find the cost
            c = (Cost) HibernateUtil.get(Cost.class, p_costId);
            // remove it from the list
            Surcharge s = c.removeSurcharge(p_surchargeName);
            // / remove from the database
            HibernateUtil.update(c);
            HibernateUtil.delete(s);
        }
        catch (Exception pe)
        {
            c_logger.error("Failed to remove surcharge " + p_surchargeName
                    + " from cost " + p_costId, pe);
            String args[] =
            { p_surchargeName, Long.toString(p_costId) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_REMOVE_SURCHARGE, args, pe);
        }
        return c;
    }

    /**
     * @see CostingEngine.setEstimatedAmountOfWorkInJob(long, int, float)
     */
    public void setEstimatedAmountOfWorkInJob(long p_jobId, int p_unitOfWork,
            float p_amount) throws RemoteException, CostingException
    {
        setEstimatedAmountOfWorkInJob(p_jobId, p_unitOfWork, p_amount,
                Cost.EXPENSE);
        setEstimatedAmountOfWorkInJob(p_jobId, p_unitOfWork, p_amount,
                Cost.REVENUE);
    }

    private void setEstimatedAmountOfWorkInJob(long p_jobId, int p_unitOfWork,
            float p_amount, int p_costType) throws CostingException
    {
        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            // find all tasks with rates of the particular unit of work
            Collection tasks = getTaskManager().getTasks(p_jobId,
                    new Integer(p_unitOfWork), new Integer(p_costType));

            // loop through them and create/set the AmountOfWork
            for (Iterator taskI = tasks.iterator(); taskI.hasNext();)
            {
                Task t = (Task) taskI.next();
                AmountOfWork aow = t.getAmountOfWork(new Integer(p_unitOfWork));
                if (aow != null) // not new
                {
                    aow.setEstimatedAmount(p_amount);
                    t.setAmountOfWork(aow);
                }
                else
                // is new
                {
                    Rate r = getActualRateToBeUsed(t);
                    if (r == null)
                    {
                        if ((t.getExpenseRate() != null)
                                && ((Integer) t.getExpenseRate().getRateType())
                                        .intValue() == p_unitOfWork)
                        {
                            aow = t.getExpenseRate().createAmountOfWork();
                        }
                        else
                        {
                            if ((t.getRevenueRate() != null)
                                    && ((Integer) t.getRevenueRate()
                                            .getRateType()).intValue() == p_unitOfWork)
                            {
                                aow = t.getRevenueRate().createAmountOfWork();
                            }
                        }
                    }
                    else
                    {
                        // if the rate isn't the right type then look to the
                        // revenue
                        if (r.getRateType().intValue() != p_unitOfWork)
                        {
                            // if the actual rate to be used isn't of the right
                            // type - then
                            // check if it applies to the revenue
                            if ((t.getRevenueRate() != null)
                                    && ((Integer) t.getRevenueRate()
                                            .getRateType()).intValue() == p_unitOfWork)
                            {
                                aow = t.getRevenueRate().createAmountOfWork();
                            }
                        }
                        else
                        {
                            aow = r.createAmountOfWork();
                        }
                    }
                    if (aow != null) // if one created - because valid
                    {
                        aow.setEstimatedAmount(p_amount);
                        t.setAmountOfWork(aow);
                    }
                }

                session.saveOrUpdate(t);
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            c_logger.error(
                    "Failed to set the estimated amount " + p_amount
                            + " of work of type " + p_unitOfWork + " on job "
                            + p_jobId, e);
            String args[] =
            { Float.toString(p_amount), Integer.toString(p_unitOfWork),
                    Long.toString(p_jobId) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_SET_AOW_IN_JOB, args, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * @see CostingEngine.getEstimatedAmountOfWorkInJob(long, int, int)
     */
    public float getEstimatedAmountOfWorkInJob(long p_jobId, int p_unitOfWork,
            int p_costType) throws RemoteException, CostingException
    {
        try
        {
            float estimatedAmount = 0;
            // find all tasks with rates of the particular unit of work
            Collection tasks = getTaskManager().getTasks(p_jobId,
                    new Integer(p_unitOfWork), new Integer(p_costType));

            estimatedAmount = -1;
            Job job = null;
            // if page count then get the default value
            if (p_unitOfWork == Rate.UnitOfWork.PAGE_COUNT.intValue())
            {
                // get the actual number of pages in the job - used as the
                // default if it hasn't been set yet.
                try
                {
                    job = ServerProxy.getJobHandler().getJobById(p_jobId);
                }
                catch (Exception e)
                {
                    c_logger.error("Exception while retrieving job ", e);
                }
                estimatedAmount = job.getPageCount();
            }
            if (tasks.size() != 0)
            {
                float totalEstimatedAmount = 0;
                // loop through the tasks and add up the amounts
                Integer unitOfWork = new Integer(p_unitOfWork);
                int numOfTasks = 0;
                Task t = null;
                for (Iterator taskI = tasks.iterator(); taskI.hasNext();)
                {
                    t = (Task) taskI.next();
                    AmountOfWork aow = t.getAmountOfWork(unitOfWork);
                    if (aow != null)
                    {
                        totalEstimatedAmount += aow.getEstimatedAmount();
                        numOfTasks++;
                    }
                }
                if (numOfTasks > 0)
                {
                    // return the average
                    estimatedAmount = (float) (totalEstimatedAmount / numOfTasks);
                }
            }
            return estimatedAmount;
        }
        catch (Exception e)
        {
            c_logger.error("Failed to get the estimated amount of type "
                    + p_unitOfWork + " of job " + p_jobId, e);
            String[] args =
            { Integer.toString(p_unitOfWork), Long.toString(p_jobId) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_GET_ESTIMATED_AOW_ON_JOB,
                    args, e);
        }
    }

    /**
     * @see CostingEngine.setEstimatedAmountOfWork(long, int, float)
     */
    public void setEstimatedAmountOfWork(long p_taskId, int p_unitOfWork,
            float p_amount) throws RemoteException, CostingException
    {
        try
        {
            Task t = getTaskManager().getTask(p_taskId);
            if (t != null)
            {
                AmountOfWork aow = t.getAmountOfWork(new Integer(p_unitOfWork));

                if (aow != null) // not new
                {
                    aow.setEstimatedAmount(p_amount);
                    t.setAmountOfWork(aow);
                }
                else
                {
                    Rate r = getActualRateToBeUsed(t);
                    if (r == null)
                    {
                        if ((t.getExpenseRate() != null)
                                && ((Integer) t.getExpenseRate().getRateType())
                                        .intValue() == p_unitOfWork)
                        {
                            aow = t.getExpenseRate().createAmountOfWork();
                        }
                        else
                        {
                            if ((t.getRevenueRate() != null)
                                    && ((Integer) t.getRevenueRate()
                                            .getRateType()).intValue() == p_unitOfWork)
                            {
                                aow = t.getRevenueRate().createAmountOfWork();
                            }
                        }
                    }
                    else
                    {
                        // if the rate isn't the right type then look to the
                        // revenue
                        if (r.getRateType().intValue() != p_unitOfWork)
                        {
                            // if the actual rate to be used isn't of the right
                            // type - then
                            // check if it applies to the revenue
                            if ((t.getRevenueRate() != null)
                                    && ((Integer) t.getRevenueRate()
                                            .getRateType()).intValue() == p_unitOfWork)
                            {
                                aow = t.getRevenueRate().createAmountOfWork();
                            }
                        }
                        else
                        {
                            aow = r.createAmountOfWork();
                        }
                    }

                    if (aow != null)
                    {
                        aow.setEstimatedAmount(p_amount);
                        t.setAmountOfWork(aow);
                    }
                    else
                    // if NULL then the unit of work isn't valid
                    {
                        c_logger.error("Failed to set an estimated amount of work because the type "
                                + p_unitOfWork + " is invalid.");
                        String[] args =
                        { Integer.toString(p_unitOfWork) };
                        throw new CostingException(
                                CostingException.MSG_INVALID_ESTIMATED_UNIT_OF_WORK,
                                args, null);
                    }
                }
                HibernateUtil.saveOrUpdate(t);
            }
            else
            // task was NULL
            {
                throw new CostingException(
                        CostingException.MSG_FAILED_TO_SET_ESTIMATED_WORK,
                        null, null);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to set the estimated amount " + p_amount
                    + " of type " + p_unitOfWork + " in task " + p_taskId, e);
            String[] args =
            { Float.toString(p_amount), Integer.toString(p_unitOfWork),
                    Long.toString(p_taskId) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_SET_ESTIMATED_WORK, args, e);
        }
    }

    /**
     * @see CostingEngine.addActualAmountOfWork(long, int, float)
     */
    public void setActualAmountOfWork(long p_taskId, int p_unitOfWork,
            float p_amount) throws RemoteException, CostingException
    {
        // UnitOfWork uow = null;
        try
        {
            Task t = getTaskManager().getTask(p_taskId);
            if (t != null)
            {
                AmountOfWork aow = t.getAmountOfWork(new Integer(p_unitOfWork));

                if (aow != null) // not new
                {
                    aow.setActualAmount(p_amount);
                    t.setAmountOfWork(aow);
                }
                else
                // is new
                {
                    int costType = Cost.EXPENSE;
                    Rate r = getActualRateToBeUsed(t);
                    if (r == null)
                    {
                        // is the expense rate the
                        if ((t.getExpenseRate() != null)
                                && ((Integer) t.getExpenseRate().getRateType())
                                        .intValue() == p_unitOfWork)
                        {
                            costType = Cost.EXPENSE;
                            aow = t.getExpenseRate().createAmountOfWork();
                        }
                        else
                        {
                            if ((t.getRevenueRate() != null)
                                    && ((Integer) t.getRevenueRate()
                                            .getRateType()).intValue() == p_unitOfWork)
                            {
                                costType = Cost.REVENUE;
                                aow = t.getRevenueRate().createAmountOfWork();
                            }
                        }

                    }
                    else
                    {
                        // if the rate type is not the same as the unit of work
                        // then
                        // check if it is for the revenue
                        if (r.getRateType().intValue() != p_unitOfWork)
                        {
                            if ((t.getRevenueRate() != null)
                                    && ((Integer) t.getRevenueRate()
                                            .getRateType()).intValue() == p_unitOfWork)
                            {
                                costType = Cost.REVENUE;
                                aow = t.getRevenueRate().createAmountOfWork();
                            }
                        }
                        else
                        {
                            aow = r.createAmountOfWork();
                        }
                    }

                    if (aow != null)
                    {
                        aow.setActualAmount(p_amount);

                        // if a page value - then set the estimated value too
                        if (p_unitOfWork == Rate.UnitOfWork.PAGE_COUNT
                                .intValue())
                        {
                            float estimatedNumOfPages = getEstimatedAmountOfWorkInJob(
                                    t.getWorkflow().getJob().getId(),
                                    Rate.UnitOfWork.PAGE_COUNT.intValue(),
                                    costType);
                            aow.setEstimatedAmount(estimatedNumOfPages);
                        }

                        t.setAmountOfWork(aow);
                    }
                    else
                    // if NULL then the unit of work isn't valid
                    {
                        c_logger.error("Failed to set an actual amount of work because the type "
                                + p_unitOfWork + " is invalid.");
                        String[] args =
                        { Integer.toString(p_unitOfWork) };
                        throw new CostingException(
                                CostingException.MSG_INVALID_ESTIMATED_UNIT_OF_WORK,
                                args, null);
                    }
                }

                HibernateUtil.saveOrUpdate(t);
            }
            else
            // task was NULL
            {
                throw new CostingException(
                        CostingException.MSG_FAILED_TO_SET_ACTUAL_WORK, null,
                        null);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to set the actual amount " + p_amount
                    + " of work of type " + p_unitOfWork + " on task "
                    + p_taskId, e);
            String args[] =
            { Float.toString(p_amount), Integer.toString(p_unitOfWork),
                    Long.toString(p_taskId) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_SET_ACTUAL_WORK, args, e);
        }
    }

    /**
     * @see CostingEngine.calculateCost(Job, boolean, int)
     */
    public Cost calculateCost(Job p_job, boolean p_recalculate, int p_costType)
            throws RemoteException, CostingException
    {
        return calculateCost(p_job, getPivotCurrency(), p_recalculate,
                p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(long, Currency, boolean, int)
     */
    public void calculateCost(long p_jobId, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException
    {
        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
            calculateCost(job, p_currency, p_recalculate, p_costType);
        }
        catch (Exception e)
        {
            c_logger.error("Failed to get job ", e);
        }
    }

    /**
     * @see CostingEngine.calculateCost(Job, Currency, boolean, int)
     */
    public Cost calculateCost(Job p_job, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException
    {
        Cost totalCost = null;
        Session session = HibernateUtil.getSession();
        Transaction tx = HibernateUtil.getTransaction();

        try
        {
            // verify that this job should have a cost.
            // and it isn't in the cancelled or import failed state.
            // TBD: - check if costing turned on at system level??
            if (!p_job.getState().equals(Job.CANCELLED)
                    && !p_job.getState().equals(Job.IMPORTFAILED))
            {
                boolean completeJob = (p_job.getState().equals(Job.EXPORTED)
                        || p_job.getState().equals(Job.ARCHIVED) || p_job
                        .getState().equals(Job.EXPORT_FAIL));
                boolean recalculateWorkflows = p_recalculate;
                totalCost = calculateCost(p_job, p_currency, p_recalculate,
                        session, p_costType, completeJob, recalculateWorkflows);
            }
            else if (p_job.getState().equals(Job.IMPORTFAILED))
            {
                totalCost = new Cost(p_job, Cost.ZERO_COST, Cost.ZERO_COST,
                        Cost.ZERO_COST, p_currency, p_costType);
            }

            HibernateUtil.commit(tx);
        }
        catch (Exception e)
        {
            HibernateUtil.rollback(tx);
            c_logger.error(
                    "Failed to calculate the cost for job " + p_job.getId(), e);
            String args[] =
            { Long.toString(p_job.getId()) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_COST_A_JOB, args, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
        return totalCost;
    }

    /**
     * Re-sets the job's total cost based on the workflow costs. This does not
     * recalculate workflow costs.
     */
    public Cost reCostJob(Job p_job, Currency p_currency, int p_costType)
            throws RemoteException, CostingException
    {
        Cost totalCost = null;
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            // verify that this job should have a cost.
            // and it isn't in the cancelled or import failed state.
            // TBD: - check if costing turned on at system level??
            if (!p_job.getState().equals(Job.CANCELLED)
                    && !p_job.getState().equals(Job.IMPORTFAILED))
            {
                boolean completeJob = false; // pretend it's not complete
                boolean recalculateJob = true;
                boolean recalculateWorkflows = false;
                totalCost = calculateCost(p_job, p_currency, recalculateJob,
                        session, p_costType, completeJob, recalculateWorkflows);

                tx.commit();
            }
            else if (p_job.getState().equals(Job.IMPORTFAILED))
            {
                totalCost = new Cost(p_job, Cost.ZERO_COST, Cost.ZERO_COST,
                        Cost.ZERO_COST, p_currency, p_costType);
            }
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            c_logger.error("Failed to re-cost job " + p_job.getId(), e);
            String args[] =
            { Long.toString(p_job.getId()) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_COST_A_JOB, args, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
        return totalCost;
    }

    /**
     * @see CostingEngine.calculateCost(Workflow, Currency, boolean, int)
     */
    public Cost calculateCost(Workflow p_workflow, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException
    {
        return calculateCost(p_workflow, p_currency, p_recalculate, p_costType,
                false);
    }

    /**
     * @see CostingEngine.calculateCost(Workflow p_workflow, Currency
     *      p_currency, boolean p_recalculate, int p_costType, boolean
     *      p_recalcFinishedWorkflow)
     */
    public Cost calculateCost(Workflow p_workflow, Currency p_currency,
            boolean p_recalculate, int p_costType,
            boolean p_recalcFinishedWorkflow) throws RemoteException,
            CostingException
    {
        Cost totalCost = null;
        Session session = HibernateUtil.getSession();

        Transaction tx = session.beginTransaction();
        try
        {
            // if the workflow is not cancelled or in
            // the import failed state then costing is allowed
            // calculate its cost
            // tbd - check if system set up with costing
            if (!p_workflow.getState().equals(Workflow.CANCELLED)
                    && !p_workflow.getState().equals(Workflow.IMPORT_FAILED))
            {
                // now create the unit of work - transaction
                // UnitOfWork uow = PersistenceService.getInstance()
                // .acquireUnitOfWork();

                totalCost = calculateCost(p_workflow, p_currency, false,
                        session, p_costType, p_recalcFinishedWorkflow);
                // commit the entire transaction
                // uow.commit();
            }
            else if (p_workflow.getState().equals(Workflow.IMPORT_FAILED))
            {
                totalCost = new Cost(p_workflow, Cost.ZERO_COST,
                        Cost.ZERO_COST, Cost.ZERO_COST, p_currency, p_costType);
            }
            tx.commit();
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            c_logger.error("Failed to cost workflow " + p_workflow.getId(), e);
            String args[] =
            { Long.toString(p_workflow.getId()) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_COST_A_WORKFLOW, args, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
        return totalCost;
    }

    /**
     * @see CostingEngine.calculateCost(Workflow, boolean, int)
     */
    public Cost calculateCost(Workflow p_workflow, boolean p_recalculate,
            int p_costType) throws RemoteException, CostingException
    {
        return calculateCost(p_workflow, getPivotCurrency(), p_recalculate,
                p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Task, Currency, boolean, int)
     */
    public Cost calculateCost(Task p_task, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException
    {
        Cost totalCost = null;
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            // create the unit of work - transaction
            // UnitOfWork uow = PersistenceService.getInstance()
            // .acquireUnitOfWork();
            totalCost = calculateCost(p_task, p_currency, p_recalculate,
                    session, p_costType, false);
            // commit the entire transaction
            // uow.commit();
            tx.commit();
        }
        catch (Exception e)
        {
            if (tx != null)
            {
                tx.rollback();
            }
            c_logger.error("Failed to cost task " + p_task.getId(), e);
            String args[] =
            { Long.toString(p_task.getId()) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_COST_A_TASK, args, e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
        return totalCost;
    }

    /**
     * @see CostingEngine.calculateCost(Task, boolean, int)
     */
    public Cost calculateCost(Task p_task, boolean p_recalculate, int p_costType)
            throws RemoteException, CostingException
    {
        return calculateCost(p_task, getPivotCurrency(), p_recalculate,
                p_costType);
    }

    // ------------------ private methods ---------------------

    /**
     * Calculate the cost of a job.
     */
    private Cost calculateCost(Job p_job, Currency p_currency,
            boolean p_recalculateJob, Session p_session, int p_costType,
            boolean p_isJobComplete, boolean p_recalculateWorkflows)
            throws CostingException
    {

        // get the cost for the job
        Cost totalCost = getCost(p_job, p_costType);
        totalCost = (Cost) p_session.get(Cost.class, totalCost.getIdAsLong());

        p_currency = (Currency) p_session.get(Currency.class,
                p_currency.getIdAsLong());
        totalCost = totalCost.convert(p_currency);

        totalCost.isUseInContext = PageHandler.isInContextMatch(p_job);

        // if the cost should be recalculated
        if (p_recalculateJob && !p_isJobComplete)
        {
            // clear out the estimated and actual costs to start re-calculating.
            totalCost.setEstimatedCost(Cost.ZERO_COST);
            totalCost.setNoUseEstimatedCost(Cost.ZERO_COST);
            totalCost.setDefaultContextEstimatedCost(Cost.ZERO_COST);
            totalCost.setActualCost(Cost.ZERO_COST);
            totalCost.setType(p_costType);

            CostByWordCount jobCostByWordCount = new CostByWordCount(totalCost);
            boolean hasWordCountCostBreakdown = false;
            for (Iterator i = p_job.getWorkflows().iterator(); i.hasNext();)
            {
                Workflow w = (Workflow) i.next();
                if (!w.getState().equals(Workflow.CANCELLED)
                        && !w.getState().equals(Workflow.IMPORT_FAILED))
                {
                    Cost workflowCost = calculateCost(w, p_currency,
                            p_recalculateWorkflows, p_session, p_costType,
                            false);

                    // record every workflow's costWordCount,
                    // and when need them in the workflow calculate cost,
                    // don't calculate them again.
                    totalCost.addworkflowCost(w.getId(), workflowCost);

                    totalCost = totalCost.add(workflowCost);
                    totalCost.isUseInContext = PageHandler
                            .isInContextMatch(p_job);
                    CostByWordCount workflowCostByWordCount = workflowCost
                            .getCostByWordCount();
                    if (workflowCostByWordCount != null)
                    {
                        hasWordCountCostBreakdown = true;
                        jobCostByWordCount.add(workflowCostByWordCount);
                    }
                }
            }
            totalCost.calculateFinalCost();
            if (hasWordCountCostBreakdown)
            {
                CostByWordCount j = totalCost.getCostByWordCount();
                CostByWordCount cloneJ = null;
                if (j == null)
                {
                    cloneJ = jobCostByWordCount;
                }
                else
                {
                    cloneJ = j;
                    cloneJ.set(jobCostByWordCount);
                }
                p_session.saveOrUpdate(cloneJ);
                totalCost.setCostByWordCount(cloneJ);
            }
            else
            {
                CostByWordCount j = totalCost.getCostByWordCount();
                if (j != null)
                {
                    totalCost.setCostByWordCount(null);
                    p_session.delete(j);
                }
            }
        }
        else
        {
            // else :: the cost is fine, only load workflow cost for usage.
            for (Iterator wfIter = p_job.getWorkflows().iterator(); wfIter
                    .hasNext();)
            {
                Workflow wf = (Workflow) wfIter.next();
                Cost wfCost = getCost(wf, p_costType);
                totalCost.addworkflowCost(wf.getId(), wfCost);
            }
        }

        p_session.saveOrUpdate(totalCost);
        return totalCost;
    }

    /**
     * Calculate the cost of a workflow.
     */
    private Cost calculateCost(Workflow p_workflow, Currency p_currency,
            boolean p_recalculate, Session p_session, int p_costType,
            boolean p_recalcFinishedWorkflow) throws CostingException
    {
        try
        {
            // get the cost
            Cost totalCost = getCost(p_workflow, p_costType);
            totalCost = (Cost) p_session.get(Cost.class,
                    totalCost.getIdAsLong());
            p_currency = (Currency) p_session.get(Currency.class,
                    p_currency.getIdAsLong());

            totalCost = totalCost.convert(p_currency);
            totalCost.isUseInContext = PageHandler.isInContextMatch(p_workflow
                    .getJob());

            boolean completeWorkflow = (p_workflow.getState().equals(
                    Workflow.EXPORTED)
                    || p_workflow.getState().equals(Workflow.EXPORT_FAILED) || p_workflow
                    .getState().equals(Workflow.ARCHIVED));

            // if the cost should be recalculated

            if ((p_recalculate && !completeWorkflow)
                    || (p_recalcFinishedWorkflow && completeWorkflow))
            {
                // clear out the estimated and actual
                // since these will be recalculated
                totalCost.setEstimatedCost(Cost.ZERO_COST);
                totalCost.setNoUseEstimatedCost(Cost.ZERO_COST);
                totalCost.setDefaultContextEstimatedCost(Cost.ZERO_COST);
                totalCost.setActualCost(Cost.ZERO_COST);
                totalCost.setType(p_costType);

                // get all the task ids for the path in the workflow
                long[] taskIds = getWorkflowServer().taskIdsInDefaultPath(
                        p_workflow.getId());

                Hashtable tasks = null;
                tasks = ((WorkflowImpl) p_workflow).getTasks();
                CostByWordCount workflowCostbyWordCount = new CostByWordCount(
                        totalCost);
                boolean hasWordCountCostBreakdown = false;
                for (int i = 0; i < taskIds.length; i++)
                {
                    Task t = (Task) tasks.get(new Long(taskIds[i]));

                    // if they are in the process of being deleted they will be
                    // NULL
                    if (t != null)
                    {
                        Cost taskCost = calculateCost(t, p_currency,
                                p_recalculate, p_session, p_costType,
                                p_recalcFinishedWorkflow);

                        CostByWordCount taskCostByWordCount = taskCost
                                .getCostByWordCount();
                        totalCost.addTaskCost(t.getId(), taskCost);
                        // totalCost.isUseInContext = p_workflow.getJob()
                        // .getL10nProfile().getTranslationMemoryProfile()
                        // .getIsContextMatchLeveraging();
                        totalCost.isUseInContext = PageHandler
                                .isInContextMatch(p_workflow.getJob());
                        totalCost = totalCost.add(taskCost);
                        if (taskCostByWordCount != null)
                        {
                            hasWordCountCostBreakdown = true;
                            workflowCostbyWordCount.add(taskCostByWordCount);
                        }
                    }
                }
                totalCost.calculateFinalCost();
                if (hasWordCountCostBreakdown)
                {
                    // now see if there is an existing workflow cost breakdown
                    CostByWordCount w = totalCost.getCostByWordCount();
                    CostByWordCount cloneWorkflowCostByWordCount = null;
                    if (w == null)
                    {
                        cloneWorkflowCostByWordCount = workflowCostbyWordCount;
                    }
                    else
                    {
                        cloneWorkflowCostByWordCount = w;
                        cloneWorkflowCostByWordCount
                                .set(workflowCostbyWordCount);
                    }
                    p_session.saveOrUpdate(cloneWorkflowCostByWordCount);
                    totalCost.setCostByWordCount(cloneWorkflowCostByWordCount);
                }
                else
                {
                    CostByWordCount w = totalCost.getCostByWordCount();
                    if (w != null)
                    {
                        // delete this cost
                        totalCost.setCostByWordCount(null);
                        p_session.delete(w);
                    }
                }
            }
            p_session.saveOrUpdate(totalCost);
            // else just return the cost that is set
            return totalCost;
        }
        catch (CostingException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            c_logger.error(
                    "WorkflowException when getting tasks to calculate cost for workflow "
                            + p_workflow.getId(), e);
            String args[] =
            { Long.toString(p_workflow.getId()) };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_GET_TASKS_FOR_COSTING, args,
                    e);
        }
    }

    /**
     * Calculate the cost of a task.
     */
    private Cost calculateCost(Task p_task, Currency p_currency,
            boolean p_recalculate, Session p_session, int p_costType,
            boolean p_recalcFinishedWorkflow) throws CostingException,
            RemoteException, TaskException, EnvoyServletException
    {
        try
        {
            Task t = (TaskImpl) p_session.get(TaskImpl.class,
                    new Long(p_task.getId()));
            Cost cost = getCost(t, p_costType);
            cost = (Cost) p_session.get(Cost.class, cost.getIdAsLong());
            boolean addToActualCost = false;
            // Cost costClone = (Cost) p_tlUow.registerObject(cost);
            // Currency currClone = (Currency)
            // p_tlUow.registerObject(p_currency);
            Currency currClone = (Currency) p_session.get(Currency.class,
                    new Long(p_currency.getId()));
            boolean isTaskAccepted = false;
            String userId = null;
            userId = p_task.getAcceptor();
            if (userId != null && !userId.equals("0"))
            {
                isTaskAccepted = true;
            }

            // If the task is not yet ACCEPTED the don't
            // consider it for Actual Cost calculations.

            // For some reason when this part is reached from
            // JobDetails page the getState() does not return
            // state as accpeted.
            // Note that the completed state is picked up properly.
            // Even when a state is not accepted getstate() returns
            // -1 => TASK_COMPLETED. So it's necessary to verify
            // accepted and completed dates exist to avoid any
            // ambiguity.
            if (isTaskAccepted
                    || (t.getState() == WorkflowConstants.TASK_COMPLETED
                            && t.getAcceptedDate() != null && t
                            .getCompletedDate() != null))
            {
                addToActualCost = true;
            }
            else
            {
                addToActualCost = false;
            }

            // convert to the correct currency
            cost = cost.convert(currClone);

            // if the workflow is complete then no reason to recalculate task
            // the task can be complete though and still need to be costed
            Workflow wf = t.getWorkflow();
            // don't check localized since the last task needs to be calculated
            // and may not happen till the workflow is moved to localized
            boolean completeWorkflow = (wf.getState().equals(Workflow.EXPORTED)
                    || wf.getState().equals(Workflow.EXPORT_FAILED) || wf
                    .getState().equals(Workflow.ARCHIVED));

            // if the cost should be recalculated
            if ((p_recalculate && !completeWorkflow)
                    || (p_recalcFinishedWorkflow && completeWorkflow))
            {
                // clear out the costs that will be recalculated
                cost.setEstimatedCost(Cost.ZERO_COST);
                cost.setNoUseEstimatedCost(Cost.ZERO_COST);
                cost.setDefaultContextEstimatedCost(Cost.ZERO_COST);
                cost.setActualCost(Cost.ZERO_COST);
                cost.setType(p_costType);
                Rate r = null;

                if (p_costType == Cost.EXPENSE)
                {
                    r = getActualRateToBeUsed(t);
                    if (r == null)
                    {
                        r = t.getExpenseRate();
                    }
                }
                else if (p_costType == Cost.REVENUE)
                {
                    r = t.getRevenueRate();
                }
                if (r != null)
                {
                    if (r.getRateType().equals(Rate.UnitOfWork.HOURLY))
                    {
                        cost = calculateByHour(cost, r,
                                t.getAmountOfWork(Rate.UnitOfWork.HOURLY),
                                addToActualCost);
                    }
                    else if (r.getRateType().equals(Rate.UnitOfWork.PAGE_COUNT))
                    {
                        // get the actual number of pages in the job - used as
                        // the
                        // default if it hasn't been set yet.
                        float defaultNumOfPages = getEstimatedAmountOfWorkInJob(
                                t.getWorkflow().getJob().getId(),
                                Rate.UnitOfWork.PAGE_COUNT.intValue(),
                                p_costType);
                        cost = calculateByPageCount(cost, r,
                                t.getAmountOfWork(Rate.UnitOfWork.PAGE_COUNT),
                                (int) defaultNumOfPages, addToActualCost);
                    }
                    else if (r.getRateType().equals(Rate.UnitOfWork.WORD_COUNT)
                            || r.getRateType().equals(
                                    Rate.UnitOfWork.WORD_COUNT_BY))
                    {
                        cost = calculateByWordCount(p_session, cost, r,
                                t.getWorkflow(), addToActualCost);
                    }
                    else if (r.getRateType().equals(Rate.UnitOfWork.FIXED))
                    {
                        cost = calculateByFixed(cost, r, addToActualCost);
                    }
                    else
                    {
                        // invalid - so just leave as '0'
                        c_logger.error("Invalid rate type " + r.getRateType()
                                + " left cost as '0' when calculating cost.");
                    }
                }
                // else - no rate - this is OK
                cost.calculateFinalCost();
            }
            cost.isUseInContext = PageHandler.isInContextMatch(wf.getJob());
            // else cost is fine
            p_session.saveOrUpdate(cost);
            return cost;
        }
        catch (TaskException te)
        {
            throw new EnvoyServletException(te);
        }
        catch (CostingException e)
        {
            throw e;
        }
        catch (Exception ge)
        {
            throw new EnvoyServletException(ge);
        }
    }

    /**
     * Return the cost using the hourly rate. Assumes the currencies within the
     * rate and cost object are the same.
     */
    private Cost calculateByHour(Cost p_cost, Rate p_rate, AmountOfWork p_aow,
            boolean addToActualCost)
    {
        if (p_rate.getRateType().equals(Rate.UnitOfWork.HOURLY))
        {
            // if there is an AmountOfWork - calculate the cost
            if (p_aow != null)
            {
                Currency rateCurrency = p_rate.getCurrency();
                Currency costCurrency = p_cost.getCurrency();
                p_cost.setEstimatedCost(Cost.convert((float) (p_rate
                        .getUnitRate() * p_aow.getEstimatedAmount()),
                        rateCurrency, costCurrency));
                if (addToActualCost)
                {
                    p_cost.setActualCost(Cost.convert((float) (p_rate
                            .getUnitRate() * p_aow.getActualAmount()),
                            rateCurrency, costCurrency));
                }
            }
        }
        return p_cost;
    }

    /**
     * Return the cost using the by page rate.
     */
    private Cost calculateByPageCount(Cost p_cost, Rate p_rate,
            AmountOfWork p_aow, int p_defaultNumOfPages, boolean addToActualCost)
    {
        if (p_rate.getRateType().equals(Rate.UnitOfWork.PAGE_COUNT))
        {
            double estimatedAmount = p_defaultNumOfPages;
            double actualAmount = 0;

            if (p_aow != null)
            {
                estimatedAmount = p_aow.getEstimatedAmount();
                actualAmount = p_aow.getActualAmount();
            }
            Currency rateCurrency = p_rate.getCurrency();
            Currency costCurrency = p_cost.getCurrency();
            p_cost.setEstimatedCost(Cost.convert(
                    (float) (p_rate.getUnitRate() * estimatedAmount),
                    rateCurrency, costCurrency));
            if (addToActualCost)
            {
                p_cost.setActualCost(Cost.convert(
                        (float) (p_rate.getUnitRate() * actualAmount),
                        rateCurrency, costCurrency));
            }

        }
        return p_cost;
    }

    /**
     * Return the cost using the word count rate.
     */
    private Cost calculateByWordCount(Session p_session, Cost p_cost,
            Rate p_rate, Workflow p_workflow, boolean addToActualCost)
    {
        Currency rateCurrency = p_rate.getCurrency();
        Currency costCurrency = p_cost.getCurrency();

        boolean isUseInContext = PageHandler.isInContextMatch(p_workflow
                .getJob());
        if (p_rate.getRateType().equals(Rate.UnitOfWork.WORD_COUNT)
                || p_rate.getRateType().equals(Rate.UnitOfWork.WORD_COUNT_BY))
        {
            // get word counts
            // Note: the adjusted workflow word counts (which include cross-file
            // repetition analysis) are stored on the workflow.
            // These adjusted word counts are what we want to cost off of.

            // all repetitions
            int repetitionCount = p_workflow.getRepetitionWordCount();
            // The fuzzy match category
            int inContextMatchCount = p_workflow.getInContextMatchWordCount();
            int noUseInContextMatchCount = p_workflow
                    .getNoUseInContextMatchWordCount();
            int totalExactMatchWordCount = p_workflow
                    .getTotalExactMatchWordCount();
            // The exact match category
            int contextMatchCount = p_workflow.getContextMatchWordCount();
            int segmentTmMatchCount = p_workflow.getSegmentTmWordCount();

            int defaultContextSegmentTmMatchCount = totalExactMatchWordCount
                    - contextMatchCount;

            int lowFuzzyMatchCount = p_workflow.getThresholdLowFuzzyWordCount();
            int medFuzzyMatchCount = p_workflow.getThresholdMedFuzzyWordCount();
            int medHiFuzzyMatchCount = p_workflow
                    .getThresholdMedHiFuzzyWordCount();
            int hiFuzzyMatchCount = p_workflow.getThresholdHiFuzzyWordCount();
            int noMatchCount = p_workflow.getThresholdNoMatchWordCount();

            // The areas where calculations are done (multiply, add, subtract)
            // should be changed to use BigDecimal for the actual calculation.
            float noMatchCost = BigDecimalHelper.multiply(noMatchCount,
                    p_rate.getNoMatchRate());
            float repetitionCost = BigDecimalHelper.multiply(repetitionCount,
                    p_rate.getRepetitionRate());
            // Fuzzy Match Categories
            float lowFuzzyMatchCost = BigDecimalHelper.multiply(
                    lowFuzzyMatchCount, p_rate.getLowFuzzyMatchRate());
            float medFuzzyMatchCost = BigDecimalHelper.multiply(
                    medFuzzyMatchCount, p_rate.getMedFuzzyMatchRate());
            float medHiFuzzyMatchCost = BigDecimalHelper.multiply(
                    medHiFuzzyMatchCount, p_rate.getMedHiFuzzyMatchRate());
            float hiFuzzyMatchCost = BigDecimalHelper.multiply(
                    hiFuzzyMatchCount, p_rate.getHiFuzzyMatchRate());
            float inContextMatchCost = BigDecimalHelper.multiply(
                    inContextMatchCount, p_rate.getInContextMatchRate());
            float noUseInContextMatchCost = BigDecimalHelper.multiply(
                    noUseInContextMatchCount, p_rate.getInContextMatchRate());
            float totalExactMatchCost = BigDecimalHelper.multiply(
                    totalExactMatchWordCount, p_rate.getSegmentTmRate());
            // Exact Match
            float contextMatchCost = BigDecimalHelper.multiply(
                    contextMatchCount, p_rate.getContextMatchRate());
            float segmentTmMatchCost = BigDecimalHelper.multiply(
                    segmentTmMatchCount, p_rate.getSegmentTmRate());
            float defaultContextSegmentTmMatchCost = BigDecimalHelper.multiply(
                    defaultContextSegmentTmMatchCount,
                    p_rate.getSegmentTmRate());
            // convert all the costs to the cost currency
            noMatchCost = Cost.convert(noMatchCost, rateCurrency, costCurrency);
            repetitionCost = Cost.convert(repetitionCost, rateCurrency,
                    costCurrency);
            lowFuzzyMatchCost = Cost.convert(lowFuzzyMatchCost, rateCurrency,
                    costCurrency);
            medFuzzyMatchCost = Cost.convert(medFuzzyMatchCost, rateCurrency,
                    costCurrency);
            medHiFuzzyMatchCost = Cost.convert(medHiFuzzyMatchCost,
                    rateCurrency, costCurrency);
            hiFuzzyMatchCost = Cost.convert(hiFuzzyMatchCost, rateCurrency,
                    costCurrency);
            inContextMatchCost = Cost.convert(inContextMatchCost, rateCurrency,
                    costCurrency);
            noUseInContextMatchCost = Cost.convert(noUseInContextMatchCost,
                    rateCurrency, rateCurrency);
            totalExactMatchCost = Cost.convert(totalExactMatchCost,
                    rateCurrency, rateCurrency);
            contextMatchCost = Cost.convert(contextMatchCost, rateCurrency,
                    costCurrency);
            segmentTmMatchCost = Cost.convert(segmentTmMatchCost, rateCurrency,
                    costCurrency);

            // The areas where calculations are done (multiply, add, subtract)
            // should be changed to use BigDecimal for the actual calculation.
            // Leverage In-Context Match
            float[] param =
            { noMatchCost, repetitionCost, lowFuzzyMatchCost,
                    medFuzzyMatchCost, medHiFuzzyMatchCost, hiFuzzyMatchCost,
                    // inContextMatchCost, contextMatchCost, segmentTmMatchCost
                    // };
                    inContextMatchCost, segmentTmMatchCost };
            float amount = BigDecimalHelper.add(param);
            // Leverage 100% Match
            float[] noUseParam =
            { noMatchCost, repetitionCost, lowFuzzyMatchCost,
                    medFuzzyMatchCost, medHiFuzzyMatchCost, hiFuzzyMatchCost,
                    // noUseInContextMatchCost, contextMatchCost,
                    noUseInContextMatchCost, totalExactMatchCost };
            float noUseAmount = BigDecimalHelper.add(noUseParam);
            // Leverage Default Match
            float[] defaultContextParam =
            { noMatchCost, repetitionCost, lowFuzzyMatchCost,
                    medFuzzyMatchCost, medHiFuzzyMatchCost, hiFuzzyMatchCost,
                    contextMatchCost, defaultContextSegmentTmMatchCost };
            float defaultContextAmount = BigDecimalHelper
                    .add(defaultContextParam);

            p_cost.setEstimatedCost(amount);
            p_cost.setNoUseEstimatedCost(noUseAmount);
            p_cost.setDefaultContextEstimatedCost(defaultContextAmount);

            CostByWordCount wordCountCost = p_cost.getCostByWordCount();
            if (wordCountCost == null)
            {
                wordCountCost = new CostByWordCount(p_cost, repetitionCost,
                        contextMatchCost, inContextMatchCost,
                        segmentTmMatchCost, lowFuzzyMatchCost,
                        medFuzzyMatchCost, medHiFuzzyMatchCost,
                        hiFuzzyMatchCost, noMatchCost, noUseInContextMatchCost,
                        totalExactMatchCost, defaultContextSegmentTmMatchCost);
                p_cost.setCostByWordCount(wordCountCost);
                p_session.save(wordCountCost);
            }
            else
            {
                CostByWordCount wordCountCost_temp = (CostByWordCount) p_session
                        .get(CostByWordCount.class, wordCountCost.getIdAsLong());
                wordCountCost_temp.setRepetitionCost(repetitionCost);
                wordCountCost_temp.setContextMatchCost(contextMatchCost);
                wordCountCost_temp.setInContextMatchCost(inContextMatchCost);
                wordCountCost_temp
                        .setNoUseInContextMatchCost(noUseInContextMatchCost);
                wordCountCost_temp.setNoUseExactMatchCost(totalExactMatchCost);
                wordCountCost_temp.setSegmentTmMatchCost(segmentTmMatchCost);
                wordCountCost_temp.setLowFuzzyMatchCost(lowFuzzyMatchCost);
                wordCountCost_temp.setMedFuzzyMatchCost(medFuzzyMatchCost);
                wordCountCost_temp.setMedHiFuzzyMatchCost(medHiFuzzyMatchCost);
                wordCountCost_temp.setHiFuzzyMatchCost(hiFuzzyMatchCost);
                wordCountCost_temp.setNoMatchCost(noMatchCost);
                wordCountCost_temp
                        .setDefaultContextExactMatchCost(defaultContextSegmentTmMatchCost);
                p_session.saveOrUpdate(wordCountCost_temp);
                p_cost.setCostByWordCount(wordCountCost_temp);
            }
            p_cost.isUseInContext = isUseInContext;
            if (addToActualCost)
            {
                p_cost.setActualCost((isUseInContext) ? amount : noUseAmount);
            }
        }
        return p_cost;
    }

    /**
     * Return the cost using the fixed rate.
     */
    private Cost calculateByFixed(Cost p_cost, Rate p_rate,
            boolean addToActualCost)
    {
        if (p_rate.getRateType().equals(Rate.UnitOfWork.FIXED))
        {
            Currency rateCurrency = p_rate.getCurrency();
            Currency costCurrency = p_cost.getCurrency();
            p_cost.setEstimatedCost(Cost.convert(p_rate.getUnitRate(),
                    rateCurrency, costCurrency));
            p_cost.setNoUseEstimatedCost(Cost.convert(p_rate.getUnitRate(),
                    rateCurrency, costCurrency));
            p_cost.setDefaultContextEstimatedCost(Cost.convert(
                    p_rate.getUnitRate(), rateCurrency, costCurrency));
            if (addToActualCost)
            {
                p_cost.setActualCost(Cost.convert(p_rate.getUnitRate(),
                        rateCurrency, costCurrency));
            }
        }
        return p_cost;
    }

    /**
     * Returns the cost associated with this specific WorkObject. If the cost
     * doesn't exist it'll create a new one(without persisting).
     */
    private Cost getCost(WorkObject p_obj, int p_costType)
            throws CostingException
    {
        Cost c = null;
        String queryName = null;
        String sql = null;
        Long objectId = null;
        String costType = Cost.getTypeAsString(p_costType);
        try
        {
            // find the right query to run
            if (p_obj instanceof Job)
            {
                sql = CostDescriptorModifier.COST_BY_JOB_ID_SQL;
                objectId = ((JobImpl) p_obj).getIdAsLong();
            }
            else if (p_obj instanceof Workflow)
            {
                sql = CostDescriptorModifier.COST_BY_WORKFLOW_ID_SQL;
                objectId = ((WorkflowImpl) p_obj).getIdAsLong();
            }
            else if (p_obj instanceof Task)
            {
                sql = CostDescriptorModifier.COST_BY_TASK_ID_SQL;
                objectId = ((TaskImpl) p_obj).getIdAsLong();
            }
            else
            {
                c_logger.error("Failed to retrieve cost associated with an object because "
                        + " the object is not a valid costable type.");
                // not supported
                throw new CostingException(
                        CostingException.MSG_INVALID_OBJ_TYPE, null, null);
            }

            Map map = new HashMap();
            map.put("oId", objectId);
            map.put("costRateType", costType);
            // get the cost of this costable object
            synchronized (LOCK)
            {
            	Iterator costI = HibernateUtil.searchWithSql(sql, map, Cost.class)
	                    .iterator();
	
	            if (costI.hasNext())
	            {
	                c = (Cost) costI.next();
	            }
	            else
	            // create one if it doesn't exist - using the system currency
	            {
	                String companyId = CompanyThreadLocal.getInstance().getValue();
	                Currency privot = (Currency) Currency.getPivotCurrencies().get(
	                        Long.parseLong(companyId));
	                c = new Cost(p_obj, Cost.ZERO_COST, Cost.ZERO_COST,
	                        Cost.ZERO_COST, privot, p_costType);
	                HibernateUtil.save(c);
	            }
            }
        }
        catch (Exception pe)
        {
            c_logger.error(
                    "Failed to retrieve cost associated with costable object "
                            + queryName + " with id " + objectId, pe);
            String args[] =
            { queryName, objectId.toString() };
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_RETRIEVE_COST_ON_OBJ, args,
                    pe);
        }
        return c;
    }

    /**
     * Wraps the code for getting the user manager and any exceptions.
     */
    private UserManager getUserManager() throws CostingException
    {
        UserManager um = null;

        try
        {
            um = ServerProxy.getUserManager();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Couldn't find the User Manager", ge);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_FIND_USER_MANAGER, null, ge);
        }

        return um;
    }

    /**
     * Wraps the code for getting the locale manager and any exceptions.
     */
    private LocaleManager getLocaleManager() throws CostingException
    {
        LocaleManager lm = null;

        try
        {
            lm = ServerProxy.getLocaleManager();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Couldn't find the Locale Manager", ge);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_FIND_LOCALE_MANAGER, null,
                    ge);
        }

        return lm;
    }

    /*
     * Wraps the code for getting the workflow server and any exceptions.
     */
    private WorkflowServer getWorkflowServer() throws CostingException
    {
        WorkflowServer ws = null;

        try
        {
            ws = ServerProxy.getWorkflowServer();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Couldn't find the WorkflowServer", ge);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_FIND_WORKFLOW_SERVER, null,
                    ge);
        }

        return ws;
    }

    /*
     * Wraps the code for getting the task manager and any exceptions.
     */
    private TaskManager getTaskManager() throws CostingException
    {
        TaskManager tm = null;

        try
        {
            tm = ServerProxy.getTaskManager();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Couldn't find the TaskManager", ge);
            throw new CostingException(
                    CostingException.MSG_FAILED_TO_FIND_TASKMANAGER, null, ge);
        }
        return tm;
    }

    private Rate getActualRateToBeUsed(Task t)
    {
        Rate useRate = null;
        // int selectionCriteria = 2; // use until accepted
        int selectionCriteria = t.getRateSelectionCriteria();
        User user = null;
        boolean isTaskAccepted = false;
        boolean isConsiderTask = false;
        String userId = null;
        userId = t.getAcceptor();
        if (userId != null && !userId.equals("0"))
        {
            isTaskAccepted = true;
        }

        // If the task is not yet ACCEPTED the don't
        // consider it for Actual Cost calculations.

        // For some reason when this part is reached from
        // JobDetails page the getState() does not return
        // state as accpeted.
        // Note that the completed state is picked up properly.
        if (isTaskAccepted
                || (t.getState() == WorkflowConstants.TASK_COMPLETED && t
                        .getCompletedDate() != null))
        {
            isConsiderTask = true;
        }

        if ((selectionCriteria == WorkflowConstants.USE_SELECTED_RATE_UNTIL_ACCEPTANCE)
                && isConsiderTask)
        {
            // find out who accepted the task
            try
            {
                String acceptor = t.getAcceptor();
                if (acceptor != null)
                {
                    user = getUserManager().getUser(acceptor);
                }
            }
            catch (Exception e)
            {
                c_logger.error("CostingEngineLocal::"
                        + "Problem getting user information for workflowtask",
                        e);
            }
            try
            {
                // Now find out what is the default rate for this user.
                if (user != null)
                {
                    // find out user role
                    Vector uRoles = new Vector(getUserManager().getUserRoles(
                            user));
                    String activity = t.getTaskName();
                    GlobalSightLocale source = t.getSourceLocale();
                    GlobalSightLocale target = t.getTargetLocale();

                    for (int i = 0; i < uRoles.size(); i++)
                    {
                        Role curRole = (Role) uRoles.get(i);
                        // Get the source and target locale for each role.
                        String sourceLocale = curRole.getSourceLocale();
                        String targetLocale = curRole.getTargetLocale();
                        Activity act = curRole.getActivity();
                        UserRole cRole = (UserRole) uRoles.get(i);

                        if (act.getActivityName().equals(activity)
                                && sourceLocale.equals(source.toString())
                                && targetLocale.equals(target.toString()))
                        {
                            // Found the userRole we are talking about
                            String cRate = cRole.getRate();
                            if (cRate != null)
                            {
                                Long rate = new Long(cRate);
                                useRate = getRate(rate.longValue());
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                c_logger.error("CostingEngineLocal::"
                        + "Problem getting user information", e);
            }
        }
        return useRate;
    }

    public float getFactor(Currency currency, String targetName,
            String companyId) throws RemoteException, CostingException,
            GeneralException
    {
        float targeFactor = getCurrencyFactorByName(targetName, companyId);
        return targeFactor / currency.getConversionFactor();
    }

    private float getCurrencyFactorByName(String targetName, String companyId)
    {

        StringBuilder sb = new StringBuilder();
        sb.append("select new Currency(c.conversionFactor) ");
        sb.append("from Currency c ");
        sb.append("where c.isoCurrency.name like :targetName ");
        sb.append("and c.companyId = :companyId");

        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery(sb.toString());
        query.setString("targetName", targetName + "%");
        query.setString("companyId", companyId);
        query.setMaxResults(1);
        Currency currency = (Currency) query.uniqueResult();
        transaction.commit();
        return currency.getConversionFactor();

    }

    public Currency getCurrencyByName(String targetName, String companyId)
            throws RemoteException, CostingException, GeneralException
    {
        StringBuilder sb = new StringBuilder();

        sb.append("from Currency c ");
        sb.append("where c.isoCurrency.name like :targetName ");
        sb.append("and c.companyId = :companyId");

        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery(sb.toString());
        query.setString("targetName", targetName + "%");
        query.setString("companyId", companyId);
        query.setMaxResults(1);
        Currency currency = (Currency) query.uniqueResult();
        transaction.commit();
        return currency;
    }
}
