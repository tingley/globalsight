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

package com.globalsight.terminology.userdata;

//  import com.globalsight.terminology.userdata.Filter;
//  import com.globalsight.terminology.userdata.InputModel;
//  import com.globalsight.terminology.userdata.Layout;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.globalsight.everest.permission.Permission;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.terminology.IUserdataManager;
import com.globalsight.terminology.Termbase;
import com.globalsight.terminology.TermbaseException;
import com.globalsight.terminology.TermbaseExceptionMessages;
import com.globalsight.terminology.util.PermissionHelper;
import com.globalsight.terminology.util.SqlUtil;
import com.globalsight.util.SessionInfo;
import com.globalsight.util.edit.EditUtil;

/**
 * <p>The RMI interface implementation for the User Data Manager.</p>
 *
 * <p>Throughout this class, the parameter p_user denotes the username
 * for which data is requested. The name of the user currently calling
 * this code is stored in m_session.</p>
 */
public class UserdataManager
    implements IUserdataManager,
               TermbaseExceptionMessages
{
    static private final GlobalSightCategory CATEGORY =
        (GlobalSightCategory)GlobalSightCategory.getLogger(
            UserdataManager.class);

    static final public String ALL_USERS = "*";
    static final public int MAX_USERNAME_LEN = 80;
    static final public int MAX_NAME_LEN = 200;

    //
    // Private Members
    //
    private SessionInfo m_session = null;
    private Termbase m_database = null;

    //
    // Constructor
    //
    public UserdataManager(Termbase p_database, SessionInfo p_session)
    {
        m_session = p_session;
        m_database = p_database;
    }

    //
    // Interface Implementation
    //

    public String getTermbaseName()
        throws TermbaseException, RemoteException
    {
        return m_database.getName();
    }

    /**
     * Retrieves all object names for objects of the specified
     * type. Administrators can read object names for all users,
     * normal users can read only their own objects' names.
     *
     * @return an XML object
     * &lt;names&gt;&lt;name type="system|user"&gt;...&lt;/name&gt;&lt;/names&gt;
     */
    public String getObjectNames(int p_type, String p_user)
        throws TermbaseException, RemoteException
    {
        String result = null;

        checkReadAccess(p_user);

        result = doGetObjectNames(p_type, p_user);

        return result;
    }

    /**
     * <p>Retrieves a single typed and named object. Administrators
     * can retrieve objects for all users, normal users can only see
     * their own objects.</p>
     */
    public String getObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException
    {
        String result = null;

        checkReadAccess(p_user);

        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "object name cannot be empty" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        result = doGetObject(p_type, p_user, p_name);

        return result;
    }

    /**
     * <p>Retrieves a system-wide default object of the given type.</p>
     */
    public String getDefaultObject(int p_type)
        throws TermbaseException, RemoteException
    {
        return doGetDefaultObject(p_type);
    }

    /**
     * Creates an object with name p_name and value p_value for the
     * given user. If the user name is null, a system object is
     * created for all users.
     */
    public void createObject(int p_type, String p_user, String p_name,
        String p_value)
        throws TermbaseException, RemoteException
    {
        checkWriteAccess(p_user);

        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "object name cannot be empty" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        doCreateObject(p_type, p_user, p_name, p_value);
    }

    /**
     * Updates an object with name p_name and value p_value for the
     * given user.
     */
    public void modifyObject(int p_type, String p_user, String p_name,
        String p_value)
        throws TermbaseException, RemoteException
    {
        checkWriteAccess(p_user);

        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "object name cannot be empty" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        doModifyObject(p_type, p_user, p_name, p_value);
    }

    /**
     * Deletes an objects for the given user. If the caller is
     * Administrator and p_user is null, the system object is
     * deleted.
     */
    public void deleteObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException
    {
        checkWriteAccess(p_user);

        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "object name cannot be empty" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        doDeleteObject(p_type, p_user, p_name);
    }

    /**
     * Makes a system-wide object the default object.
     */
    public void makeDefaultObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException
    {
        checkWriteAccess(p_user);

        if (p_name == null || p_name.length() == 0)
        {
            String[] args = { "object name cannot be empty" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        doMakeDefaultObject(p_type, p_user, p_name);
    }

    /**
     * Unsets a system-wide default object.
     */
    public void unsetDefaultObject(int p_type, String p_user)
        throws TermbaseException, RemoteException
    {
        checkWriteAccess(p_user);

        doUnsetDefaultObject(p_type, p_user);
    }

    /**
     * Deletes all objects for the given user. If the caller is
     * Administrator and p_user is null, all system objects are
     * deleted.
     */
    public void deleteObjects(int p_type, String p_user)
        throws TermbaseException, RemoteException
    {
        checkWriteAccess(p_user);

        doDeleteObjects(p_type, p_user);
    }

    //
    // Worker Methods
    //

    private String doGetObjectNames(int p_type, String p_user)
        throws TermbaseException, RemoteException
    {
        StringBuffer result = new StringBuffer();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        result.append("<names>\n");

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            rset = stmt.executeQuery(
                "select USERNAME, NAME, ISDEFAULT from TB_USER_DATA" +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                (p_user == null || p_user.length() == 0
                    ?
                    // retrieve all object names
                    ""
                    :
                    // retrive system and specific user names
                    "   and (USERNAME='" + ALL_USERS + "'" +
                    "    or USERNAME='" + SqlUtil.quote(p_user) + "')"
                    ) +
                " order by username, name");

            while (rset.next())
            {
                String userName = rset.getString(1);
                String name = rset.getString(2);
                String isDefault = rset.getString(3);

                if (userName.equals(ALL_USERS))
                {
                    result.append("<name type=\"system\" user=\"\"");
                }
                else
                {
                    result.append("<name type=\"user\" user=\"");
                    result.append(EditUtil.encodeXmlEntities(userName));
                    result.append("\"");
                }

                result.append(" isdefault=\"");
                result.append(isDefault.equals("Y"));
                result.append("\"");

                result.append(">");

                result.append(EditUtil.encodeXmlEntities(name));

                result.append("</name>\n");
            }

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }

        result.append("</names>");

        return result.toString();
    }

    private String doGetObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException
    {
        String result = null;

        String user = (p_user == null || p_user.length() == 0) ?
            ALL_USERS : EditUtil.truncateUTF8Len(p_user, MAX_USERNAME_LEN);
        String name = EditUtil.truncateUTF8Len(p_name, MAX_NAME_LEN);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            rset = stmt.executeQuery(
                "select VALUE from TB_USER_DATA" +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                "   and USERNAME='" + SqlUtil.quote(user) + "'" +
                "   and NAME='" + SqlUtil.quote(name) + "'");

            rset.next();
            result = SqlUtil.readClob(rset, "VALUE");

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    private String doGetDefaultObject(int p_type)
        throws TermbaseException, RemoteException
    {
        String result = null;

        String user = ALL_USERS;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            rset = stmt.executeQuery(
                "select VALUE from TB_USER_DATA" +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                "   and USERNAME='" + SqlUtil.quote(user) + "'" +
                "   and ISDEFAULT='Y'");

            rset.next();
            result = SqlUtil.readClob(rset, "VALUE");

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }

            // Don't throw exception, default object is just not set.
            result = "";
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }

        return result;
    }

    private void doCreateObject(int p_type, String p_user, String p_name,
        String p_value)
        throws TermbaseException, RemoteException
    {
        String user = (p_user == null || p_user.length() == 0) ?
            ALL_USERS : EditUtil.truncateUTF8Len(p_user, MAX_USERNAME_LEN);
        String name = EditUtil.truncateUTF8Len(p_name, MAX_NAME_LEN);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

//            boolean needClob = EditUtil.getUTF8Len(p_value) > 4000;
            boolean needClob = false;

            stmt.executeUpdate("insert into TB_USER_DATA " +
                " (TBID, TYPE, USERNAME, NAME, ISDEFAULT, VALUE) " +
                " values (" + m_database.getId() + "," + p_type + "," +
                "'" + SqlUtil.quote(user) + "'," +
                "'" + SqlUtil.quote(name) + "', 'N'," +
                SqlUtil.getClobInitializer(p_value, needClob) + ")");

//            if (needClob)
//            {
//                rset = stmt.executeQuery(
//                    "select VALUE from TB_USER_DATA" +
//                    " where TBID=" + m_database.getId() +
//                    "   and TYPE=" + p_type +
//                    "   and USERNAME='" + SqlUtil.quote(user) + "'" +
//                    "   and NAME='" + SqlUtil.quote(name) + "' FOR UPDATE");
//
//                rset.next();
//
//                SqlUtil.writeClob(rset, "VALUE", p_value);
//            }

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }
    }

    private void doModifyObject(int p_type, String p_user, String p_name,
        String p_value)
        throws TermbaseException, RemoteException
    {
        String user = (p_user == null || p_user.length() == 0) ?
            ALL_USERS : EditUtil.truncateUTF8Len(p_user, MAX_USERNAME_LEN);
        String name = EditUtil.truncateUTF8Len(p_name, MAX_NAME_LEN);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

//            boolean needClob = EditUtil.getUTF8Len(p_value) > 4000;
            boolean needClob = false;

            stmt.executeUpdate("update TB_USER_DATA " +
                " set VALUE=" + SqlUtil.getClobInitializer(p_value, needClob) +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                "   and USERNAME='" + SqlUtil.quote(user) + "'" +
                "   and NAME='" + SqlUtil.quote(name) + "'");

//            if (needClob)
//            {
//                rset = stmt.executeQuery(
//                    "select VALUE from TB_USER_DATA" +
//                    " where TBID=" + m_database.getId() +
//                    "   and TYPE=" + p_type +
//                    "   and USERNAME='" + SqlUtil.quote(user) + "'" +
//                    "   and NAME='" + SqlUtil.quote(name) + "' FOR UPDATE");
//
//                rset.next();
//
//                SqlUtil.writeClob(rset, "VALUE", p_value);
//            }

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }
    }

    private void doDeleteObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException
    {
        String user = (p_user == null || p_user.length() == 0) ?
            ALL_USERS : EditUtil.truncateUTF8Len(p_user, MAX_USERNAME_LEN);
        String name = EditUtil.truncateUTF8Len(p_name, MAX_NAME_LEN);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            stmt.executeUpdate("delete from TB_USER_DATA " +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                "   and USERNAME='" + SqlUtil.quote(user) + "'" +
                "   and NAME='" + SqlUtil.quote(name) + "'");

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }
    }

    private void doDeleteObjects(int p_type, String p_user)
        throws TermbaseException, RemoteException
    {
        String user = (p_user == null || p_user.length() == 0) ?
            ALL_USERS : EditUtil.truncateUTF8Len(p_user, MAX_USERNAME_LEN);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            stmt.executeUpdate("delete from TB_USER_DATA " +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                "   and USERNAME='" + SqlUtil.quote(user) + "'");

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }
    }

    private void doMakeDefaultObject(int p_type, String p_user, String p_name)
        throws TermbaseException, RemoteException
    {
        // Make Default is only meaningful for system-wide objects but
        // I'll leave in the code to let users set their own defaults.
        String user = (p_user == null || p_user.length() == 0) ?
            ALL_USERS : EditUtil.truncateUTF8Len(p_user, MAX_USERNAME_LEN);
        String name = EditUtil.truncateUTF8Len(p_name, MAX_NAME_LEN);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            stmt.executeUpdate("update TB_USER_DATA " +
                " set ISDEFAULT = 'N'" +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                "   and USERNAME='" + SqlUtil.quote(user) + "'" +
                "   and ISDEFAULT = 'Y'");

            stmt.executeUpdate("update TB_USER_DATA " +
                " set ISDEFAULT = 'Y'" +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                "   and USERNAME='" + SqlUtil.quote(user) + "'" +
                "   and NAME='" + SqlUtil.quote(name) + "'");

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }
    }

    private void doUnsetDefaultObject(int p_type, String p_user)
        throws TermbaseException, RemoteException
    {
        // Unset Default is only meaningful for system-wide objects but
        // I'll leave in the code to let users set their own defaults.
        String user = (p_user == null || p_user.length() == 0) ?
            ALL_USERS : EditUtil.truncateUTF8Len(p_user, MAX_USERNAME_LEN);

        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;

        try
        {
            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();

            stmt.executeUpdate("update TB_USER_DATA " +
                " set ISDEFAULT = 'N'" +
                " where TBID=" + m_database.getId() +
                "   and TYPE=" + p_type +
                "   and USERNAME='" + SqlUtil.quote(user) + "'" +
                "   and ISDEFAULT = 'Y'");

            conn.commit();
        }
        catch (Exception e)
        {
            try { conn.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new TermbaseException(MSG_SQL_ERROR, null, e);
        }
        finally
        {
            try
            {
                if (rset != null) rset.close();
                if (stmt != null) stmt.close();
            }
            catch (Throwable t) { /* ignore */ }

            SqlUtil.fireConnection(conn);
        }
    }

    //
    // Helper Methods
    //

    /** Checks if the current user is accessing its own data. */
    private boolean isCurrentUser(String p_user)
    {
        return m_session.getUserName().equals(p_user);
    }

    /**
     * Checks read access. A user can read system data and his own data.
     */
    private void checkReadAccess(String p_user)
        throws TermbaseException
    {
        // If the data belongs to somebody else, it cannot be read.
        if (p_user != null && p_user.length() > 0 && !isCurrentUser(p_user))
        {
            String[] args = { "caller is not allowed to view data" };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }
    }

    /**
     * Checks write access. A user can modify his own data. Only
     * administrators can modify other users' and system data.
     *
     * @param p_user this is the user that owns the object to be
     * modified, not the current user. The current user name is stored
     * in m_session.
     */
    private void checkWriteAccess(String p_user)
        throws TermbaseException
    {
        // Users with the permission to manage input models can change
        // all data (Administrator and Project Managers by default).
        if (PermissionHelper.hasPermission(m_session.getUserName(),
            Permission.TERMINOLOGY_INPUT_MODELS))
        {
            return;
        }

        // Other users cannot modify system data (which belongs to user "")
        if (p_user == null || p_user.length() == 0)
        {
            String[] args =
                {
                "caller is not allowed to create or modify system data"
                };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }

        // Other users cannot modify somebody else's data, only their own.
        if (p_user != null && p_user.length() > 0 && !isCurrentUser(p_user))
        {
            String[] args =
                {
                "caller is not allowed to create or modify other users' data"
                };
            throw new TermbaseException(MSG_INVALID_ARG, args, null);
        }
    }
}
