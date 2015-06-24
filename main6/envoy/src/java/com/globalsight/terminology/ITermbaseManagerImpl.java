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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.terminology.audit.TermAuditEvent;
import com.globalsight.terminology.audit.TermAuditLog;
import com.globalsight.util.SessionInfo;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * <p>
 * The implementation of the RMI interface for the Terminology Database Manager.
 * Responsible for creating, deleting, and accessing terminology databases.
 * </p>
 *
 * <p>
 * This class is a singleton (a factory for ITermbase objects) and manages the
 * list of known termbases.
 * </p>
 */
public class ITermbaseManagerImpl extends RemoteServer implements
        ITermbaseManager, TermbaseExceptionMessages
{
    private static final Logger CATEGORY = Logger
            .getLogger(ITermbaseManagerImpl.class);

    //
    // Member Variables
    //
    static public final String VERSION = "GlobalSight Termbase Version 1.0";

    //
    // Constructor
    //
    public ITermbaseManagerImpl() throws RemoteException
    {
        super(RemoteServer.getServiceName(ITermbaseManager.class));
    }

    //
    // RemoteServer method overwrites
    //

    /**
     * <p>
     * Binds the remote server to the ServerRegistry.
     * </p>
     *
     * @throws SystemStartupException
     *             when a NamingException or other Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        // Initialize list of known termbases
        TermbaseManager.initTermbases();

        super.init();
    }

    /**
     * <p>
     * Unbinds the remote server from the ServerRegistry.
     * </p>
     *
     * @throws SystemShutdownException
     *             when a NamingException or other Exception occurs.
     */
    public void destroy() throws SystemShutdownException
    {
        // Release list of known termbases
        TermbaseManager.shutdownTermbases();

        super.destroy();
    }

    //
    // ITermbaseManager interface methods
    //

    /**
     * Retrieves the server name and version info.
     *
     * @return a string like "GlobalSight Termbase Version 1.0".
     */
    public String getVersion() throws RemoteException
    {
        return VERSION;
    }

    /**
     * Retrieves a list of termbase names and descriptions known to the server.
     *
     * @param p_uiLocale
     *            -- the UI locale to use for sorting
     * @return an ArrayList
     */
    public ArrayList getTermbaseList(Locale p_uiLocale) throws RemoteException
    {
        return TermbaseList.getTermbases(p_uiLocale);
    }

    /**
     * Retrieves a sorted list of termbase names and descriptions known to the
     * server.
     *
     * @param p_uiLocale
     *            -- the UI locale to use for sorting
     * @return an XML string: <termbases> <termbase> <name>NAME</name>
     *         <description>DESC</description> </termbase> </termbases>
     */
    public String getTermbases(Locale p_uiLocale, String p_userId)
            throws RemoteException
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        return TermbaseList.getDescriptions(p_uiLocale, p_userId, companyId);
    }

    /**
     * Retrieves a sorted list of termbase names and descriptions known to the
     * server.
     *
     * @param p_uiLocale
     *            -- the UI locale to use for sorting
     * @param p_userId
     * @param p_companyId
     * @return an XML string: <termbases> <termbase> <name>NAME</name>
     *         <description>DESC</description> </termbase> </termbases>
     */
    public String getTermbases(Locale p_uiLocale, String p_userId,
            String p_companyId) throws RemoteException
    {
        return TermbaseList.getDescriptions(p_uiLocale, p_userId, p_companyId);
    }

    /**
     * Retrieves the name of the termbase with the given id.
     *
     * @return String if termbase was found, else null.
     */
    public String getTermbaseName(long p_id) throws RemoteException
    {
        Termbase tb = TermbaseList.get(p_id);

        if (tb != null)
        {
            return tb.getName();
        }

        return null;
    }

    /**
     * Retrieves the id of the termbase with the given name.
     *
     * @return long >= 0 if termbase was found, else -1.
     */
    public long getTermbaseId(String p_name) throws RemoteException
    {
        Termbase tb = TermbaseList.get(p_name);

        if (tb != null)
        {
            return tb.getId();
        }

        return -1;
    }

    /**
     * Retrieves the id of the termbase with the given name and company id.
     * 
     * @return long >= 0 if termbase was found, else -1.
     */
    public long getTermbaseId(String p_name, String p_companyId)
            throws RemoteException
    {
        Termbase tb = TermbaseList.get(p_companyId, p_name);

        if (tb != null)
        {
            return tb.getId();
        }

        return -1;
    }

    /**
     * Connects to a termbase by name.
     *
     * @return an ITermbase interface pointer.
     *
     * @throws TermbaseException
     *             when the name does not exist or the termbase is locked by a
     *             manager.
     */
    public ITermbase connect(String p_name, String p_user, String p_password)
            throws TermbaseException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
            { "user name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_name);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        SessionInfo session = new SessionInfo(p_user, "guest");

        return new ITermbaseImpl(tb, session);
    }

    public ITermbase connect(String p_termbaseName, String p_user,
            String p_password, String p_companyId) throws TermbaseException,
            RemoteException
    {
        if (p_termbaseName == null || p_termbaseName.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
            { "user name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_companyId, p_termbaseName);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        SessionInfo session = new SessionInfo(p_user, "guest");

        return new ITermbaseImpl(tb, session);
    }

    /**
     * Connects to a termbase by name.
     *
     * @return an ITermbase interface pointer.
     *
     * @throws TermbaseException
     *             when the name does not exist or the termbase is locked by a
     *             manager.
     */
    public ITermbase connect(long p_id, String p_user, String p_password)
            throws TermbaseException, RemoteException
    {
        if (p_id < 1)
        {
            String[] args =
            { "id is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
            { "user name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_id);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        SessionInfo session = new SessionInfo(p_user, "guest");

        return new ITermbaseImpl(tb, session);
    }

    /**
     * Creates a new termbase. A termbase name and termbase definition have to
     * be specified.
     *
     * @param definition
     *            : an XML object for the database definition.
     *
     * @throws TermbaseException
     *             when the name exists, the definition is incorrect, or the
     *             termbase cannot be created physically.
     */
    public ITermbase create(String p_user, String p_password,
            String p_definition, String p_companyId) throws TermbaseException,
            RemoteException
    {
        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
            { "user name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        // create and persist new termbase
        Termbase tb = TermbaseManager.createTermbase(p_definition, p_companyId);

        SessionInfo session = new SessionInfo(p_user, "guest");
        TermAuditEvent auditEvent = new TermAuditEvent(new Date(), p_user,
                tb.getName(), tb.getName(), "ALL", "create", "created termbase");
        TermAuditLog.log(auditEvent);

        return new ITermbaseImpl(tb, session);
    }

    public ITermbase create(String p_user, String p_password,
            String p_definition) throws TermbaseException, RemoteException
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();

        return create(p_user, p_password, p_definition, companyId);
    }

    /**
     * Deletes a Termbase from the system.
     * 
     * @throws TermbaseException
     *             when the termbase does not exist, is in use by another reader
     *             or manager, or when a database error occurs during deletion.
     */
    public void delete(String p_name, String p_user, String p_password)
            throws TermbaseException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
            { "user name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_name);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        TermbaseManager.deleteTermbase(p_name);
        TermAuditEvent auditEvent = new TermAuditEvent(new Date(), p_user,
                tb.getName(), tb.getName(), "ALL", "delete", "deleted termbase");
        TermAuditLog.log(auditEvent);
    }

    /**
     * Renames a Termbase.
     *
     * @throws TermbaseException
     *             when the termbase does not exist, the new name already
     *             exists, or the termbase is in use by another reader or
     *             manager, or a database error occurs during renaming.
     */
    public void rename(String p_name, String p_newName, String p_user,
            String p_password) throws TermbaseException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        if (p_newName == null || p_newName.length() == 0)
        {
            String[] args =
            { "new name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
            { "user name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_name);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb2 = TermbaseList.get(p_newName);

        if (tb2 != null)
        {
            String[] args =
            { "duplicate new name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        TermbaseManager.renameTermbase(p_name, p_newName);
        TermAuditEvent auditEvent = new TermAuditEvent(new Date(), p_user,
                p_name, p_name, "ALL", "delete", "renamed termbase to "
                        + p_newName);
        TermAuditLog.log(auditEvent);
    }

    /**
     * Updates a Termbase Definition. Since the definition includes the termbase
     * name, it can be changed as well.
     *
     * @throws TermbaseException
     *             when the termbase does not exist, the the definition is
     *             incorrect or cannot be saved in the database, the new name
     *             already exists (if changed in the definition), the termbase
     *             is in use by another reader or manager, or a database error
     *             occurs during renaming.
     */
    public void updateDefinition(String p_name, String p_definition,
            String p_user, String p_password) throws TermbaseException,
            RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        if (p_definition == null || p_definition.length() == 0)
        {
            String[] args =
            { "definition is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
            { "user name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_name);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        SessionInfo session = new SessionInfo(p_user, "guest");
        String oldDefinition = getDefinition(p_name, false);
        TermbaseManager.updateDefinition(p_name, p_definition, session);
        TermAuditEvent auditEvent = new TermAuditEvent(new Date(), p_user,
                p_name, p_name, "ALL", "update", "updated definition");
        TermAuditLog.log(auditEvent);
    }

    /**
     * Returns the termbase definition of the given termbase. If the argument
     * <code>clone</code> is true, a copy of the definition is returned (without
     * Termbase name).
     */
    public String getDefinition(String p_name, boolean p_clone)
            throws TermbaseException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_name);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        String definition = tb.getDefinition();

        if (p_clone)
        {
            // Cut out name of the termbase; UI expects this to detect
            // the clone operation. (We may cut out the description
            // too.)

            try
            {
                RE pattern = new RE("<definition>\\s*<name>(.*?)</name>",
                        RE.MATCH_NORMAL);
                definition = pattern.subst(definition,
                        "<definition><name></name>", RE.REPLACE_FIRSTONLY);
            }
            catch (RESyntaxException e)
            {
                CATEGORY.error("pilot error in regex", e);
            }
        }

        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug(definition);
        }

        return definition;
    }

    /**
     * Returns a default termbase definition that can be modified according to
     * taste.
     */
    public String getDefaultDefinition() throws RemoteException
    {
        return Definition.getDefaultDefinition();
    }

    /**
     * Returns the termbase statistics of the given termbase.
     */
    public String getStatistics(String p_name) throws TermbaseException,
            RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_name);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        String result = tb.getStatistics();

        return result;
    }

    public String getStatisticsNoIndexInfo(String p_name)
            throws TermbaseException, RemoteException
    {
        if (p_name == null || p_name.length() == 0)
        {
            String[] args =
            { "name is null" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        Termbase tb = TermbaseList.get(p_name);

        if (tb == null)
        {
            String[] args =
            { "unknown name" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        String result = tb.getStatisticsWithoutIndexInfo();

        return result;
    }
}
