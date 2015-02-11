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
    public void notifyPageExportedEvent(Collection p_targetTuvs)
            throws TuvException, RemoteException
    {
        updateStateOfTuvs(p_targetTuvs, TuvState.COMPLETE);

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
    public void notifyPageExportedForUpdateEvent(Collection p_targetTuvs)
            throws TuvException, RemoteException
    {
        // change the Tuv state LOCALIZED,
        // LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED and
        // EXACT_MATCH_LOCALIZED to COMPLETE
        Collection fromStates = new ArrayList();
        fromStates.add(TuvState.LOCALIZED);
        fromStates.add(TuvState.LEVERAGE_GROUP_EXACT_MATCH_LOCALIZED);
        fromStates.add(TuvState.EXACT_MATCH_LOCALIZED);

        updateStateOfTuvs(p_targetTuvs, fromStates, TuvState.COMPLETE);

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
    public void notifyJobExportedEvent(Collection p_sourceTuvs)
            throws TuvException, RemoteException
    {
        updateStateOfTuvs(p_sourceTuvs, TuvState.COMPLETE);

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

    /**
     * Notification that the workflow last task completed for this target locale
     * page of Tuvs.
     * 
     * @param p_targetTuvs
     *            target locale page Tuvs.
     * @param p_taskId
     *            task identifier that completed.
     * @throws TuvException
     *             when an error occurs.
     * 
     * @deprecated deleteTaskTuvs(p_targetTuvs) will always fail.
     */
    public void notifyLastTaskCompleteEvent(Collection p_targetTuvs,
            long p_taskId) throws TuvException, RemoteException
    {
        deleteTaskTuvs(p_targetTuvs);

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = null;

            try
            {
                ids = TuvPersistenceHelper.getIds(p_targetTuvs);
            }
            catch (Throwable e)
            {
                CATEGORY.error("notifyLastTaskCompleteEvent in debug ", e);
                ids = p_targetTuvs;
            }

            CATEGORY.debug("notifyLastTaskCompleteEvent" + " p_taskId="
                    + Long.toString(p_taskId) + " p_targetTuvs="
                    + ids.toString());
        }
    }

    /**
     * Notification that the workflow of target locale page Tuvs has been
     * cancelled.
     * 
     * @param p_targetTuvs
     *            target locale page Tuvs.
     * @throws TuvException
     *             when an error occurs.
     * 
     * @deprecated deleteTaskTuvs(p_targetTuvs) will always fail.
     */
    public void notifyWorkflowCancelEvent(Collection p_targetTuvs)
            throws TuvException, RemoteException
    {
        deleteTaskTuvs(p_targetTuvs);

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = null;

            try
            {
                ids = TuvPersistenceHelper.getIds(p_targetTuvs);
            }
            catch (Throwable e)
            {
                CATEGORY.error("notifyWorkflowCancelEvent in debug", e);
                ids = p_targetTuvs;
            }

            CATEGORY.debug("notifyWorkflowCancelEvent p_targetTuvs="
                    + ids.toString());
        }
    }

    /**
     * Notification that all the workflows of target locale page Tuvs have been
     * cancelled.
     * 
     * @param p_targetTuvs
     *            target locale page Tuvs.
     * @throws TuvException
     *             when an error occurs.
     * 
     * @deprecated deleteTaskTuvs(p_targetTuvs) will always fail.
     */
    public void notifyAllWorkflowsCancelEvent(Collection p_targetTuvs)
            throws TuvException, RemoteException
    {
        deleteTaskTuvs(p_targetTuvs);

        if (CATEGORY.isDebugEnabled())
        {
            Collection ids = null;

            try
            {
                ids = TuvPersistenceHelper.getIds(p_targetTuvs);
            }
            catch (Throwable e)
            {
                CATEGORY.error("notifyAllWorkflowsCancelEvent in debug ", e);
                ids = p_targetTuvs;
            }

            CATEGORY.debug("notifyAllWorkflowsCancelEvent p_targetTuvs="
                    + ids.toString());
        }
    }

    //
    // PACKAGE METHODS
    //

    // update the state of the Tuvs
    static void updateStateOfTuvs(Collection p_tuvs, TuvState p_state)
            throws TuvException
    {
        updateStateOfTuvs(p_tuvs, (Collection) null, p_state);
    }

    // update the state of the Tuvs if the state matches the from state
    static void updateStateOfTuvs(Collection p_tuvs, TuvState p_fromState,
            TuvState p_toState) throws TuvException
    {
        Collection fromStates = new ArrayList();
        fromStates.add(p_fromState);

        updateStateOfTuvs(p_tuvs, fromStates, p_toState);
    }

    // update the state of the Tuvs if the state matches the list of
    // from states
    static void updateStateOfTuvs(Collection p_tuvs, Collection p_fromStates,
            TuvState p_toState) throws TuvException
    {
        Collection stateUpdateTuvs = new ArrayList();
        Collection stateAndCrcUpdateTuvs = new ArrayList();

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

    /**
     * @deprecated The query argument is a workflow id, not a list of tuv ids
     *             for an IN clause. This function will always fail.
     */
    private static void deleteTaskTuvs(Collection p_targetTuvs)
            throws TuvException
    {
        // Vector queryArgs = null;
        // Collection result = null;
        // Collection resultAll = new HashSet();
        // try
        // {
        // queryArgs = TuvPersistenceHelper
        // .makeInArgumentFromObjects(p_targetTuvs);
        // queryArgs = (Vector) queryArgs.get(0);
        // for (int i =0; i < queryArgs.size(); i++)
        // {
        // Map map = new HashMap();
        // map.put("taskId", queryArgs.get(i));
        // String sql =
        // TaskTuvDescriptorModifier.PREVIOUS_TASK_TUV_BY_WORKFLOW_SQL;
        // result = HibernateUtil.searchWithSql(sql, map, TaskTuv.class);
        // resultAll.addAll(result);
        // }
        //
        // HibernateUtil.delete(resultAll);
        // }
        // catch (Exception e)
        // {
        // String message = "deleteTaskTuvs " + p_targetTuvs.toString() + " "
        // + e.toString();
        // CATEGORY.error(message, e);
        // throw new TuvException(message, e);
        // }
        //
        // if (CATEGORY.isDebugEnabled())
        // {
        // Collection taskTuvids = null;
        // Collection tuvids = null;
        // try
        // {
        // taskTuvids = TuvPersistenceHelper.getIds(result);
        // tuvids = TuvPersistenceHelper.getIds(p_targetTuvs);
        // }
        // catch (Throwable e)
        // {
        // CATEGORY.error("deleteTaskTuvs in debug", e);
        //                taskTuvids = result;
        //                tuvids = p_targetTuvs;
        //            }
        //            CATEGORY.debug("deleteTaskTuvs" + " tuvids=" + tuvids.toString()
        //                    + " returned TaskTuvs=" + taskTuvids.toString());
        //        }
    }

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
