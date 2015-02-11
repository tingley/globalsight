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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.terminology.management.FileUploadHelper;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.Termbase.Statements;
import com.globalsight.terminology.util.Sortkey;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.UTC;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlFragmentReader;
import com.globalsight.util.gxml.GxmlFragmentReaderPool;

/**
 * The persistence layer for termbase management.
 */
public class TermbaseManager implements TermbaseExceptionMessages
{
    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
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
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            boolean oldCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            rset = stmt
                    .executeQuery("select TBID, TB_NAME, TB_DESCRIPTION, TB_DEFINITION, COMPANYID "
                            + "from TB_TERMBASE");

            while (rset.next())
            {
                String name = "*UNKNOWN*";

                try
                {
                    name = rset.getString("TB_NAME");
                    String companyId = rset.getString("COMPANYID");

                    CompanyThreadLocal.getInstance().setIdValue(companyId);

                    Termbase tb = new Termbase(rset.getLong("TBID"), name, rset
                            .getString("TB_DESCRIPTION"), SqlUtil.readClob(
                            rset, "TB_DEFINITION"), companyId);
                    TermbaseList.add(tb.getCompanyId(), tb.getName(), tb);

                    CATEGORY.info("Started termbase `" + name + "'");

                    CompanyThreadLocal.getInstance().setIdValue(null);
                }
                catch (TermbaseException e)
                {
                    // A broken Termbase Definition, ignore and continue;
                    CATEGORY.error("Cannot start termbase `" + name + "'", e);
                }
            }

            conn.commit();
            conn.setAutoCommit(oldCommit);
        }
        catch (Exception e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ex)
            { /* ignore */
            }
            CATEGORY.warn("can't read termbase data", e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
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
                String[] args = { "name is null" };
                throw new TermbaseException(MSG_INVALID_ARG, args, null);
            }
        }
        catch (TermbaseException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            String[] args = { name };
            throw new TermbaseException(MSG_FAILED_TO_CREATE_TB, args, ex);
        }

        try
        {
            synchronized (s_creationLock)
            {
                tb = TermbaseList.get(p_companyId, name);

                if (tb != null)
                {
                    String[] args = { name };
                    throw new TermbaseException(MSG_TB_ALREADY_EXISTS, args,
                            null);
                }

                // save definition in TB_TERMBASE table
                long tbid = createPhysicalTermbase(name, description,
                        definition, p_companyId);

                tb = new Termbase(tbid, name, description, definition,
                        p_companyId);

                TermbaseList.add(tb.getCompanyId(), tb.getName(), tb);
                
                tb.initIndexes(definition);
            }

            CATEGORY.info("Termbase `" + name + "' created.");

            return tb;
        }
        catch (TermbaseException ex)
        {
            throw ex;
        }
        catch (SQLException ex)
        {
            CATEGORY.error("Termbase `" + name + "' could not be created.", ex);

            String[] args = { name };
            throw new TermbaseException(MSG_FAILED_TO_CREATE_TB, args, ex);
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
                String[] args = { p_name };
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
            catch (SQLException ex)
            {
                tb.setRunning();

                CATEGORY.error("Termbase `" + p_name
                        + "' could not be deleted.", ex);

                String[] args = { p_name };
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

            CATEGORY.info("Termbase `" + p_name + "' deleted.");
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
            String[] args = { p_name };
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
                String[] args = { p_newName };
                throw new TermbaseException(MSG_TB_ALREADY_EXISTS, args, null);
            }

            // Check old name

            tb = TermbaseList.get(p_name);

            if (tb == null)
            {
                String[] args = { p_name };
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
            catch (SQLException ex)
            {
                CATEGORY.error("Termbase `" + p_name
                        + "' could not be renamed to `" + p_newName + "'.", ex);

                String[] args = { p_name, p_newName };
                throw new TermbaseException(MSG_FAILED_TO_RENAME_TB, args, ex);
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
            throws SQLException
    {
        String definition = p_definition.getXml();
        // boolean isClob = EditUtil.getUTF8Len(definition) > 4000;
        boolean isClob = false;

        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        long tbid;

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Creating termbase `" + p_name + "'");
            }

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            // fetch a tbid
            stmt = conn.createStatement();
            rset = stmt.executeQuery("select VALUE from TB_SEQUENCE "
                    + "where NAME='tbid' FOR UPDATE");

            rset.next();
            tbid = rset.getLong(1);
            rset.close();

            stmt.executeUpdate("update TB_SEQUENCE set VALUE=" + (tbid + 1)
                    + " " + "where NAME='tbid'");

            pstmt = conn
                    .prepareStatement("insert into TB_TERMBASE "
                            + "(TBID, TB_NAME, TB_DESCRIPTION, TB_DEFINITION, COMPANYID) "
                            + "values (?,?,?,?,?)");
            pstmt.setLong(1, tbid);
            pstmt.setString(2, p_name);

            // Avoid writing too much into the description field.
            if (p_description.length() > 0)
            {
                p_description = EditUtil.truncateUTF8Len(p_description,
                        MAX_DESCRIPTION_LEN);
            }
            pstmt.setString(3, p_description);

            // If the definition is large, write nothing here.
            if (isClob)
            {
                pstmt.setString(4, "All your base are belong to us!");
            }
            else
            {
                pstmt.setString(4, definition);
            }
            pstmt.setString(5, p_companyId);

            pstmt.executeUpdate();

            // If the definition is large, update the CLOB.
            // if (isClob)
            // {
            // rset = stmt.executeQuery(
            // "select TB_DEFINITION from TB_TERMBASE" +
            // " where TBID=" + tbid + " FOR UPDATE");
            //
            // rset.next();
            //
            // SqlUtil.writeClob(rset, "TB_DEFINITION", definition);
            // }

            conn.commit();

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Creating termbase `" + p_name + "' - done.");
            }

            return tbid;
        }
        catch (SQLException e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ex)
            { /* ignore */
            }
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
                if (pstmt != null) pstmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }
    }

    /**
     * Helper method for deleteTermbase() that performs the necessary deletes in
     * the SQL database.
     */
    static private void deletePhysicalTermbase(Termbase p_tb)
            throws SQLException
    {
        Connection conn = null;
        Statement stmt = null;
        long tbid = p_tb.getId();

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Deleting termbase `" + p_tb.getName() + "'.");
            }

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            stmt = conn.createStatement();
            stmt.addBatch("delete from TB_TERMBASE where tbid=" + tbid);
            stmt.executeBatch();

            conn.commit();

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Deleting termbase `" + p_tb.getName()
                        + "' - done. Will delete entries asynchronously.");
            }
        }
        catch (SQLException e)
        {
            try
            {
                if (conn != null) conn.rollback();
            }
            catch (Throwable t)
            { /* ignore */
            }

            throw e;
        }
        finally
        {
            try
            {
                if (stmt != null) stmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }

        // Delete the entries in the background.

        try
        {
            HashMap params = new HashMap();
            params.put(CompanyWrapper.CURRENT_COMPANY_ID, p_tb.getCompanyId());
            params.put("action", "delete_termbase");
            params.put("tbid", new Long(tbid));

            JmsHelper.sendMessageToQueue(params,
                    JmsHelper.JMS_TERMBASE_DELETION_QUEUE);
        }
        catch (Exception ex)
        {
            CATEGORY.error("Cannot tell JMS queue to delete termbase " + tbid
                    + ", must delete data manually.", ex);
        }
    }

    /**
     * Helper method for renameTermbase() that performs the necessary updates in
     * the SQL database.
     */
    static private long renamePhysicalTermbase(Termbase p_termbase,
            String p_name, Definition p_definition) throws SQLException
    {
        String definition = p_definition.getXml();
        // boolean isClob = EditUtil.getUTF8Len(definition) > 4000;
        boolean isClob = false;

        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;

        try
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Renaming termbase `" + p_termbase.getName()
                        + "' to `" + p_name + "'.");
            }

            long tbid = p_termbase.getId();

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            pstmt = conn
                    .prepareStatement("update TB_TERMBASE set TB_NAME=?, TB_DEFINITION=? where TBID=?");
            pstmt.setString(1, p_name);
            pstmt.setLong(3, tbid);

            // If the definition is large, write nothing here.
            if (isClob)
            {
                pstmt.setString(2, "All your base are belong to us!");
            }
            else
            {
                pstmt.setString(2, definition);
            }

            pstmt.executeUpdate();

            // If the definition is large, update the CLOB.
            // if (isClob)
            // {
            // rset = stmt.executeQuery(
            // "select TB_DEFINITION from TB_TERMBASE" +
            // " where TBID=" + tbid + " FOR UPDATE");
            //
            // rset.next();
            //
            // SqlUtil.writeClob(rset, "TB_DEFINITION", definition);
            // }

            conn.commit();

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Renaming termbase `" + p_termbase.getName()
                        + "' to `" + p_name + "' - done.");
            }

            return tbid;
        }
        catch (SQLException e)
        {
            try
            {
                conn.rollback();
            }
            catch (Throwable ex)
            { /* ignore */
            }
            throw e;
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
                if (pstmt != null) pstmt.close();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
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
            CATEGORY.warn("The re-index schedule for termbase `"
                    + p_termbase.getName() + "' (ID=" + p_termbase.getId()
                    + ") could not be deleted.", ex);
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
        Statements stmts;

        try {
            ITermbaseManager s_manager = ServerProxy.getTermbaseManager();
            String name = s_manager.getTermbaseName(TBId);
            Termbase tbase = TermbaseList.get(name);
            
            stmts = getAddTUVStatements(TBId, tuvs, creator);

            if (stmts != null)
            {
                tbase.executeStatements(stmts);
            }
        }
        catch (Exception ex) {
            // Ignore errors in this one entry.
            CATEGORY.warn("batchAdd tuv to termbase error: " + ex.getMessage());
        }
    }
    
    /*
     * <p>
     * Produces SQL statements to store a single concept from TUV in the database.
     * </p>
     */
    private Statements getAddTUVStatements(long TBId, List tuvs,
                                           String creator)
    {
        Statements result = new Statements();
        String statement;
        Termbase tbase = new Termbase();
        TuConceptRelation tcr = getTuConceptRelationByTu(((BaseTmTuv)tuvs.get(0)).getTu());
        boolean isNew = true;
        boolean isSouceTermHasExised = false;
        
        int size = 2;
        
        if(tcr != null) {
            isNew = false;
            size = 1;
        }
        
        long[] cids = new long[1];
        long[] lids = new long[size];
        long[] tids = new long[size];
        
        //if the source term has same content in termbase, make the target overwrite the term of the concept
        //and don't create a new concept.
        if(tuvs.size() > 0) {
            BaseTmTuv tuvSource = (BaseTmTuv)tuvs.get(0);
            BaseTmTuv tuvTarget = (BaseTmTuv)tuvs.get(1);
            String sourceLanguage = tuvSource.getLocale().getDisplayLanguage(Locale.US);
            String sourceTerm = new String();
            String targetTerm = new String();
            
            GxmlFragmentReader reader = null;

            try
            {
                reader = GxmlFragmentReaderPool.instance()
                        .getGxmlFragmentReader();

                GxmlElement m_gxmlElement = reader.parseFragment(tuvSource.getSegment());
                sourceTerm = m_gxmlElement.getTextValue();
                sourceTerm = sourceTerm.replaceAll("\n", "");
                sourceTerm = sourceTerm.replaceAll("&", "&amp;");
                sourceTerm = sourceTerm.replaceAll("<", "&lt;");
                sourceTerm = sourceTerm.replaceAll(">", "&gt;");
                sourceTerm = sourceTerm.replaceAll("'", "&apos;");
                sourceTerm = sourceTerm.replaceAll("\"", "&quot;");
                
                m_gxmlElement = reader.parseFragment(tuvTarget.getSegment());
                targetTerm = m_gxmlElement.getTextValue();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error in TuvImpl: "
                        + GeneralException.getStackTraceString(e));
            }
            finally
            {
                GxmlFragmentReaderPool.instance()
                        .freeGxmlFragmentReader(reader);
            }
            
            targetTerm = targetTerm.replaceAll("\n", "");
            targetTerm = targetTerm.replaceAll("&", "&amp;");
            targetTerm = targetTerm.replaceAll("<", "&lt;");
            targetTerm = targetTerm.replaceAll(">", "&gt;");
            targetTerm = targetTerm.replaceAll("'", "&apos;");
            targetTerm = targetTerm.replaceAll("\"", "&quot;");
                
            String termType = "*unknown*";
            String termStatus = "*unknown*";
            String sortKey;
        
            // Extract term and compute binary sortkey
            //for source tuv
            sortKey = SqlUtil.toHex(Sortkey.getSortkey(targetTerm, 
                    tuvTarget.getLocale().getLanguage()),2000); 

            // Limit size of data
            targetTerm = EditUtil.truncateUTF8Len(targetTerm, 2000);
            
            Connection conn = null;
            Statement stmt = null;
            ResultSet rset = null;
            ResultSet rset2 = null;
            
            try{
                conn = SqlUtil.hireConnection();
                conn.setAutoCommit(false);
                String isDuplicateSql =  
                    "select * from tb_term where LANG_NAME='" + sourceLanguage 
                    + "' and TERM='" + sourceTerm + "'"
                    + "and TBID=" + TBId;
                stmt = conn.createStatement();
                rset = stmt.executeQuery(isDuplicateSql);
                
                if(rset.next()) {
                    isSouceTermHasExised = true;
                    lids = new long[1];
                    tids = new long[1];
                    cids[0] = rset.getInt("CID");
                    
                    //delete all the terms and languages of this concept
                    //add the new target term and language into the concept
                    String targetLanguage = tuvTarget.getLocale().getDisplayLanguage(Locale.US);
                    String sql2 = "select * from tb_term where TBID=" + TBId + " and CID=" + cids[0]
                                  + " and LANG_NAME='" + targetLanguage +"'";
                    rset2 = stmt.executeQuery(sql2);
                    
                    if(rset2.next()) {
                        int termId = rset2.getInt("Tid");
                        tids[0] = termId;
                        String sql3 = "update tb_term set TERM='" + SqlUtil.quote(targetTerm) + "', TYPE='"
                                      + SqlUtil.quote(termType) + "',Status='"
                                      + SqlUtil.quote(termStatus) + "',Sort_Key='" 
                                      + sortKey + "',XML='' Where TBID=" + TBId 
                                      + " and CID=" + cids[0] + " and TID=" + termId;
                        stmt.addBatch(sql3);
                        stmt.executeBatch();
                        conn.commit();
                    }
                    else {
                        tbase.allocateIds(lids, tids); 
                    
                    /*
                    String sql2 = "delete from tb_term where TBID=" + TBId + " and CID=" + cids[0] + " and LANG_NAME not in('"+sourceLanguage + "')";
                    String sql3 = "delete from TB_LANGUAGE where TBID=" + TBId + " and CID=" + cids[0] + " and NAME not in('"+sourceLanguage + "')";
                    stmt.addBatch(sql2);
                    stmt.addBatch(sql3);
                    stmt.executeBatch();
                    conn.commit();
                    */
                    statement = "insert into TB_LANGUAGE "
                            + " (TBId, Lid, Cid, Name, Locale, Xml)" + " values ("
                            + TBId + "," + lids[0] + "," + cids[0] + "," + "'"
                            + targetLanguage + "'," 
                            + "'" + tuvTarget.getLocale().getLanguage() + "','')";
            
                    result.addLanguageStatement(statement);
                    
                    statement = "insert into TB_TERM "
                             + " (TBId, Cid, Lid, Tid, Lang_Name, Term, "
                             + " Type, Status, Sort_Key, XML)" + " values ("
                             + TBId
                             + ","
                             + cids[0]
                             + ","
                             + lids[0]
                             + ","
                             + tids[0]
                             + ","
                             + "'"
                             + SqlUtil.quote(targetLanguage)
                             + "',"
                             + "'"
                             + SqlUtil.quote(targetTerm)
                             + "',"
                             + "'"
                             + SqlUtil.quote(termType)
                             + "',"
                             + "'"
                             + SqlUtil.quote(termStatus)
                             + "',"
                             + "'"
                             + sortKey
                             + "','')";

                     result.addTermStatement(statement);
                    }
                     
                     String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";
                     File parentFilePath = new File(termImgPath.toString());
                     File[] files = parentFilePath.listFiles();
                         
                     if (files != null && files.length > 0) {
                         for (int j = 0; j < files.length; j++) {
                             File file = files[j];
                             String fileName = file.getName();
                             
                             if(fileName.lastIndexOf(".") > 0) {
                                 String tempName= fileName.substring(0, fileName.lastIndexOf("."));
                                 String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
                                 
                                 String nowImgName =  "tuv_" + Long.toString(tuvTarget.getId());
                                 
                                 if(tempName.equals(nowImgName)) {
                                     String newFileName = "tb_" + tids[0] + suffix;
                                     File newFile = new File(termImgPath, newFileName);

                                     FileUploadHelper.renameFile(file, newFile, true);

                                 }
                             }
                         }
                     }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                try
                {
                    if (rset != null) rset.close();
                    if (rset2 != null) rset.close();
                    if (stmt != null) stmt.close();
                }
                catch (Throwable t)
                { /* ignore */
                }

                SqlUtil.fireConnection(conn);
            }
        }
        
        if(!isSouceTermHasExised) {
            // Allocate new ids for the table rows
            if(!isNew) {
                cids[0] = tcr.getConceptId();
                tbase.allocateIds(lids, tids); 
            }
            else {
                tbase.allocateIds(cids, lids, tids); 
    
                String domain = "*unknown*";
                String project = "*unknown*";
                String status = "proposed";
                StringBuffer xml = new StringBuffer();
                
                xml = xml.append("<concept>").append(cids[0])
                  .append("</concept><transacGrp><transac type=\"origination\">");
                xml = xml.append(((BaseTmTuv)tuvs.get(0)).getCreationUser()).append("</transac><date>");
                xml = xml.append(UTC.valueOf((new Date()))).append("</date></transacGrp>");
                
                boolean needClob = false;
    
                statement = "insert into TB_CONCEPT "
                        + " (TBId, Cid, Domain, Status, Project, XML, "
                        + " Created_On, Created_By)" + " values (" + TBId + ","
                        + cids[0] + "," + "'" + SqlUtil.quote(domain) + "'," + "'"
                        + SqlUtil.quote(status) + "'," + "'"
                        + SqlUtil.quote(project) + "',"
                        + SqlUtil.getClobInitializer(xml.toString(), needClob) + "," + "'"
                        + UTC.valueOf((new Date())) + "'," + "'"
                        + SqlUtil.quote(creator) + "')";
        
                result.addConceptStatement(statement);
            }
            
            String insertedLanguage = "";
            
            if(!isNew) {
                insertedLanguage = tcr.getAddedLanguage();
            }
            
            int index = 0;
            
            for(int x = 0; x < tuvs.size(); x++){
                BaseTmTuv tuv = (BaseTmTuv)tuvs.get(x);
                String tuvLanguage = tuv.getLocale().getDisplayLanguage(Locale.US);
                //produce language-level statements
                //for source tuv
                if((!isNew && tcr.getAddedLanguage() != null 
                        && tcr.getAddedLanguage().indexOf(tuvLanguage) < 0) || isNew) {
                    
                    insertedLanguage = insertedLanguage + "," + tuvLanguage;
                    
                    statement = "insert into TB_LANGUAGE "
                            + " (TBId, Lid, Cid, Name, Locale, Xml)" + " values ("
                            + TBId + "," + lids[index] + "," + cids[0] + "," + "'"
                            + tuvLanguage + "'," 
                            + "'" + tuv.getLocale().getLanguage() + "','')";
            
                    result.addLanguageStatement(statement);
                    
                    String term = new String();
                    
                    GxmlFragmentReader reader = null;
    
                    try
                    {
                        reader = GxmlFragmentReaderPool.instance()
                                .getGxmlFragmentReader();
    
                        GxmlElement m_gxmlElement = reader.parseFragment(tuv.getSegment());
                        term = m_gxmlElement.getTextValue();
                    }
                    catch (Exception e)
                    {
                        //c_category.error("Error in TuvImpl: " + toString(), e);
                        // Can't have Tuv in inconsistent state, throw runtime
                        // exception.
                        throw new RuntimeException("Error in TuvImpl: "
                                + GeneralException.getStackTraceString(e));
                    }
                    finally
                    {
                        GxmlFragmentReaderPool.instance()
                                .freeGxmlFragmentReader(reader);
                    }
                   
                    term =  term.replaceAll("\n", "");
                    term = term.replaceAll("&", "&amp;");
                    term = term.replaceAll("<", "&lt;");
                    term = term.replaceAll(">", "&gt;");
                    term = term.replaceAll("'", "&apos;");
                    term = term.replaceAll("\"", "&quot;");
                    String termType = "*unknown*";
                    String termStatus = "*unknown*";
                    String sortKey;
                
                    // Extract term and compute binary sortkey
                    //for source tuv
                    sortKey = SqlUtil.toHex(Sortkey.getSortkey(term, 
                              tuv.getLocale().getLanguage()),2000);
        
                    // Limit size of data
                    term = EditUtil.truncateUTF8Len(term, 2000);
            
                    statement = "insert into TB_TERM "
                            + " (TBId, Cid, Lid, Tid, Lang_Name, Term, "
                            + " Type, Status, Sort_Key, XML)" + " values ("
                            + TBId
                            + ","
                            + cids[0]
                            + ","
                            + lids[index]
                            + ","
                            + tids[index]
                            + ","
                            + "'"
                            + SqlUtil.quote(tuv.getLocale().getDisplayLanguage(Locale.US))
                            + "',"
                            + "'"
                            + SqlUtil.quote(term)
                            + "',"
                            + "'"
                            + SqlUtil.quote(termType)
                            + "',"
                            + "'"
                            + SqlUtil.quote(termStatus)
                            + "',"
                            + "'"
                            + sortKey
                            + "','')";
    
                    result.addTermStatement(statement);
                    
                    String termImgPath = FileUploadHelper.DOCROOT + "terminologyImg";
                    File parentFilePath = new File(termImgPath.toString());
                    File[] files = parentFilePath.listFiles();
                    
                    if (files != null && files.length > 0)
                    {
                        for (int j = 0; j < files.length; j++) 
                        {
                            File file = files[j];
                            String fileName = file.getName();
                            
                            if(fileName.lastIndexOf(".") > 0) {
                                String tempName= fileName.substring(0, fileName.lastIndexOf("."));
                                String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
                                
                                String nowImgName =  "tuv_" + Long.toString(tuv.getId());
                                
                                if(tempName.equals(nowImgName)) {
                                    String newFileName = "tb_" + tids[index] + suffix;
                                    File newFile = new File(termImgPath, newFileName);
                                    
                                    try {
                                        FileUploadHelper.renameFile(file, newFile, true);
                                    }
                                    catch(Exception e){}
                                }
                            }
                        }
                    }
                    index++;
                }
            }
            
            if(!isNew) {
                tcr.setAddedLanguage(insertedLanguage);
                updateTuConceptRelation(tcr);
            }
            else {
                tcr = new TuConceptRelation();
                tcr.setConceptId(cids[0]);
                tcr.setTuId(((BaseTmTuv)tuvs.get(0)).getTu().getId());
                tcr.setAddedLanguage(insertedLanguage);
                createTuConceptRelation(tcr);
            }
        }
        
        return result;
    }
    
    private void createTuConceptRelation(TuConceptRelation tuConcept){
        
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            session.save(tuConcept);
            transaction.commit();
        } catch (PersistenceException e) {
            transaction.rollback();
            CATEGORY.warn("Create TuConceptRelation error!");
        }
    }
    
    private void updateTuConceptRelation(TuConceptRelation tuConcept) {
        Session session = null;
        Transaction transaction = null;
        
        try {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            session.saveOrUpdate(tuConcept);
            transaction.commit();
        } catch (PersistenceException e) {
            transaction.rollback();
            CATEGORY.warn("Update TuConceptRelation error!");
        }
    }
    
    private TuConceptRelation getTuConceptRelationByTu(BaseTmTu tu) {
        TuConceptRelation tc = null;
        
        try {
            String hql = "from TuConceptRelation a where a.tuId = :id";
            HashMap map = new HashMap<String, String>();
            map.put("id", tu.getId());
            Collection tcs = HibernateUtil.search(hql, map);
            Iterator i = tcs.iterator();
            tc = i.hasNext() ? (TuConceptRelation) i.next() : null;
        }
        catch(Exception e) {
            CATEGORY.error(
                "Persistence Exception when retrieving TuConceptRelation" + tu.getId(), e); 
        }
        
        return tc;
    }
}
