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
package com.globalsight.everest.tuv;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.pageexport.UpdateTuvStatePersistenceCommand;

/**
 * This class represents an observer of events that affect Tuv. The callers
 * notify the observer of an event that could have an affect on the state of the
 * Tuv.
 */
public class TuvEventObserverLocal implements TuvEventObserver
{
    private static final Logger CATEGORY = Logger
            .getLogger(TuvEventObserverLocal.class.getName());

    static final TuvManagerLocal TUV_MANAGER_LOCAL = new TuvManagerLocal();

    /**
     * Constructs a TuvEventObserverLocal.
     */
    TuvEventObserverLocal()
    {
    }

    //
    // TuvEventObserver interface methods
    //

    /**
     * Notification that the target locale page of Tuvs has been exported.
     * 
     * @param p_targetTuvs
     *            target locale page Tuvs.
     * @throws TuvException
     *             when an error occurs.
     */
    public void notifyPageExportedEvent(Collection p_targetTuvs, long p_jobId)
            throws TuvException, RemoteException
    {
        updateStateOfTuvs(p_targetTuvs, TuvState.COMPLETE, p_jobId);

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = null;

            try
            {
                ids = TuvPersistenceHelper.getIds(p_targetTuvs);
            }
            catch (Throwable e)
            {
                CATEGORY.error("notifyPageExportedEvent in debug", e);
                ids = p_targetTuvs;
            }

            CATEGORY.debug("notifyPageExportedEvent p_targetTuvs="
                    + ids.toString());
        }
    }

    /**
     * Updates the state of target page TUVs that are LOCALIZED to COMPLETED
     * otherwise leaves the state alone. Should be called after an export for
     * update of a source page.
     * 
     * @param p_targetTuvs
     *            collection of target TUVs
     * @exception TuvException
     * @exception RemoteException
     */
    public void notifyPageExportedForUpdateEvent(Collection p_targetTuvs,
            long p_jobId) throws TuvException, RemoteException
    {
        // change the Tuv state LOCALIZED,
        // LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED and
        // EXACT_MATCH_LOCALIZED to COMPLETE
        Collection<TuvState> fromStates = new ArrayList<TuvState>();
        fromStates.add(TuvState.LOCALIZED);
        fromStates.add(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED);
        fromStates.add(TuvState.EXACT_MATCH_LOCALIZED);

        updateStateOfTuvs(p_targetTuvs, fromStates, TuvState.COMPLETE, p_jobId);

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = null;

            try
            {
                ids = TuvPersistenceHelper.getIds(p_targetTuvs);
            }
            catch (Throwable e)
            {
                CATEGORY.error("notifyPageExportedEvent in debug ", e);
                ids = p_targetTuvs;
            }

            CATEGORY.debug("notifyPageExportedForUpdateEvent p_targetTuvs="
                    + ids.toString());
        }
    }

    /**
     * Notification that the job of source locale page Tuvs has been exported.
     * 
     * @param p_sourceTuvs
     *            source locale page Tuvs.
     * @throws TuvException
     *             when an error occurs.
     */
    public void notifyJobExportedEvent(Collection p_sourceTuvs, long p_jobId)
            throws TuvException, RemoteException
    {
        updateStateOfTuvs(p_sourceTuvs, TuvState.COMPLETE, p_jobId);

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = null;

            try
            {
                ids = TuvPersistenceHelper.getIds(p_sourceTuvs);
            }
            catch (Throwable e)
            {
                CATEGORY.error("notifyJobExportedEvent in debug ", e);
                ids = p_sourceTuvs;
            }

            CATEGORY.debug("notifyJobExportedEvent p_targetTuvs="
                    + ids.toString());
        }
    }

    /**
     * Notification that the workflow task completed for this target locale page
     * of Tuvs.
     * 
     * @param p_targetTuvs
     *            target locale page Tuvs.
     * @param p_taskId
     *            task identifier that completed.
     * @throws TuvException
     *             when an error occurs.
     */
    public void notifyTaskCompleteEvent(Collection p_targetTuvs, long p_taskId)
            throws TuvException, RemoteException
    {
        createTaskTuvs(p_targetTuvs, p_taskId);

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = null;

            try
            {
                ids = TuvPersistenceHelper.getIds(p_targetTuvs);
            }
            catch (Throwable e)
            {
                CATEGORY.error("notifyTaskCompleteEvent in debug", e);
                ids = p_targetTuvs;
            }

            CATEGORY.debug("notifyTaskCompleteEvent" + " p_taskId="
                    + Long.toString(p_taskId) + " p_targetTuvs="
                    + ids.toString());
        }
    }

    //
    // PACKAGE METHODS
    //

    // update the state of the Tuvs
    static void updateStateOfTuvs(Collection p_tuvs, TuvState p_state,
            long p_jobId) throws TuvException
    {
        updateStateOfTuvs(p_tuvs, (Collection) null, p_state, p_jobId);
    }

    // update the state of the Tuvs if the state matches the from state
    static void updateStateOfTuvs(Collection p_tuvs, TuvState p_fromState,
            TuvState p_toState, long p_jobId) throws TuvException
    {
        Collection<TuvState> fromStates = new ArrayList<TuvState>();
        fromStates.add(p_fromState);

        updateStateOfTuvs(p_tuvs, fromStates, p_toState, p_jobId);
    }

    /**
     * Update the state of the Tuvs if the state matches the list of from states.
     * 
     * As Tuvs are using separated tables per company since 8.2.3,the Tuvs to 
     * be updated are limited to same company.
     * 
     * @param p_tuvs
     * @param p_fromStates
     * @param p_toState
     * @param p_companyId
     * @throws TuvException
     */
    static void updateStateOfTuvs(Collection p_tuvs, Collection p_fromStates,
            TuvState p_toState, long p_jobId) throws TuvException
    {
        Collection<TuvImpl> stateUpdateTuvs = new ArrayList<TuvImpl>();
        Collection<TuvImpl> stateAndCrcUpdateTuvs = new ArrayList<TuvImpl>();

        Connection connection = null;

        try
        {
            Iterator it = p_tuvs.iterator();
            while (it.hasNext())
            {
                TuvImpl ti = (TuvImpl) it.next();

                if (p_fromStates == null)
                {
                    ti.setState(p_toState);
                }
                else
                {
                    if (p_fromStates.contains(ti.getState()))
                    {
                        ti.setState(p_toState);
                    }
                    else
                    {
                        // do not update this TUV
                        continue;
                    }
                }

                if (p_toState.equals(TuvState.COMPLETE))
                {
                    if (ti.getExactMatchKey() == 0)
                    {
                        String exactMatchFormat = ti.getExactMatchFormat();
                        long crc = GlobalSightCrc.calculate(exactMatchFormat);
                        ti.setExactMatchKey(crc);

                        stateAndCrcUpdateTuvs.add(ti);
                    }
                    else
                    {
                        stateUpdateTuvs.add(ti);
                    }
                }
                else
                {
                    stateUpdateTuvs.add(ti);
                }
            }

            connection = PersistenceService.getInstance().getConnection();
            connection.setAutoCommit(false);
            UpdateTuvStatePersistenceCommand utspc = new UpdateTuvStatePersistenceCommand();
            if (stateUpdateTuvs.size() > 0)
            {
                utspc.setTuvsForStateUpdate(stateUpdateTuvs);
            }
            if (stateAndCrcUpdateTuvs.size() > 0)
            {
                utspc.setTuvsForStateAndCrcUpdate(stateAndCrcUpdateTuvs);
            }
            // The jobId is required.
            utspc.setJobId(p_jobId);
            utspc.persistObjects(connection);
            connection.commit();
        }
        catch (Exception e)
        {
            CATEGORY.error("updateStateOfTuvs(), p_tuvs={" + p_tuvs
                    + "}, p_state=" + p_toState, e);
            try
            {
                connection.rollback();
                throw new TuvException(
                        "FAIL: TuvEventObserverLocal.updateStateOfTuvs()", e);
            }
            catch (Exception se)
            {
                throw new TuvException(
                        "FAIL: TuvEventObserverLocal.updateStateOfTuvs()", se);
            }
        }
        finally
        {
            try
            {
                PersistenceService.getInstance().returnConnection(connection);
            }
            catch (Exception e)
            {
                CATEGORY.error("The connection could not be returned");
            }
        }

    }

    //
    // PRIVATE METHODS
    //

    private static void createTaskTuvs(Collection p_targetTuvs, long p_taskId)
            throws TuvException
    {
        TaskTuv taskTuv = null;
        ArrayList newTaskTuvs = new ArrayList(3);

        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            Task task = (Task) HibernateUtil.get(TaskImpl.class, p_taskId);

            Iterator it = p_targetTuvs.iterator();
            TaskTuv previousTaskTuv = null;
            while (it.hasNext())
            {
                TuvImpl tuvImpl = (TuvImpl) it.next();
                List previousTaskTuvs = TUV_MANAGER_LOCAL.getPreviousTaskTuvs(
                        tuvImpl.getId(), 1);
                if (!previousTaskTuvs.isEmpty())
                {
                    previousTaskTuv = (TaskTuv) previousTaskTuvs.get(0);
                }
                // copy the current Tuv, store as previous Tuv with new ID
                TuvImpl copyTuvImplClone = new TuvImpl(tuvImpl);
                copyTuvImplClone.setState(TuvState.OUT_OF_DATE);
                session.saveOrUpdate(copyTuvImplClone);
                taskTuv = new TaskTuv(
                        tuvImpl,
                        copyTuvImplClone,
                        (previousTaskTuv != null ? previousTaskTuv.getVersion() + 1
                                : 1), task);
                newTaskTuvs.add(taskTuv);
                session.save(taskTuv);
            }
            transaction.commit();
        }
        catch (Exception ex)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            Collection ids = null;
            try
            {
                ids = TuvPersistenceHelper.getIds(p_targetTuvs);
            }
            catch (Throwable e)
            {
                CATEGORY.error("createTaskTuvs " + e.toString(), e);
                ids = p_targetTuvs;
            }
            CATEGORY.error("createTaskTuvs " + " newTaskTuvs="
                    + (newTaskTuvs != null ? newTaskTuvs.toString() : "null")
                    + " " + ids.toString(), ex);
            throw new TuvException(ex.toString(), ex);
        }
    }
}
