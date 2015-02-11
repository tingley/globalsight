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

package com.globalsight.terminology;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.command.ITermbaseTmPopulator;
import com.globalsight.terminology.command.TermbaseTmPopulator;
import com.globalsight.util.SessionInfo;

/**
 * The persistence layer for termbase management.
 */
public class TermbaseManager implements TermbaseExceptionMessages
{
    private static final Logger CATEGORY = Logger
            .getLogger(TermbaseManager.class);

    /** Description column width in database */
    public static final int MAX_DESCRIPTION_LEN = 4000;

    private static Object s_creationLock = new Object();

    private static boolean s_isExtendedFeaturesEnabled = false;

    /**
     * Flag indicating that extended terminology features are enabled per
     * licence agreement. In the GlobalSight 6.0 release, the only extended
     * feature is Input Models.
     */
    static public boolean isExtendedFeaturesEnabled()
    {
        // TBF-2284-1574583831
        String expectedKey = "TBF-" + "GS".hashCode() + "-"
                + "terminology-features".hashCode();

        s_isExtendedFeaturesEnabled = SystemConfiguration
                .isKeyValid(SystemConfigParamNames.TERMINOLOGY_FEATURES_INSTALL_KEY);

        return s_isExtendedFeaturesEnabled;
    }

    /**
     * <p>
     * Bootstraps the list of known termbases by reading names and definitions
     * from the database and populating the TermbaseList.
     * </p>
     */
    static protected void initTermbases()
    {

        String hql = "from Termbase";
        Iterator ite = HibernateUtil.search(hql).iterator();

        while (ite.hasNext())
        {
            com.globalsight.terminology.java.Termbase tbase = (com.globalsight.terminology.java.Termbase) ite
                    .next();

            String companyId = String.valueOf(tbase.getCompany().getId());
            CompanyThreadLocal.getInstance().setIdValue(companyId);

            try
            {
                Termbase tb = new Termbase(tbase.getId(), tbase.getName(),
                        tbase.getDescription(), tbase.getDefination(),
                        companyId);
                TermbaseList.add(tb.getCompanyId(), tb.getName(), tb);
            }
            catch (TermbaseException e)
            {
                // A broken Termbase Definition, ignore and continue;
                CATEGORY.error("Cannot start termbase `" + tbase.getName()
                        + "'", e);
            }

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.info("Started termbase `" + tbase.getName() + "'");
            }
            CompanyThreadLocal.getInstance().setIdValue(null);
        }
    }

    /**
     * <p>
     * Releases all termbases during system shutdown.
     * </p>
     */
    static protected void shutdownTermbases()
    {
        Termbase tb = null;
        ArrayList names = TermbaseList.getAllNames();

        for (int i = 0; i < names.size(); i++)
        {
            String name = (String) names.get(i);

            tb = TermbaseList.remove(name);
            tb.shutdown();

            CATEGORY.info("Stopped termbase `" + name + "'");
        }

    }

    /**
     * Creates a new in-memory termbase object based on the supplied definition
     * and adds it to the TermbaseList.
     */
    static protected Termbase createTermbase(String p_definition,
            String p_companyId) throws TermbaseException
    {
        String name = null;
        String description = null;
        Definition definition = null;
        Termbase tb = null;

        try
        {
            // Read termbase definition and check correctness.
            // Throws an exception if definition is invalid.
            definition = new Definition(p_definition);
            definition.validate();

            description = definition.getDescription();
            name = definition.getName();

            if (name == null || name.length() == 0)
            {
                String[] args =
                { "name is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }
        }
        catch (TermbaseException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            String[] args =
            { name };
            throw new TermbaseException(MSG_FAILED_TO_CREATE_TB, args, ex);
        }

        try
        {
            synchronized (s_creationLock)
            {
                tb = TermbaseList.get(p_companyId, name);

                if (tb != null)
                {
                    String[] args =
                    { name };
                    throw new TermbaseException(MSG_TB_ALREADY_EXISTS, args,
                            null);
                }

                // save definition in TB_TERMBASE table
                long tbid = createPhysicalTermbase(name, description,
                        definition, p_companyId);

                tb = new Termbase(tbid, name, description, definition,
                        p_companyId);

                TermbaseList.add(tb.getCompanyId(), tb.getName(), tb);

                // tb.initIndexes(definition);
            }

            return tb;
        }
        catch (TermbaseException ex)
        {
            throw ex;
        }
    }

    /**
     * <p>
     * Stops the in-memory termbase object by setting it's status to DELETED and
     * removing it from the TermbaseList. The object may still be referenced by
     * many clients, but all their calls to the object will fail. Once the last
     * client has released his reference to the Termbase object, it will be
     * garbage-collected.
     * </p>
     */
    static protected void deleteTermbase(String p_name)
            throws TermbaseException
    {
        Termbase tb = null;

        boolean deleted = false;

        // Caller has verified the args but there is still a race
        // condition. First check the existing termbases.
        synchronized (s_creationLock)
        {
            tb = TermbaseList.get(p_name);

            if (tb == null)
            {
                String[] args =
                { p_name };
                throw new TermbaseException(MSG_TB_DOES_NOT_EXIST, args, null);
            }

            // Get an exclusive lock on the termbase. If there are
            // active readers connected, this will fail.
            tb.addWriter();

            try
            {
                // Since this is deletion of a termbase, it is possible
                // to force exclusive access by setting the termbase
                // state to DELETED anyway; subsequent reader calls will fail.
                tb.setDeleted();

                // This call deletes the definition from TB_TERMBASE
                // and sends a message to a JMS queue to delete the
                // real data asynchronously (see TermbaseDeleterMDB).

                // If the definition can not be deleted (throws an
                // exception), the Termbase is made visible again.
                deletePhysicalTermbase(tb);

                // Cancel and remove any running re-indexing schedules.
                deleteSchedules(tb);

                // Allow termbase to delete its own resources (indexes).
                tb.delete();

                tb = TermbaseList.remove(p_name);

                deleted = true;
            }
            catch (TermbaseException ex)
            {
                tb.setRunning();

                CATEGORY.error("Termbase `" + p_name
                        + "' could not be deleted.", ex);

                String[] args =
                { p_name };
                throw new TermbaseException(MSG_FAILED_TO_DELETE_TB, args, ex);
            }
            finally
            {
                tb.releaseWriter();
            }
        }

        if (deleted)
        {
            notifyTermbaseDeleted(p_name);

            CATEGORY.info("Termbase `" + p_name + "' is deleted.");
        }
    }

    /**
     * <p>
     * Updates the definition and possibly the name of an in-memory termbase
     * object. Both operations are not performed in the same transaction and may
     * fail independently.
     * </p>
     */
    static protected void updateDefinition(String p_name, String p_definition,
            SessionInfo p_session) throws TermbaseException
    {
        // Read termbase definition and check correctness.
        // Throws an exception if definition is invalid.
        Definition definition = new Definition(p_definition);
        definition.validate();

        String newName = definition.getName();

        Termbase tb = TermbaseList.get(p_name);

        if (tb == null)
        {
            String[] args =
            { p_name };
            throw new TermbaseException(MSG_TB_DOES_NOT_EXIST, args, null);
        }

        tb.addWriter();

        try
        {
            // Update the TB definition (not the name). This also
            // updates the indexes.
            tb.updateDefinition(definition, p_session);
        }
        finally
        {
            tb.releaseWriter();
        }

        // Change the name in a separate transaction, if needed.
        if (!p_name.equals(newName))
        {
            renameTermbase(p_name, newName);
        }

        CATEGORY.info("Termbase `" + p_name + "' updated.");
    }

    /**
     * <p>
     * Renames an in-memory termbase object by changing it's name and updating
     * the TermbaseList. The object may still be referenced by many clients, but
     * calls to the object should not fail (since they're by ID). New clients
     * will need to request the termbase by it's new name.
     * </p>
     */
    static protected void renameTermbase(String p_name, String p_newName)
            throws TermbaseException
    {
        Termbase tb = null;

        boolean renamed = false;

        // Caller has verified the args but there is still a race
        // condition. First check the existing termbases.
        synchronized (s_creationLock)
        {
            // Check new name
            tb = TermbaseList.get(p_newName);

            if (tb != null)
            {
                String[] args =
                { p_newName };
                throw new TermbaseException(MSG_TB_ALREADY_EXISTS, args, null);
            }

            // Check old name

            tb = TermbaseList.get(p_name);

            if (tb == null)
            {
                String[] args =
                { p_name };
                throw new TermbaseException(MSG_TB_DOES_NOT_EXIST, args, null);
            }

            // Remove the termbase temporarily from the list
            tb = TermbaseList.remove(p_name);

            // Definition contains a convenience copy of the name.
            Definition definition = new Definition(tb.getDefinition());
            definition.setName(p_newName);

            // Now we can safely rename the termbase, adding it back
            // to the list at the end.
            try
            {
                // Get an exclusive lock on the termbase. If there are
                // active readers connected, this will fail.
                tb.addWriter();

                try
                {
                    // May throw a sql exception and we don't change
                    // the in-memory tb.
                    renamePhysicalTermbase(tb, p_newName, definition);

                    // Rename the tb and update the internal definition.
                    tb.rename(p_newName);

                    // "finally" clauses release the lock and adds the
                    // termbase back to the list.

                    renamed = true;
                }
                finally
                {
                    tb.releaseWriter();
                }
            }
            catch (TermbaseException ex)
            {
                throw ex;
            }
            finally
            {
                TermbaseList.add(tb.getCompanyId(), tb.getName(), tb);
            }
        }

        if (renamed)
        {
            notifyTermbaseRenamed(p_name, p_newName);

            CATEGORY.info("Termbase `" + p_name + "' renamed to `" + p_newName
                    + "'.");
        }
    }

    //
    // Private Methods
    //

    /**
     * Helper method for createTermbase() that performs the necessary updates in
     * the SQL database.
     */
    static private long createPhysicalTermbase(String p_name,
            String p_description, Definition p_definition, String p_companyId)
            throws TermbaseException
    {
        String definition = p_definition.getXml();

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Creating termbase `" + p_name + "'");
            }

            com.globalsight.terminology.java.Termbase tbase = new com.globalsight.terminology.java.Termbase();
            Company company = (Company) ServerProxy.getJobHandler()
                    .getCompanyById(Long.parseLong(p_companyId));
            tbase.setCompany(company);
            tbase.setDefination(definition);
            tbase.setDescription(p_description);
            tbase.setIsActive(true);
            tbase.setName(p_name);

            HibernateUtil.save(tbase);

            return tbase.getId();
        }
        catch (Exception e)
        {
            CATEGORY.error("Termbase `" + p_name
                    + "' could not be renamed to `" + p_name + "'.", e);

            String[] args =
            { p_name, p_name };
            throw new TermbaseException(MSG_FAILED_TO_RENAME_TB, args, e);
        }
    }

    /**
     * Helper method for deleteTermbase() that performs the necessary deletes in
     * the SQL database. Because mabe one termbase have vast amount data, here
     * need use SQL to batch delete data, can't use hibernate cascading delete,
     * that will cost huge memory and very slowly and will lead to bad
     * performance.
     */
    static private void deletePhysicalTermbase(Termbase p_tb)
            throws TermbaseException
    {
        // Delete the termbase info only.
        Connection conn = null;
        Statement stmt = null;
        try
        {
            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            stmt.addBatch("delete from TB_TERMBASE where tbid=" + p_tb.getId());
            stmt.executeBatch();
            conn.commit();
        }
        catch (Exception e)
        {

        }
        finally
        {
            DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(conn);
        }

        // Delete real TB data from DB store in background.
        try
        {
            HashMap params = new HashMap();
            params.put(CompanyWrapper.CURRENT_COMPANY_ID, p_tb.getCompanyId());
            params.put("action", "delete_termbase");
            params.put("tbid", new Long(p_tb.getId()));

            JmsHelper.sendMessageToQueue(params,
                    JmsHelper.JMS_TERMBASE_DELETION_QUEUE);
        }
        catch (Exception ex)
        {
            CATEGORY.error(
                    "Cannot tell JMS queue to delete termbase " + p_tb.getId()
                            + ", must delete data manually.", ex);
        }
    }

    /**
     * Helper method for renameTermbase() that performs the necessary updates in
     * the SQL database.
     */
    static private long renamePhysicalTermbase(Termbase p_termbase,
            String p_name, Definition p_definition) throws TermbaseException
    {
        String definition = p_definition.getXml();

        try
        {
            com.globalsight.terminology.java.Termbase tbase = HibernateUtil
                    .get(com.globalsight.terminology.java.Termbase.class,
                            p_termbase.getId());

            tbase.setName(p_name);
            tbase.setDefination(definition);
            HibernateUtil.update(tbase);

            return tbase.getId();

        }
        catch (Exception e)
        {
            throw new TermbaseException(e);
        }
    }

    //
    // Deletion helpers
    //

    static private void deleteSchedules(Termbase p_termbase)
    {
        // Since Flux cannot participate in distributed 2-phase
        // commits, we can only hope that this call succeeds.

        try
        {
            // Un-schedule the TB re-index event.
            ServerProxy.getTermbaseScheduler().unscheduleEvent(
                    new Long(p_termbase.getId()));
        }
        catch (Throwable ex)
        {
            CATEGORY.warn(
                    "The re-index schedule for termbase `"
                            + p_termbase.getName() + "' (ID="
                            + p_termbase.getId() + ") could not be deleted.",
                    ex);
        }
    }

    //
    // Event Notifiers
    //

    static private void notifyTermbaseRenamed(String p_oldName, String p_newName)
    {
        try
        {
            ServerProxy.getProjectEventObserver().notifyTermbaseRenamed(
                    p_oldName, p_newName);
        }
        catch (Throwable ignore)
        {
        }
    }

    static private void notifyTermbaseDeleted(String p_name)
    {
        try
        {
            ServerProxy.getProjectEventObserver().notifyTermbaseDeleted(p_name);
        }
        catch (Throwable ignore)
        {
        }
    }

    /**
     * <p>
     * Add tmtuv segemnts into termbase database.
     * </p>
     * 
     */
    public void batchAddTuvsAsNew(long TBId, List tuvs, String creator)
    {
        ITermbaseTmPopulator tp = new TermbaseTmPopulator();
        tp.populateTermbase(TBId, tuvs, creator);
    }
}
